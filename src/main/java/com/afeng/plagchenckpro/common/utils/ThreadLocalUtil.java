package com.afeng.plagchenckpro.common.utils;

import com.afeng.plagchenckpro.entity.pojo.User;

public class ThreadLocalUtil {

    private final static ThreadLocal<Long> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    //存入线程中
    public static void setId(Long id){
        WM_USER_THREAD_LOCAL.set(id);
    }

    //从线程中获取
    public static Long getId(){
        return WM_USER_THREAD_LOCAL.get();
    }

    //清理
    public static void clear(){
        WM_USER_THREAD_LOCAL.remove();
    }
}
