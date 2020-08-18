package com.lion.gateway.loadbalancer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

/**
 * @description: 自定义LoadBalancer，为解决开发过程中服务乱窜的问题
 * @author: Mr.Liu
 * @create: 2020-07-10 14:22
 */
public class DevelopmentLoadBalancer extends RoundRobinLoadBalancer {

    public DevelopmentLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        super(serviceInstanceListSupplierProvider, serviceId);
    }

    public DevelopmentLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, int seedPosition) {
        super(serviceInstanceListSupplierProvider, serviceId, seedPosition);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return super.choose(request);
    }
}
