package com.fenglin.springboottest.common;

/**
 * 业务异常：抛出后由全局异常处理器转为 Result 返回前端
 */
public class BizException extends RuntimeException {

    public BizException(String msg) {
        super(msg);
    }
}
