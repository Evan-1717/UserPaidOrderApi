package com.rabbiter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.entity.UserPaidOrder;
import com.rabbiter.mapper.AdvertiserCostMapper;
import com.rabbiter.mapper.OrderMapper;
import com.rabbiter.mapper.PromotionMapper;
import com.rabbiter.service.AdvertiserCostService;
import com.rabbiter.service.PromotionService;
import com.rabbiter.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
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
public class AdvertiserCostServiceImpl extends ServiceImpl<AdvertiserCostMapper, UserPaidOrder> implements AdvertiserCostService {
    private static final Logger LOGGER = Logger.getLogger(AdvertiserCostServiceImpl.class.getName());

    @Autowired
    private AdvertiserCostMapper advertiserCostMapper;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String url = "https://openapi.mobgi.com/api/Account/report";

    private static final String secret = "3b0a86732b58e6040805326835fdd042";

    private static final String email = "1499519575@qq.com";


    @Override
    public void dealAdvertiserCost() {
        String date = Utils.getTime3(Instant.now().getEpochSecond() * 1000);
        for(int i = 1; i < 30; i++) {
            List<Map<String,String>> batchData = getAdvertiserCost(i, date);
            if (null == batchData || batchData.size() == 0) {
                break;
            }
            dealBatchData(batchData);
        }
    }

    @Override
    public void dealDailyAdvertiser() {
        long time = Instant.now().getEpochSecond() * 1000;
        String hour = Utils.getTime6(time + "").substring(11, 13);
        if (!hour.equals("01")) {
            return;
        }
        String date = Utils.getTime3(time - 24 * 3600000);
        for(int i = 1; i < 30; i++) {
            List<Map<String,String>> batchData = getAdvertiserCost(i, date);
            if (null == batchData || batchData.size() == 0) {
                break;
            }
            for (Map<String,String> info : batchData) {
                String promotionName = info.get("media_advertiser_nick");
                Map param = new HashMap() {
                    {
                        put("tableName", date + "_userpaidorder");
                        put("promotionName", promotionName);
                    }
                };
                String recharge = "";
                try {
                    recharge = promotionService.calculateRecharge(param);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                info.put("fund_recharge", recharge);
            }
            dealDailyAdvertiser(batchData);
        }
    }

    private void dealDailyAdvertiser(List<Map<String,String>> batchData) {
        advertiserCostMapper.dealDailyAdvertiser(batchData);
    }

    private void dealBatchData(List<Map<String,String>> batchData) {
        List<String> existAdvertiserId = getAdvertiserId(batchData);
        List<Map<String,String>> existAdvertiserInfo = new ArrayList<>();
        List<Map<String,String>> newAdvertiserInfo = new ArrayList<>();
        for (Map<String,String> info : batchData) {
            String date = Utils.getTime3();
            String promotionName = info.get("media_advertiser_nick");
            Map param = new HashMap() {
                {
                    put("tableName", date + "_userpaidorder");
                    put("promotionName", promotionName);
                }
            };
            String recharge = "";
            try {
                recharge = promotionService.calculateRecharge(param);
            } catch (Exception e) {
                e.printStackTrace();
            }
            info.put("fund_recharge", recharge);
            if (existAdvertiserId.contains(info.get("advertiser_id"))) {
                info.put("update_date", date);
                existAdvertiserInfo.add(info);
            } else {
                if (promotionName.contains("iaa") || promotionName.contains("免费")) {
                    info.put("media_source", "免费");
                } else if(promotionName.contains("抖")) {
                    info.put("media_source", "抖音");
                } else {
                    info.put("media_source", "微信");
                }
                info.put("creater", promotionName.split("-")[0]);
                info.put("date", date);
                info.put("update_date", date);
                newAdvertiserInfo.add(info);
            }
        }
        if (existAdvertiserInfo.size() > 0) advertiserCostMapper.dealExistAdvertiser(existAdvertiserInfo);
        if (newAdvertiserInfo.size() > 0) advertiserCostMapper.dealNewAdvertiser(newAdvertiserInfo);
    }

    private List<String> getAdvertiserId(List<Map<String,String>> batchData) {
        List<String> ids = new ArrayList<>();
        for (Map<String,String> info : batchData) {
            ids.add(info.get("advertiser_id"));
        }
        return advertiserCostMapper.getAdvertiserId(ids);
    }

    public List<Map<String,String>> getAdvertiserCost(int page,String date) {
        long timestamp = System.currentTimeMillis()/1000;

        //请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("media_type", "toutiao_upgrade");
        params.put("kpis", new ArrayList<>(Arrays.asList(new String[]{"fund_cost", "stat_cost", "pay_amount_roi", "stat_pay_amount"})));
        params.put("page_size", 1000);
        params.put("page", page);
        params.put("start_date", date);
        params.put("end_date", date);

        try {
            //获得到签名
            String signStr = buildSignature(timestamp, params , secret);

            //设置header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
            headers.add("email",email);
            headers.add("timestamp",String.valueOf(timestamp));
            headers.add("signature",signStr);

            //接口请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return (List<Map<String,String>>)((Map<String, Object>)res.getBody().get("data")).get("list");
//            String decodedStr = Utils.decodeUnicode(body);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public String buildSignature(long timestamp, Map<String,Object> params, String secret) throws Exception {
        //加入时间戳
        params.put("timestamp", timestamp);
        //生成url参数字符串
        String signatureStr = http_build_query(params, true);
        //将密钥拼接在后面
        signatureStr += "&cl_secret=" + secret;

        //MD5
        return getMD5(signatureStr);
    }

    /**
     * @param params 参数
     * @return 返回php http_build_query()效果的URL-encode的字符串
     * @throws Exception
     */
    public String http_build_query(Map<String,Object> params) throws Exception {
        return http_build_query(params, true);
    }

    private String getMD5(String str) throws NoSuchAlgorithmException {
        DigestUtils.md5DigestAsHex(str.getBytes());
        String MD5 = DigestUtils.md5DigestAsHex(str.getBytes());
        return MD5.toUpperCase();
    }

    /**
     * Java实现PHP中的http_build_query()效果
     * https://www.cnblogs.com/timseng/p/13280722.html
     *
     * @return
     */
    private String http_build_query(Map<String, Object> array, boolean sort) throws Exception {
        String reString = "";
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        reString = rescBuild(array, "", true, sort);
        reString = StringUtils.removeEnd(reString, "&");

        //将得到的字符串进行处理得到目标格式的字符串：utf8处理中文出错
        reString = java.net.URLEncoder.encode(reString, "utf-8");
        reString = reString.replace("%3D", "=").replace("%26", "&");
        return reString;
    }

    private String rescBuild(Object object, String parentStr, boolean first, boolean sort) throws Exception {
        String r = "";
        if (object instanceof Map) {

            List<Map.Entry<String, Object>> list = new ArrayList<Map.Entry<String, Object>>(((Map<String, Object>) object).entrySet());

            //按照map的key排序
            if(sort) {
                Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
                    //升序排序
                    public int compare(Map.Entry<String, Object> o1,
                                       Map.Entry<String, Object> o2) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                });
            }

            for (Map.Entry<String, Object> mapping : list) {
                String key = mapping.getKey();
                Object value = mapping.getValue();

                if (first) {
                    r += rescBuild(value, key, false, sort);
                } else {
                    r += rescBuild(value, parentStr + "[" + key + "]", false, sort);
                }

            }

        } else if (object instanceof List) {
            for (int i = 0; i < ((List) object).size(); i++) {
                r += rescBuild(((List) object).get(i), parentStr + "[" + i + "]", false, sort);
            }
            //叶节点是String或者Number
        } else if (object instanceof String) {
            r += parentStr + "=" + object.toString() + "&";
        } else if (object instanceof Number) {
            r += parentStr + "=" + ((Number) object).toString() + "&";
        } else if (object instanceof Boolean) {
            r += parentStr + "=" + ((Boolean) object).toString() + "&";
        } else {
            throw new Exception("unsupported type:"+object.getClass().toString());
        }

        return r;
    }
}
