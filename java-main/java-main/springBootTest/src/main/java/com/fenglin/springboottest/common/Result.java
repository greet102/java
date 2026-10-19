package com.fenglin.springboottest.common;

import lombok.Data;

/**
 * 统一返回结果，格式与前端约定一致：{ ok, msg, data }
 */
@Data
public class Result<T> {

    private boolean ok;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null, null);
    }

    public static <T> Result<T> ok(T data) {
        return ok(null, data);
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> r = new Result<>();
        r.setOk(true);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> err(String msg) {
        Result<T> r = new Result<>();
        r.setOk(false);
        r.setMsg(msg);
        return r;
    }
}
