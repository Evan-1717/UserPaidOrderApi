package com.rabbiter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class NovelLike {

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "小说素菜ID")
    private String materialId;

    @ApiModelProperty(value = "点赞数")
    private String likeCount;

    @Override
    public String toString() {
        return "NovelLike{" +
                "id=" + id +
                ", materialId='" + materialId + '\'' +
                ", likeCount='" + likeCount + '\'' +
                '}';
    }

    public NovelLike(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }
}
