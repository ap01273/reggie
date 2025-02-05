package com.cqie.reggie_take_out.common;

public class BaseContext {
    //服务器会对每一次浏览器请求新建一个线程
    //通过ThreadLocal在登录检查过滤器中设置当前线程中的用户id值
    //然后在元数据对象处理器中获取用户id来设置操作用户
    private static ThreadLocal<Long>threadLocal = new ThreadLocal<>();
    public static Long getCurrentId() {
        return threadLocal.get();
    }
    public static void setCurrentId(Long currentId) {
        threadLocal.set(currentId);
    }
}
