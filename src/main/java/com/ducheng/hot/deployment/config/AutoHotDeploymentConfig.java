package com.ducheng.hot.deployment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoHotDeploymentConfig {

    @Bean
    public NacosRefresher getNacosRefresher(){
        return  new NacosRefresher();
    }

}
