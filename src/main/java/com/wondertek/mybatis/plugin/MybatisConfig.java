package com.wondertek.mybatis.plugin;

import com.github.pagehelper.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    public Interceptor getInterceptor() {
        System.out.println("注册拦截器");
        return new FirstPlugin();
    }

}
