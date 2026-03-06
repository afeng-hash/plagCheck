package com.afeng.plagchenckpro.service.impl;

import com.afeng.plagchenckpro.common.reuslt.PageResult;
import com.afeng.plagchenckpro.common.reuslt.Result;
import com.afeng.plagchenckpro.common.reuslt.ResultCodeEnum;
import com.afeng.plagchenckpro.entity.pojo.User;
import com.afeng.plagchenckpro.mapper.UserMapper;
import com.afeng.plagchenckpro.service.UserService;
import com.afeng.plagchenckpro.common.utils.JwtUtils;
import com.afeng.plagchenckpro.common.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Result login(String username, String password) {
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return Result.build(null, ResultCodeEnum.LOGIN_NULL);
        }

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserName,username);
        User user = this.getOne(lambdaQueryWrapper);

        if(user==null){
            return Result.build(null,ResultCodeEnum.ACCOUNT_ERROR);
        }

        String pswd = DigestUtils.md5DigestAsHex(password.getBytes());
        if(pswd.equals(user.getPassword())){
            Map<String,Object> map  = new HashMap<>();
            map.put("token", JwtUtils.getToken(user.getId()));
            user.setPassword("");
            map.put("user",user);

            return Result.ok(map);
        }

        return Result.build(null,ResultCodeEnum.PASSWORD_ERROR);
    }


    /**
     * 注册
     * @param user
     * @return
     */
    @Override
    public Result add(User user) {
        //参数校验
        if(user==null){
            return Result.fail();
        }
        if(StringUtils.isBlank(user.getUserName()) || StringUtils.isBlank(user.getPassword())){
            return Result.fail();
        }
        if(StringUtils.isBlank(user.getName())){
            user.setName("张三");
        }

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserName,user.getUserName());
        User one = this.getOne(lambdaQueryWrapper);
        if(one!=null){
            return Result.build(null,ResultCodeEnum.LOGIN_EXIT);
        }

        user.setIsManage(0);
        user.setCreateTime(LocalDate.now());
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5DigestAsHex);
        this.save(user);

        return Result.ok();
    }


    /**
     * 修改昵称
     * @param name
     * @return
     */
    @Override
    public Result updateName(String name) {
        if(StringUtils.isBlank(name)){
            return Result.fail();
        }

        Long id = ThreadLocalUtil.getId();
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId,id);
        lambdaUpdateWrapper.set(User::getName,name);
        userMapper.update(lambdaUpdateWrapper);

        return Result.ok();
    }


    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Override
    public Result unpdatePassword(String oldPassword, String newPassword) {
        if(StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)){
            return Result.fail();
        }

        Long id = ThreadLocalUtil.getId();

        User dbUser = this.getById(id);
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if(!md5DigestAsHex.equals(dbUser.getPassword())){
            return Result.build(null,ResultCodeEnum.PASSWORD_ERROR);
        }
        dbUser.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userMapper.updateById(dbUser);

        return Result.ok();
    }


    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名模糊搜索
     * @return 用户列表分页结果
     */
    @Override
    public Result getUserList(Integer page, Integer size, String username) {
        //检查是否是管理员
        Long id = ThreadLocalUtil.getId();
        User user = this.getById(id);
        if(user== null || user.getIsManage()!=1){
            return Result.build(null,ResultCodeEnum.NO_AUTH);
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotBlank(username),User::getUserName,username);
        Page<User> userPage = userMapper.selectPage(new Page<>(page,size),lambdaQueryWrapper);
        userPage.getRecords().forEach(user1 -> user1.setPassword(""));
        PageResult<User> pageResult = new PageResult<>(page,size,userPage.getTotal(),userPage.getRecords());
        return Result.ok(pageResult);
    }

    /**
     * 修改用户权限
     * @param user
     * @return
     */
    @Override
    public Result updateUserAdminStatus(User user) {
        //检查是否是管理员
        Long id = ThreadLocalUtil.getId();
        User nowuser = this.getById(id);
        if(nowuser== null || nowuser.getIsManage()!=1){
            return Result.build(null,ResultCodeEnum.NO_AUTH);
        }

        if(user.getId()==null || user.getIsManage()==null){
            return Result.build(null,ResultCodeEnum.ILLEGAL_REQUEST);
        }

        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId,user.getId());
        lambdaUpdateWrapper.set(user.getIsManage()!=null,User::getIsManage,user.getIsManage());
        userMapper.update(lambdaUpdateWrapper);
        return Result.ok();
    }

}
