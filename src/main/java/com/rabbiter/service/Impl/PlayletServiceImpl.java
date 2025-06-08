package com.rabbiter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.entity.UserPaidOrder;
import com.rabbiter.mapper.NovelMapper;
import com.rabbiter.mapper.OrderMapper;
import com.rabbiter.service.NovelService;
import com.rabbiter.service.PlayletService;
import com.rabbiter.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class PlayletServiceImpl extends ServiceImpl<OrderMapper, UserPaidOrder> implements PlayletService {
    private static final Logger LOGGER = Logger.getLogger(PlayletServiceImpl.class.getName());

    @Autowired
    private NovelMapper novelMapper;

    @Autowired
    private RestTemplate restTemplate;

    public static List<List<List<String>>> top100_24 = new ArrayList<>();
    public static List<String> top100_24_time = new ArrayList<>();

    @Override
    public void dealTopPlaylet() {
        String token = getNoveltoken();
        novelMapper.dropLikeCount("heatPlaylet");
        novelMapper.createPlaylet("heatPlaylet");
        long time = Instant.now().getEpochSecond() * 1000;
        for(int i = 0; i < 30; i++) {
            List<Map<String,String>> data = novelmaterial(i, token, time);
            if (null == data) {
                break;
            }
            if (data.size()==0) {
                continue;
            }
            novelMapper.batchInsertPlaylet(data, "heatPlaylet");
        }
        List<Map<String,Object>> topData = novelMapper.selectheatPlaylet("heatPlaylet", Instant.now().getEpochSecond() * 1000 - 3600000 * 24 * 3);
        List<List<String>> sheet = new ArrayList<>();
        List<String> uniqueIdList = new ArrayList<>();
        for(Map<String,Object> value : topData) {
            String uniqueId = (String) value.get("uniqueId");
            if (uniqueIdList.contains(uniqueId)) {
                continue;
            }
            uniqueIdList.add(uniqueId);
            sheet.add(transform2(value));
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
    public void dealAllPlaylet() {
        String token = getNoveltoken();
        novelMapper.dropNovelInfo("heatPlaylet");
        novelMapper.createNovelInfo("heatPlaylet");
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
                    info.put("tableName", "heatPlaylet");
                    novelMapper.insertNovel(info);
                }

            }
            time+=3600000;
        }
    }

    public String getNoveltoken(){
        String url = "https://dongxiaoju.com/api/login/account";
        Map param = new HashMap() {
            {
                put("userName", "13510556226");
                put("password", "84f70722d7678b705518cf1dca6fbac6");
                put("verCode", "6226");
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
        String url = "https://dongxiaoju.com//api/v4/data/material?page="+page+"&size=500&sort=update_time,desc";
        Map param = new HashMap() {
            {
                put("startTime", time - 3600000);
                put("endTime",   time);
                put("userVideo",  true);
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
            newMap.put("miniseriesId", map.get("miniseriesId").toString());
            newMap.put("miniseriesName", map.get("miniseriesName").toString());
            newMap.put("materialId", map.get("materialId").toString());
            newMap.put("uniqueId", map.get("uniqueId").toString());
            newMap.put("title", map.get("title").toString());
            newMap.put("width", map.get("width").toString());
            newMap.put("height", map.get("height").toString());
            newMap.put("duration", map.get("duration").toString());
            newMap.put("coverUrl", map.get("coverUrl").toString());
            newMap.put("videoUrl", map.get("videoUrl").toString());
            newMap.put("likeCount", ((Map<String,Object>)map.get("likeCount")).get("countStr").toString());
            newMap.put("commentCount", ((Map<String,Object>)map.get("commentCount")).get("countStr").toString());
            newMap.put("shareCount", ((Map<String,Object>)map.get("shareCount")).get("countStr").toString());
            newMap.put("favoriteCount", ((Map<String,Object>)map.get("favoriteCount")).get("countStr").toString());
            newMap.put("heat", map.get("heat").toString());
            newMap.put("createTime", map.get("createTime").toString());
            newMap.put("updateTime", map.get("updateTime").toString());
            newMap.put("materialType", map.get("materialType").toString());
            newMap.put("videoMd5", map.get("videoMd5").toString());
            resList.add(newMap);
        }
        return resList;
    }

    private List<String> transform2(Map<String,Object> value) {
        // [miniseriesId, miniseriesName, materialId, uniqueId, title, width, height, duration, coverUrl, videoUrl, likeCount, commentCount, shareCount, favoriteCount, heat, createTime, updateTime, materialType, videoMd5]
        List<String> resList = new ArrayList<>();
        resList.add(value.get("miniseriesId").toString());
        resList.add(value.get("miniseriesName").toString());
        resList.add(value.get("miniseriesName").toString());
        resList.add(value.get("materialId").toString());
        resList.add(value.get("uniqueId").toString());
        resList.add(value.get("title").toString());
        resList.add(value.get("width").toString());
        resList.add(value.get("height").toString());
        resList.add(value.get("duration").toString());
        resList.add(value.get("coverUrl").toString());
        resList.add(value.get("videoUrl").toString());
        resList.add(value.get("likeCount").toString());
        resList.add(value.get("commentCount").toString());
        resList.add(value.get("shareCount").toString());
        resList.add(value.get("favoriteCount").toString());
        resList.add(value.get("heat").toString());
        resList.add(value.get("createTime").toString());
        resList.add(value.get("updateTime").toString());
        resList.add(value.get("materialType").toString());
        resList.add(value.get("videoMd5").toString());
        return resList;
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    public Map<String,Object> noveldetail(String materialId, String token){
        String url = "https://dongxiaoju.com/api/v4/data/detail";
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
