package com.alibaba.arthas.tunnel.server.utils;

public class CommonResultUtil {
    public static <T> CommonResult<T> errMsgWithData(String msg, T data) {
        CommonResult<T> res = new CommonResult<>();
        res.setData(data);
        res.setMsg(msg);
        return res;
    }

    public static CommonResult errMsg(String msg) {
        CommonResult<Object> res = new CommonResult<>();
        res.setData(null);
        res.setMsg(msg);
        return res;
    }
}
