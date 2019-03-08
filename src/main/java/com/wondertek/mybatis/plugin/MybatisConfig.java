package com.wondertek.mybatis.plugin;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

//    @Bean
//    public Interceptor getInterceptor() {
//        System.out.println("注册拦截器");
//        return new FirstPlugin();
//    }

    /**
     *
     * @return
     */
    @Bean
    public Interceptor getInterceptor() {
        return new SqlStatsInterceptor();
    }
}
