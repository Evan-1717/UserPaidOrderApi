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
    void batchInsertNovel(@Param("likeCountList")List<Map<String, String>> likeCountList, @Param("tableName") String tableName);

    void batchInsertPlaylet(@Param("likeCountList")List<Map<String, String>> likeCountList, @Param("tableName") String tableName);

    void insertNovel(Map<String, String> map);

    void createPlaylet(@Param("tableName") String tableName);

    void createLikeCount(@Param("tableName") String tableName);

    void createNovelInfo(@Param("tableName") String tableName);

    void dropLikeCount(@Param("tableName") String tableName);

    void dropNovelInfo(@Param("tableName") String tableName);

    List<Map<String, Object>> selectLikeCount(@Param("tableName") String tableName);

    List<Map<String, Object>> selectheatPlaylet(@Param("tableName") String tableName, @Param("time") long time);
}
