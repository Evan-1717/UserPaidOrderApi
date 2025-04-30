package com.rabbiter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.entity.UserPaidOrder;
import com.rabbiter.mapper.PromotionMapper;
import com.rabbiter.service.PromotionService;
import com.rabbiter.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

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
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, UserPaidOrder> implements PromotionService {
    private static final Logger LOGGER = Logger.getLogger(PromotionServiceImpl.class.getName());

    @Autowired
    private PromotionMapper promotionMapper;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public void dealPromotion() {
        List<Map<String, String>> distributorList = promotionMapper.selectDistributor();
        for (Map<String, String> distributor : distributorList ) {
            List<LinkedHashMap<String, String>> promotionList = getPromotionFromHttp(distributor);
            if (null == promotionList || promotionList.size() == 0) {
                continue;
            }
            for (LinkedHashMap<String, String> pro : promotionList) {
                String promotionName = pro.get("promotion_name");
                String userName = promotionName.split("-")[0];
                pro.put("user_name", userName);
                pro.put("mini_program_name", distributor.get("mini_program_name"));
            }
            promotionMapper.batchInsertPromotion(promotionList);
        }
    }

    public List<LinkedHashMap<String, String>> getPromotionFromHttp(@RequestBody Map<String, String> distributor){
        String url = "https://www.changdunovel.com/novelsale/openapi/promotion/list/v1?sign={sign}&distributor_id={distributor_id}&ts={ts}&begin={begin}&end={end}&limit={limit}";
        String distributor_id = distributor.get("distributor_id");
        long ts = Instant.now().getEpochSecond();
        String sign = Utils.calculateMD5(distributor_id+distributor.get("secret_key")+ts);

        Map param = new HashMap() {
            {
                put("distributor_id", distributor_id);
                put("ts", ts);
                put("sign", sign);
                put("begin", ts - 86400);
                put("end", ts);
                put("limit", 1000);
            }
        };
        ResponseEntity<Map<String, Object>> res = restTemplate.getForEntity(url, Map.class, param);
        return (List<LinkedHashMap<String, String>>)res.getBody().get("result");
    }

    @Override
    public String calculateRecharge(Map<String, String> param) {
        Double count = null;
        try {
            count = promotionMapper.calculateRecharge(param);
        } catch (Exception e) {
            LOGGER.info("tableName doesn't exist:" +  param.get("tableName"));
        }
        if (null == count) {
            return "0.00";
        } else {
            return String.format("%.2f", count / 100);
        }

    }
}
