package com.rabbiter.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.entity.UserPaidOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
@Mapper
public interface OrderMapper extends BaseMapper<UserPaidOrder> {
    void batchInsertOrder(Map<String, Object> map);

    void createOrder(@Param("tableName")String tableName);

    void insertPromotion(Map<String, String> map);

    List<Map<String, String>> selectPromotionById(@Param("promotion_id")String promotion_id);

    List<Map<String, String>> selectDistributorById(@Param("distributorId")String distributorId);

    List<Map<String, String>> selectPerantDistributor(@Param("distributorList")List<String> distributorList);

    void insertDistributor(Map<String, String> map);
}
