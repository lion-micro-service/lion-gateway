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

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }

    public synchronized Mono<Response<ServiceInstance>> choose(Request request,String ip) {
        if (serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                    .getIfAvailable(NoopServiceInstanceListSupplier::new);
            Mono<Response<ServiceInstance>> serviceInstance = supplier.get().next().map(list -> {
                if (list.isEmpty()) {
                    log.warn("No servers available for service: " + this.serviceId);
                    return new EmptyResponse();
                }
                if (StringUtils.hasText(ip)){
                    for (ServiceInstance si : list){
                        if (si.getHost().equals(ip)){
                            return new DefaultResponse(si);
                        }
                    }
                }
                int pos = Math.abs(this.position.incrementAndGet());
                ServiceInstance instance = list.get(pos % list.size());
                return new DefaultResponse(instance);
            });
            return serviceInstance;
        }
        ServiceInstanceSupplier supplier = this.serviceInstanceSupplier
                .getIfAvailable(NoopServiceInstanceSupplier::new);
        return supplier.get().collectList().map(this::getInstanceResponse);
    }



}
