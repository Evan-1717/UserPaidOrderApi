package com.rabbiter.mapper;

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
public interface NovelMapper extends BaseMapper<UserPaidOrder> {
    void batchInsertNovel(@Param("likeCountList")List<Map<String, String>> likeCountList);

    void insertNovel(Map<String, String> map);


    void createLikeCount();

    void createNovelInfo();

    void dropLikeCount();

    void dropNovelInfo();

    List<Map<String, Object>> selectLikeCount();
}
