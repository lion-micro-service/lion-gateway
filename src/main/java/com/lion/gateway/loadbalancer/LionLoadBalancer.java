package com.lion.gateway.loadbalancer;

import com.lion.exception.BusinessException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @description: 自定义LoadBalancer，为解决开发过程中服务乱窜的问题
 * @author: Mr.Liu
 * @create: 2020-07-10 14:22
 */
public class LionLoadBalancer implements ReactorServiceInstanceLoadBalancer{

    private static final Log log = LogFactory.getLog(LionLoadBalancer.class);

    private final AtomicInteger position;

    @Deprecated
    private ObjectProvider<ServiceInstanceSupplier> serviceInstanceSupplier;

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private final String serviceId;

    private AtomicReference<String> _ip = new AtomicReference<String>();

    public LionLoadBalancer(String serviceId,
                                  ObjectProvider<ServiceInstanceSupplier> serviceInstanceSupplier) {
        this(serviceId, serviceInstanceSupplier, new Random().nextInt(1000));
    }

    public LionLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
            String serviceId) {
        this(serviceInstanceListSupplierProvider, serviceId, new Random().nextInt(1000));
    }

    public LionLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
            String serviceId, int seedPosition) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(seedPosition);
    }

    public LionLoadBalancer(String serviceId,
                                  ObjectProvider<ServiceInstanceSupplier> serviceInstanceSupplier,
                                  int seedPosition) {
        this.serviceId = serviceId;
        this.serviceInstanceSupplier = serviceInstanceSupplier;
        this.position = new AtomicInteger(seedPosition);
    }

    @SuppressWarnings("rawtypes")
    public Mono<Response<ServiceInstance>> choose(Request request) {
        if (serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                    .getIfAvailable(NoopServiceInstanceListSupplier::new);
            return supplier.get().next().map(this::getInstanceResponse);
        }
        ServiceInstanceSupplier supplier = this.serviceInstanceSupplier
                .getIfAvailable(NoopServiceInstanceSupplier::new);
        return supplier.get().collectList().map(this::getInstanceResponse);
    }

    private Response<ServiceInstance> getInstanceResponse(
            List<ServiceInstance> instances) {
        String ip = _ip.get();
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            _ip.set(null);
            return new EmptyResponse();
        }
        if (StringUtils.hasText(ip)){
            for (ServiceInstance serviceInstance : instances){
                if (serviceInstance.getHost().equals(ip)){
                    _ip.set(null);
                    return new DefaultResponse(serviceInstance);
                }
            }
        }
        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        _ip.set(null);
        return new DefaultResponse(instance);
    }

    public Mono<Response<ServiceInstance>> choose(Request request,String ip) {
        if (StringUtils.hasText(_ip.get())){
            new BusinessException("开发环境触发高并发阻断（为避免该功能失效特设此异常来阻断）");
        }
        _ip.set(ip);
        if (serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                    .getIfAvailable(NoopServiceInstanceListSupplier::new);
            //他妈的不能这么干
//            supplier.get().toStream().forEach(list -> {
//                list.forEach(serviceInstance -> {
//                });
//            });
            return supplier.get().next().map(this::getInstanceResponse);
        }
        ServiceInstanceSupplier supplier = this.serviceInstanceSupplier
                .getIfAvailable(NoopServiceInstanceSupplier::new);
        return supplier.get().collectList().map(this::getInstanceResponse);
    }



}
