package com.rabbiter.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
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
public interface AdvertiserCostMapper extends BaseMapper<UserPaidOrder> {

    List<String> getAdvertiserId(@Param("advertiserIdList") List<String> advertiserIdList);

    void dealExistAdvertiser(@Param("advertiserInfo") List<Map<String,String>> advertiserInfo);

    void dealNewAdvertiser(@Param("advertiserInfo") List<Map<String,String>> advertiserInfo);

    void dealDailyAdvertiser(@Param("advertiserInfo") List<Map<String,String>> advertiserInfo);
}
