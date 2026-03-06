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
//@ToString
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    //用户名
    @TableField("name")
    private String name;

    //账号
    @TableField("user_name")
    private String userName;

    //密码
    @TableField("password")
    private String password;

    //是否为管理员(0不是，1是)
    @TableField("is_manage")
    private Integer isManage;

    //创建时间
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;
}
