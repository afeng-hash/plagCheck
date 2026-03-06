package com.afeng.plagchenckpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("batchTaskExecutor")
    public Executor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(Math.max(3, cpuCores));  // 最小3个，最多等于CPU核心数
        executor.setMaxPoolSize(Math.max(5, cpuCores * 2)); // 最大不超过CPU核心数的2倍
        executor.setQueueCapacity(50);  // 队列容量可以适当减小
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("paper-check-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}