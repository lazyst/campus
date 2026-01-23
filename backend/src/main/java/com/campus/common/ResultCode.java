package com.campus.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 * 定义系统常用的响应状态码
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 错误
     */
    ERROR(500, "操作失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 服务器内部错误
     */
    SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(401, "用户名或密码错误"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(404, "用户不存在"),

    /**
     * 用户已存在
     */
    USER_EXISTS(409, "用户已存在"),

    /**
     * 令牌无效
     */
    TOKEN_INVALID(401, "令牌无效"),

    /**
     * 令牌过期
     */
    TOKEN_EXPIRED(401, "令牌已过期");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
