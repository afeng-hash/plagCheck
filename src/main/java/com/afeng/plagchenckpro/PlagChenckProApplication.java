package com.afeng.plagchenckpro;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

@SpringBootApplication
@MapperScan("com.afeng.plagchenckpro.mapper")
public class PlagChenckProApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlagChenckProApplication.class, args);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
