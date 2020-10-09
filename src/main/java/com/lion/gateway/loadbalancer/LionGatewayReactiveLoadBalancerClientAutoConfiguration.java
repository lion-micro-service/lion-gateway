package com.lion.gateway.loadbalancer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayReactiveLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;

import java.util.Objects;

/**
 * @description:
 * @author: mr.liu
 * @create: 2020-10-09 19:39
 **/
@Configuration(proxyBeanMethods = false)
public class LionGatewayReactiveLoadBalancerClientAutoConfiguration {

    @Value("${spring.cloud.gateway.development.mode.enabled:false}")
    private Boolean mode;

    @Bean
    public GlobalFilter lionGatewayLoadBalancerClientFilter(
            LoadBalancerClientFactory clientFactory, LoadBalancerProperties properties) {
        return Objects.equals(mode,true) ?
                new LionReactiveLoadBalancerClientFilter(clientFactory, properties) :
                new ReactiveLoadBalancerClientFilter(clientFactory, properties);
    }

    private static final class OnNoRibbonDefaultCondition extends AnyNestedCondition {

        private OnNoRibbonDefaultCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(value = "spring.cloud.loadbalancer.ribbon.enabled",
                havingValue = "false")
        static class RibbonNotEnabled {

        }

        @ConditionalOnMissingClass("org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient")
        static class RibbonLoadBalancerNotPresent {

        }

    }
}
