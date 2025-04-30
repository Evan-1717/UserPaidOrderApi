package com.rabbiter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.entity.UserPaidOrder;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
public interface OrderService extends IService<UserPaidOrder> {
    void addOrder(Map<String, String> userPaidOrder);

    void saveOrder();
}
