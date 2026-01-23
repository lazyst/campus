package com.campus.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 所有API响应都使用此格式
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 额外数据（如token）
     */
    private String token;

    /**
     * 创建一个成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建一个带数据的成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建一个失败结果
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建一个指定错误码的失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建一个指定错误码和消息的失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 设置token
     */
    public Result<T> setToken(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }
}
