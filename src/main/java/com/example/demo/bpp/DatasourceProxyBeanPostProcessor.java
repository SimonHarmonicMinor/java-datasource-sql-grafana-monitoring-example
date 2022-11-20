package com.example.demo.bpp;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {
    private final SqlInterceptor sqlInterceptor;
    private final Executor asyncExecutor;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource) {
            return ProxyDataSourceBuilder.create(dataSource)
                .listener(new QueryExecutionListener() {
                    @Override
                    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {

                    }

                    @Override
                    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                        asyncExecutor.execute(() -> sqlInterceptor.interceptQuery(execInfo, queryInfoList));
                    }
                })
                .build();
        }
        return bean;
    }

    @Configuration
    static class Config {
        @Bean
        public BeanPostProcessor beanPostProcessor(SqlInterceptor sqlInterceptor) {
            return new DatasourceProxyBeanPostProcessor(sqlInterceptor, Executors.newSingleThreadExecutor());
        }
    }
}
