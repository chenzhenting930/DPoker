package com.example.dpoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * DPoker 应用启动入口。
 * 启动完成后会在日志中输出当前激活的 profile 及实际加载的配置文件，
 * 便于运维快速确认运行环境（dev / prod 等）。
 */
@SpringBootApplication
public class DPokerApplication {

    private static final Logger log = LoggerFactory.getLogger(DPokerApplication.class);

    public static void main(String[] args) {
        // 启动 Spring Boot 应用并获取应用上下文，以便读取环境信息
        ConfigurableApplicationContext context = SpringApplication.run(DPokerApplication.class, args);
        // 从 Spring 环境中读取当前激活的 profile 列表
        Environment env = context.getEnvironment();
        String[] activeProfiles = env.getActiveProfiles();
        // 若未显式激活任何 profile，Spring 内部会回退到 default，这里统一显示为 default
        String profileInfo = activeProfiles.length == 0
                ? "default"
                : String.join(", ", activeProfiles);
        // 拼接出本次启动实际加载的配置文件名，公共 application.yaml 始终会被加载
        StringBuilder configFiles = new StringBuilder("application.yaml");
        for (String profile : activeProfiles) {
            configFiles.append(", application-").append(profile).append(".yaml");
        }

        log.info("==================================================");
        log.info("应用 [DPoker] 启动成功");
        log.info("当前激活的 Profile: {}", profileInfo);
        log.info("加载的配置文件: {}", configFiles);
        log.info("==================================================");
    }

}
