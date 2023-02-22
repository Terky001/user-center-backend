package com.terky.usercenter.common;

import lombok.Data;

public enum ErrorCode {
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NO_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    private final int code;

    private final String messsage;

    private final String description;

    ErrorCode(int code, String messsage, String description) {
        this.code = code;
        this.messsage = messsage;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMesssage() {
        return messsage;
    }

    public String getDescription() {
        return description;
    }
}
