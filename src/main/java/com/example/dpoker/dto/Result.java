package com.example.dpoker.dto;

import lombok.Data;

/**
 * 所有接口统一返回格式
 */
@Data
public class Result<T> {
    // 状态码：200成功，500失败，401未登录
    private int code;
    // 提示信息
    private String msg;
    // 响应数据（泛型适配任意类型）
    private T data;

    // 快速构建成功响应（无数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    // 快速构建成功响应（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 快速构建失败响应
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    // 快速构建自定义状态码的失败响应（如401未登录）
    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}