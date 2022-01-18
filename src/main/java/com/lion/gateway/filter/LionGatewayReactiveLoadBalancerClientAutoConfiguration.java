package com.lion.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.config.GatewayReactiveLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledGlobalFilter;
import org.springframework.cloud.gateway.filter.LoadBalancerServiceInstanceCookieFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;

import java.util.Objects;

/**
 * @description: 自定义LoadBalancer 用于解决开发环境服务乱传问题
 * @author: mr.liu
 * @create: 2020-10-09 19:39
 **/
@Configuration(proxyBeanMethods = false)
public class LionGatewayReactiveLoadBalancerClientAutoConfiguration {

    @Value("${spring.cloud.gateway.development.mode.enabled:false}")
    private Boolean mode;

    @Bean
    public ReactiveLoadBalancerClientFilter gatewayLoadBalancerClientFilter(LoadBalancerClientFactory clientFactory,
                                                                            GatewayLoadBalancerProperties properties, LoadBalancerProperties loadBalancerProperties) {
        return Objects.equals(mode,true) ?
                new LionReactiveLoadBalancerClientFilter(clientFactory,properties, loadBalancerProperties) :
                new ReactiveLoadBalancerClientFilter(clientFactory,properties,loadBalancerProperties);
    }


    @Bean
    public LoadBalancerServiceInstanceCookieFilter loadBalancerServiceInstanceCookieFilter(
            LoadBalancerProperties loadBalancerProperties) {
        return new LoadBalancerServiceInstanceCookieFilter(loadBalancerProperties);
    }


}
