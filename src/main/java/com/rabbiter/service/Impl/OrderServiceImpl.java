package com.rabbiter.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rabbiter.entity.UserPaidOrder;
import com.rabbiter.mapper.OrderMapper;
import com.rabbiter.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
public class OrderServiceImpl extends ServiceImpl<OrderMapper, UserPaidOrder> implements OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderServiceImpl.class.getName());

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RestTemplate restTemplate;

    ConcurrentHashMap<String, List<UserPaidOrder>> orderListMap = new ConcurrentHashMap<>();

    ConcurrentHashMap<String, String> promotionInfo = new ConcurrentHashMap<>();

    public static final List<String> DISTRIBUTOR_LIST =
            Collections.unmodifiableList(Arrays.asList("1823743747048794", "1812882522221578", "1805370052162640",
                    "1809883619983433", "1810965673071643", "1819403410193434"));

    @Override
    public void saveOrder() {
        LOGGER.info("saveOrder，size: "+ orderListMap.toString() );
        for (String data : orderListMap.keySet()) {
            orderMapper.createOrder(data+"_userpaidorder");
            Map<String, Object> map = new HashMap<>();
            map.put("tableName", data+"_userpaidorder");
            map.put("orderList", orderListMap.get(data));
            orderMapper.batchInsertOrder(map);
            LOGGER.info("saveOrder，size: "+ orderListMap.get(data).size() + ",tableName:" + data+"_userpaidorder");
        }
        orderListMap.clear();
    }

    @Override
    public void addOrder(Map<String, String> userPaidOrder) {
        UserPaidOrder order = new UserPaidOrder(userPaidOrder);
        Long timestamp = Long.parseLong(order.getPay_timestamp());
        order.setPay_timestamp(transitionTime(timestamp * 1000, "yyyy-MM-dd HH:mm:ss"));
        order.setPromotion_name(getPromotionName(order.getDistributor_id(), order.getPromotion_id(), timestamp));
        String date = transitionTime(timestamp * 1000, "yyyy-MM-dd");
        synchronized (orderListMap){
            if (orderListMap.keySet().contains(date)) {
                orderListMap.get(date).add(order);
            } else {
                List<UserPaidOrder> list = new ArrayList<>();
                list.add(order);
                orderListMap.put(date, list);
            }
        }
    }

    private String transitionTime(Long date, String pattern) {
         SimpleDateFormat sdf = new SimpleDateFormat(pattern);
         return sdf.format(new Date(date));
    }

    private String getPromotionName (String distributorId, String promotionId, Long timestamp) {
        if (!StringUtils.isEmpty(promotionInfo.get(promotionId))) {
            return promotionInfo.get(promotionId);
        }
        List<Map<String, String>> promotion = orderMapper.selectPromotionById(promotionId);

        if (promotion.size() > 0 && !StringUtils.isEmpty(promotion.get(0).get("promotion_name"))) {
            String databasePromotion_name = promotion.get(0).get("promotion_name");
            promotionInfo.put(promotionId, databasePromotion_name);
            if (promotionInfo.size() > 2000) {
                promotionInfo.clear();
            }
            return databasePromotion_name;
        }
        Map<String, String> promotionInfo1 = getPromotionNameFromHttp(distributorId, promotionId, timestamp);
        String promotionName = promotionInfo1.get("promotion_name");
        String userName = "";
        if (StringUtils.isEmpty(promotionName)) {
            promotionName = "";
        } else {
            userName = promotionName.split("-")[0];
        }
        promotionInfo.put(promotionId, promotionName);
        if (promotionInfo.size() > 2000) {
            promotionInfo.clear();
        }

        promotionInfo1.put("promotion_id", promotionId);
        promotionInfo1.put("user_name", userName);
        orderMapper.insertPromotion(promotionInfo1);
        return promotionName;
    }

    private Map<String, String> getPromotionNameFromHttp (String distributorId, String promotionId, Long timestamp) {
        Map<String, String> distributorInfo = new HashMap<>();
        try {
            List<Map<String, String>> distributorInfoList = orderMapper.selectDistributorById(distributorId);
            if (distributorInfoList.size() == 0) {
                distributorInfo = getDistributorInfoFromHttp(distributorId);
            } else {
                distributorInfo = distributorInfoList.get(0);
            }
            String url = "https://www.changdunovel.com/novelsale/openapi/promotion/list/v1?sign={sign}&distributor_id={distributor_id}&ts={ts}&promotion_id={promotion_id}";
            Long distributor_id = Long.parseLong(distributorId);
            Long ts = timestamp;
            String sign = calculateMD5(distributor_id + distributorInfo.get("secret_key") + ts);
            Map param = new HashMap() {
                {
                    put("distributor_id", distributor_id);
                    put("ts", ts);
                    put("sign", sign);
                    put("promotion_id", promotionId);
                }
            };
            LOGGER.info("getPromotionNameFromHttp,distributor_id:" + distributor_id + ",promotionId:" + promotionId);
            ResponseEntity<Map<String, Object>> res = restTemplate.getForEntity(url, Map.class, param);
            distributorInfo.put ("promotion_name", ((ArrayList<LinkedHashMap<String, String>>)res.getBody().get("result")).get(0).get("promotion_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return distributorInfo;
    }

    private Map<String, String> getDistributorInfoFromHttp(String distributorId){
        LOGGER.info("find new distributorId: " + distributorId);
        Map<String, String> distributorInfo = new HashMap<>();
        List<Map<String, String>> perantDistributorInfoList = orderMapper.selectPerantDistributor(DISTRIBUTOR_LIST);
        out: for (Map<String, String> perantDistributorInfo : perantDistributorInfoList) {
            List<LinkedHashMap<String, Object>> childDistributorList = getDistributorListFromHttp(perantDistributorInfo);
            for (LinkedHashMap<String, Object> childDistributor : childDistributorList) {
                String childDistributorid = childDistributor.get("distributor_id").toString();
                if (distributorId.equals(childDistributorid)) {
                    perantDistributorInfo.put("distributor_id", distributorId);
                    distributorInfo = perantDistributorInfo;
                    orderMapper.insertDistributor(distributorInfo);
                    break out;
                }
            }
        }
        return distributorInfo;
    }

    private List<LinkedHashMap<String, Object>> getDistributorListFromHttp(Map<String, String> params){
        String url = "https://www.changdunovel.com/novelsale/openapi/wx/get_bound_package_list/v1/?sign={sign}&distributor_id={distributor_id}&ts={ts}&app_id={app_id}&page_size={page_size}&page_index={page_index}";
        String distributor_id = params.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = calculateMD5(distributor_id+params.get("secret_key")+ts);

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
        return ((ArrayList<LinkedHashMap<String, Object>>)res.getBody().get("wx_package_info_open_list"));
    }

    private static String calculateMD5(String input) {
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
}
