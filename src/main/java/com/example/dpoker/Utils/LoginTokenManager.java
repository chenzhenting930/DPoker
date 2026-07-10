package com.example.dpoker.Utils;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局Token管理器（内存存储，线程安全）
 * key：随机token，value：用户名
 */
public class LoginTokenManager {
    // ConcurrentHashMap保证多线程下安全
    private static final Map<String, Integer> TOKEN_MAP = new ConcurrentHashMap<>();

    // 生成token并绑定用户名（HTTP登录成功时调用）
    public static String generateToken(Integer useId) {
        String token = UUID.randomUUID().toString().replace("-", ""); // 32位随机token
        TOKEN_MAP.put(token, useId);
        return token;
    }

    // 验证token并获取用户名（WebSocket握手时调用）
    // ConcurrentHashMap 不允许 null key，需前置判空避免 NPE
    public static Integer validateToken(String token) {
        if (token == null || token.isEmpty()) return null;
        return TOKEN_MAP.get(token);
    }

    // 验证后删除token（避免重复使用）
    public static void removeToken(String token) {
        TOKEN_MAP.remove(token);
    }
}