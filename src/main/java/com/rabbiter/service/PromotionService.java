package com.rabbiter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.entity.UserPaidOrder;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
public interface PromotionService extends IService<UserPaidOrder> {
    void dealPromotion();

    String calculateRecharge(Map<String, String> param);
}
