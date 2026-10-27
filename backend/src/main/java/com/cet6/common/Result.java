package com.cet6.common;

/** 统一返回结构:code=0 成功,其余失败。前端拦截器按此约定处理。 */
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0; r.msg = "ok"; r.data = data;
        return r;
    }

    public static <T> Result<T> ok() { return ok(null); }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.code = 1; r.msg = msg;
        return r;
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public T getData() { return data; }
}
