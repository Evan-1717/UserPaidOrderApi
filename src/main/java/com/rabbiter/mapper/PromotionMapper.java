package com.rabbiter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.rabbiter.entity.UserPaidOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
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
public interface PromotionMapper extends BaseMapper<UserPaidOrder> {
    List<Map<String, String>> selectDistributor();

    void batchInsertPromotion(List<LinkedHashMap<String, String>> list);

    Double calculateRecharge (Map<String, String> param);
}
