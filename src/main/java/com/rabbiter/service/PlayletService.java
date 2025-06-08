package com.rabbiter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.entity.UserPaidOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
public interface PlayletService extends IService<UserPaidOrder> {
    void dealTopPlaylet();

    void dealAllPlaylet();
}
