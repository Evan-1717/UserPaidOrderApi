package com.rabbiter.controller;



import com.alibaba.fastjson.JSONObject;
import com.rabbiter.common.Result;
import com.rabbiter.service.OrderService;
import com.rabbiter.service.PromotionService;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 *  前端控制器：仓库管理模块
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/userpaidorder")
    @ResponseBody
    public void userPaidOrder(@RequestParam Map<String, String> userPaidOrder){
        LOGGER.info("推送-------------：" + userPaidOrder.toString());
        orderService.addOrder(userPaidOrder);
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void saveOrder(){
        orderService.saveOrder();
    }

    /*
     * 回调授权
     * @author rabbiter
     * @date 2023/1/2 19:11
     */
    @GetMapping("/show")
    @ResponseBody
    public Result showAuthCode(){
        Map<String, String> map = new HashMap();
        map.put("auth_code", "hello");
        return Result.success(map);
    }

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/userpaidordertest")
    @ResponseBody
    public void userPaidOrdertest(@RequestParam Map<String, String> userPaidOrder){
        LOGGER.info("userpaidorder---------:"+ userPaidOrder.toString());
        userPaidOrder.put("promotion_id", "1828727633751065");
        userPaidOrder.put("pay_timestamp", Instant.now().getEpochSecond()+"");
        userPaidOrder.put("distributor_id", "1827445441397051");
        orderService.addOrder(userPaidOrder);
    }

//    /*
//     * 检查当日是否重复录入
//     * @author rabbiter
//     * @date 2023/1/5 19:42
//     */
//    @PostMapping("/getuser")
//    public Map<String, Object> getuser(){
//        return userInfo;
//    }

    @PostMapping("/api5")
    public String api5(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/content/book_meta/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&book_id={book_id}";
        String distributor_id = params.get("distributor_id");
        Long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("book_id", params.get("book_id"));
            }
        };
        ResponseEntity<Map<String,Object>> res = restTemplate.getForEntity(url, Map.class, param);
        LinkedHashMap<String, Object> resultList = ((List<LinkedHashMap<String, Object>>)res.getBody().get("result")).get(0);
        return res.getBody().toString();
    }

    @PostMapping("/api7")
    public String api7(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/promotion/create/v1?sign={sign}&distributor_id={distributor_id}&ts={ts}&media_source={media_source}&index={index}&book_id={book_id}&promotion_name={promotion_name}";
        String distributor_id = params.get("distributor_id");
        String ts = params.get("ts");
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("book_id", params.get("book_id"));
                put("index", params.get("index"));
                put("media_source", params.get("media_source"));
                put("promotion_name", params.get("promotion_name"));

            }
        };
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, param);
        return res.getBody();
    }

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/api11")
    public String api11(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/roi_analyze/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&promotion_id={promotion_id}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("promotion_id", params.get("promotion_id"));
            }
        };
        ResponseEntity<Map<String,Object>> ress = restTemplate.getForEntity(url, Map.class, param);
        List<LinkedHashMap<String, Object>> resultList = (List<LinkedHashMap<String, Object>>)ress.getBody().get("data");
        return ress.getBody().toString();
    }

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/api15")
    public String api15(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/recharge_template/list/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&page_size={page_size}&page_index={page_index}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("page_index", 0);
                put("page_size", 50);
            }
        };
        ResponseEntity<Map<String,Object>> ress = restTemplate.getForEntity(url, Map.class, param);
        List<LinkedHashMap<String, Object>> resultList = (List<LinkedHashMap<String, Object>>)ress.getBody().get("data");
        return ress.getBody().toString();
    }

    @PostMapping("/api8")
    public String api8(@RequestBody Map<String, String> params){
        promotionService.dealPromotion();
        String url = "https://www.changdunovel.com/novelsale/openapi/promotion/list/v1?sign={sign}&distributor_id={distributor_id}&ts={ts}&begin={begin}&end={end}&offset={offset}&limit={limit}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("begin", ts-86400);
                put("end", ts);
                put("offset", 0);
                put("limit", 100);
            }
        };
        ResponseEntity<Map<String, Object>> res = restTemplate.getForEntity(url, Map.class, param);
        List<LinkedHashMap<String, Object>> resultList = (List<LinkedHashMap<String, Object>>)res.getBody().get("result");
        return resultList.toString();
    }

    @PostMapping("/api20")
    public String api20(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/distributor/get_activity/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
            }
        };
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, param);
        return res.getBody();
    }

    @PostMapping("/api23")
    public String api23(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/content/chapter_list/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&book_id={book_id}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("book_id", params.get("book_id"));
            }
        };
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, param);
        return res.getBody();
    }

    @PostMapping("/api24")
    public String api24(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/content/chapter_detail/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&book_id={book_id}&chapter_id={chapter_id}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("book_id", params.get("book_id"));
                put("chapter_id", params.get("chapter_id"));
            }
        };
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, param);
        return res.getBody();
    }

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/api25")
    public String api25(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/wx/get_package_list/v2/?sign={sign}&distributor_id={distributor_id}&ts={ts}&app_type={app_type}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("app_type", 7);
            }
        };
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, param);
        return res.getBody();
    }

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/api26")
    public String api26(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/wx/get_bound_package_list/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&app_id={app_id}&page_size={page_size}&page_index={page_index}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"0NX8os544Whz4kN9vE6kqTOUseNZ36lV"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("app_id", params.get("app_id"));
                put("page_index", 0);
                put("page_size", 50);
            }
        };
        ResponseEntity<Map<String, Object>> res = restTemplate.getForEntity(url, Map.class, param);
        List list =  ((ArrayList<LinkedHashMap<String, String>>)res.getBody().get("wx_package_info_open_list"));
        return list.toString();
    }

    /*
     * 检查当日是否重复录入
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/api28")
    public List<LinkedHashMap<String, Object>> api28(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/ad_callback_config/config_list/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&media_source={media_source}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("media_source", params.get("media_source"));
            }
        };
        ResponseEntity<Map<String,Object>> res = restTemplate.getForEntity(url, Map.class, param);
        List<LinkedHashMap<String, Object>> resultList = (List<LinkedHashMap<String, Object>>)res.getBody().get("config_list");
        return resultList;
    }

    public static String calculateMD5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/noval")
    public String noval(@RequestBody Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/ad_callback_config/config_list/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&media_source={media_source}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+"gTHDJrzsiUOwepLIv6hq9AZ0nNf2aQCx"+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("media_source", params.get("media_source"));
            }
        };
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class, param);
        return res.getBody();
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/novel/token")
    public String noveltoken(@RequestBody Map<String, String> params){
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
        return (String)((Map<String, Object>)res.getBody().get("data")).get("token");
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/novel/material")
    public String novelmaterial(@RequestBody Map<String, String> params){
        String page = params.get("page");
        String url = "https://dongxiaoju.com/api/v4/data/novel/material?page="+page+"&size=500&sort=update_time,desc";
        long time = Instant.now().getEpochSecond() * 1000;
        Map param = new HashMap() {
            {
                put("startTime", time - 3600000);
                put("endTime",   time);
            }
        };
        String token = "3001902de792a99e4277bce375ccdf9940da";
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",  token);
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(param, headers);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        List<Map<String,Object>> list = (List<Map<String,Object>>)((Map<String, Object>)res.getBody().get("data")).get("list");
        List<Map<String,String>> newList = transform(list);
        return " ";
    }

    private List<Map<String,String>> transform(List<Map<String,Object>> list) {
        List<Map<String,String>> resList = new ArrayList<>();
        for (Map<String,Object> map : list) {
            Map<String,String> newMap = new HashMap<>();
            newMap.put("materialId", map.get("materialId").toString());
            String likeCount = ((Map<String,Object>)map.get("likeCount")).get("countStr").toString();
            if ("0".equals(likeCount)) {
                continue;
            }
            newMap.put("likeCount", likeCount);
            resList.add(newMap);
        }
        return resList;
    }

    /*
     * 小说信息
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping("/novel/detail")
    public String noveldetail(@RequestBody Map<String, String> params){
        String url = "https://dongxiaoju.com/api/v4/data/novel/detail";
        List<String> list = new ArrayList<>();
        list.add("97f1f4b5d67c4b259cc48883f7b6391c");
        list.add("0d89075b1a40447c86dd520762063df6");
        list.add("d5c851aedebe4eeaa66156b7e49fa155");
        Map param = new HashMap() {
            {
                put("list", new String[]{"97f1f4b5d67c4b259cc48883f7b6391c", "0d89075b1a40447c86dd520762063df6"});
            }
        };
        String token = "3001902de792a99e4277bce375ccdf9940da";
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",  token);
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(param, headers);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Object lisst = ((Map<String, Object>)res.getBody().get("data")).get("list");
        return "";
    }


}
