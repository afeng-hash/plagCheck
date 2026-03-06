package com.afeng.plagchenckpro.service;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    Result login(String username, String password);

    /**
     * 注册
     * @param user
     * @return
     */
    Result add(User user);

    /**
     * 修改昵称
     * @param name
     * @return
     */
    Result updateName(String name);


    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     * @return
     */
    Result unpdatePassword(String oldPassword, String newPassword);

    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名模糊搜索
     * @return 用户列表分页结果
     */
    Result getUserList(Integer page, Integer size, String username);


    /**
     * 修改用户管理员状态
     * @param user
     * @return
     */
    Result updateUserAdminStatus(User user);
}
