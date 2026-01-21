package com.example.dpoker.dto;

import lombok.Data;

/**
 * 所有接口统一返回格式
 */
@Data
public class Result {
    // 状态码：200成功，500失败，401未登录
    private int code;
    // 提示信息
    private String msg;
    // 响应数据（泛型适配任意类型）
    private Object data;

    // 快速构建成功响应（无数据）
    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    // 快速构建成功响应（带数据）
    public static Result success(Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static Result success(String msg,Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 快速构建失败响应
    public static Result fail(String msg) {
        Result result = new Result();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }

    // 快速构建自定义状态码的失败响应（如401未登录）
    public static Result fail(int code, String msg,Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}