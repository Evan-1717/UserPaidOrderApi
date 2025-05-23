package com.rabbiter.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.entity.UserPaidOrder;
import com.rabbiter.mapper.NovelMapper;
import com.rabbiter.mapper.OrderMapper;
import com.rabbiter.service.NovelService;
import com.rabbiter.service.OrderService;
import com.rabbiter.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
@Service
public class NovelServiceImpl extends ServiceImpl<OrderMapper, UserPaidOrder> implements NovelService {
    private static final Logger LOGGER = Logger.getLogger(NovelServiceImpl.class.getName());

    @Autowired
    private NovelMapper novelMapper;

    @Autowired
    private RestTemplate restTemplate;

    public static List<List<List<String>>> top100_24 = new ArrayList<>();
    public static List<String> top100_24_time = new ArrayList<>();

    @Override
    public void dealTopNovel() {
        String token = getNoveltoken();
        novelMapper.dropLikeCount();
        novelMapper.createLikeCount();
        long time = Instant.now().getEpochSecond() * 1000;
        for(int i = 0; i < 30; i++) {
            List<Map<String,String>> data = novelmaterial(i, token, time);
            if (null == data) {
                break;
            }
            if (data.size()==0) {
                continue;
            }
            novelMapper.batchInsertNovel(data);
        }
        List<Map<String,Object>> topData = novelMapper.selectLikeCount();
        List<List<String>> sheet = new ArrayList<>();
        for(Map<String,Object> value : topData) {
            Map<String,Object> detail = noveldetail(value.get("materialId").toString(), token);
            if (!"黑岩".equals(detail.get("platform").toString())) {
                continue;
            }
            sheet.add(transform2(value, detail));
        }
        String sheetName = Utils.getTime6(time+"").substring(5, 13) + "点";
        top100_24_time.add(0, sheetName);
        top100_24.add(0, sheet);
        if (top100_24.size() > 24) {
            top100_24.remove(24);
            top100_24_time.remove(24);
        }
    }

    /*
     * 一次使用
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @Override
    public void dealAllNovel() {
        String token = getNoveltoken();
        novelMapper.dropNovelInfo();
        novelMapper.createNovelInfo();
        long time = Instant.now().getEpochSecond() * 1000;
        time -= (165*3600000);
        for(int ind = 0; ind<165 ; ind++) {
            for(int i = 0; i < 30; i++) {
                List<Map<String,String>> data = novelmaterial(i, token, time);
                if (null == data) {
                    break;
                }
                for(Map<String,String> info : data) {
                    Map<String,Object> detail = noveldetail(info.get("materialId"), token);
                    info.put("awemeId", detail.get("awemeId").toString());
                    info.put("awemeUrl", detail.get("awemeUrl").toString());
                    info.put("title", detail.get("title").toString());
                    info.put("bookId", detail.get("bookId").toString());
                    info.put("publishTime", Utils.getTime6(detail.get("publishTime").toString()));
                    info.put("createTime", Utils.getTime6(detail.get("createTime").toString()));
                    info.put("updateTime", Utils.getTime6(detail.get("updateTime").toString()));
                    novelMapper.insertNovel(info);
                }

            }
            time+=3600000;
        }
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    public String getNoveltoken(){
        String url = "https://dongxiaoju.com/api/login/account";
        Map param = new HashMap() {
            {
                put("userName", "18717818587");
                put("password", "74a89f62cf4aad53bfc69cfbdd38756c");
                put("verCode", "8587");
                put("loginType", "OPENAPI");
            }
        };
        ResponseEntity<Map> res = restTemplate.postForEntity(url, param, Map.class);
        return (String)((Map)res.getBody().get("data")).get("token");
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    public List<Map<String,String>> novelmaterial(int page, String token, long time){
        String url = "https://dongxiaoju.com/api/v4/data/novel/material?page="+page+"&size=500&sort=update_time,desc";
        Map param = new HashMap() {
            {
                put("startTime", time - 3600000);
                put("endTime",   time);
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",  token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(param, headers);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        List<Map<String,Object>> list = (List<Map<String,Object>>)((Map<String, Object>)res.getBody().get("data")).get("list");
        List<Map<String,String>> newList = transform(list);
        if (list.size()>0) {
            return newList;
        }
        return null;
    }

    private List<Map<String,String>> transform(List<Map<String,Object>> list) {
        List<Map<String,String>> resList = new ArrayList<>();
        for (Map<String,Object> map : list) {
            Map<String,String> newMap = new HashMap<>();
            newMap.put("materialId", map.get("materialId").toString());
            String likeCount = ((Map<String,Object>)map.get("likeCount")).get("countStr").toString();
            newMap.put("likeCount", likeCount);
            newMap.put("commentCount", ((Map<String,Object>)map.get("commentCount")).get("countStr").toString());
            newMap.put("shareCount", ((Map<String,Object>)map.get("shareCount")).get("countStr").toString());
            newMap.put("favoriteCount", ((Map<String,Object>)map.get("favoriteCount")).get("countStr").toString());
            newMap.put("heat", map.get("heat").toString());
            resList.add(newMap);
        }
        return resList;
    }

    private List<String> transform2(Map<String,Object> value, Map<String,Object> detail) {
        List<String> resList = new ArrayList<>();
        resList.add(value.get("materialId").toString());
        resList.add(detail.get("bookId").toString());
        resList.add(detail.get("bookName").toString());
        resList.add(detail.get("heat").toString());
        resList.add(value.get("likeCount").toString());
        resList.add(value.get("commentCount").toString());
        resList.add(value.get("shareCount").toString());
        resList.add(value.get("favoriteCount").toString());
        resList.add(detail.get("awemeId").toString());
        resList.add(detail.get("awemeUrl").toString());
        resList.add(detail.get("title").toString());
        resList.add(Utils.getTime6(detail.get("publishTime").toString()));
        resList.add(Utils.getTime6(detail.get("createTime").toString()));
        resList.add(Utils.getTime6(detail.get("updateTime").toString()));
        return resList;
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    public Map<String,Object> noveldetail(String materialId, String token){
        String url = "https://dongxiaoju.com/api/v4/data/novel/detail";
        Map param = new HashMap() {
            {
                put("list", new String[]{materialId});
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",  token);
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(param, headers);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return ((List<Map<String,Object>>)((Map<String, Object>)res.getBody().get("data")).get("list")).get(0);
    }
}
