package com.afeng.plagchenckpro.entity.dto;

import lombok.Data;

@Data
public class UserDto {
    //昵称
    private String name;

    //原密码
    private String oldPassword;

    //新密码
    private String newPassword;
}
