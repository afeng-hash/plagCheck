package com.afeng.plagchenckpro.controller;

import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.entity.dto.UserDto;
import com.afeng.plagchenckpro.entity.pojo.User;
import com.afeng.plagchenckpro.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     * @param
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        log.info("登录：{},{}",user.getUserName(),user.getPassword());

        return userService.login(user.getUserName(),user.getPassword());
    }


    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("/register")
    public Result add(@RequestBody User user){
        log.info("注册：{}",user.toString());
        return userService.add(user);
    }


    /**
     * 修改昵称
     */
    @PutMapping("/update/name")
    public Result undateName(@RequestBody UserDto userDto){
        log.info("修改昵称：{}",userDto.getName());
        return userService.updateName(userDto.getName());
    }

    /**
     * 修改密码
     */
    @PutMapping("/update/password")
    public Result updatePassword(@RequestBody UserDto userDto){
        log.info("修改密码：{},{}",userDto.getOldPassword(),userDto.getNewPassword());
        return userService.unpdatePassword(userDto.getOldPassword(),userDto.getNewPassword());
    }


    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名模糊搜索
     * @return 用户列表分页结果
     */
    @GetMapping("/pagelist")
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        log.info("获取用户列表：page={}, size={}, username={}", page, size, username);
        return userService.getUserList(page, size, username);
    }


    /**
     * 修改用户管理员状态
     * @return 操作结果
     */
    @PutMapping("/update/admin")
    public Result updateUserAdminStatus(@RequestBody User user) {

        return userService.updateUserAdminStatus(user);
    }
}
