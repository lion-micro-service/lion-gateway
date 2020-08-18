package com.lion.gateway;

import com.lion.aop.exception.RestulException;
import com.lion.config.EntityAuditorConfiguration;
import com.lion.gateway.loadbalancer.DevelopmentLoadBalancerClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication ()
@ComponentScan(value = "com.lion.**", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {EntityAuditorConfiguration.class, RestulException.class, LoadBalancerClientConfiguration.class}))
@EnableDiscoveryClient
@LoadBalancerClients(defaultConfiguration = {DevelopmentLoadBalancerClientConfiguration.class})
public class ApplicationGateway {

    public static void main (String[] args) {
        /*
         * new SpringApplicationBuilder(Application.class)
         * .web(WebApplicationType.NONE) .run(args);
         */
        SpringApplication.run(ApplicationGateway.class, args);
    }
}