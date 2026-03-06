package com.afeng.plagchenckpro.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@TableName("feedback")
public class Feedback {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //用户id
    @TableField("user_id")
    private Long userId;

    //用户名
    @TableField("name")
    private String name;

    //邮箱
    @TableField("email")
    private String email;

    //评分
    @TableField("rating")
    private String rating ;

    //反馈内容
    @TableField("comment")
    private String comment;

    //反馈时间
    @TableField("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
