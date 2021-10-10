package com.lion.gateway.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.*;

/**
 * @description:
 * @author: mr.liu
 * @create: 2020-10-19 17:08
 **/
@Component
@Primary
public class GatewaySwaggerResourceProvider implements SwaggerResourcesProvider {

    private static final String OAS_20_URL = "/v2/api-docs";
    private static final String OAS_30_URL = "/v3/api-docs";

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Override
    public List<SwaggerResource> get() {
        List<RouteDefinition> ds = gatewayProperties.getRoutes();
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routeHosts = new ArrayList<>();
        Map<String,String> resourceName = new HashMap<String,String>();
        routeLocator.getRoutes()
                .filter(route -> route.getUri().getHost() != null)
                .filter(route -> Objects.equals(route.getUri().getScheme(), "lb"))
                .subscribe(route -> {
                    Map<String, Object> metadata = route.getMetadata();
                    if (metadata.containsKey("swagger_enable") && Objects.equals(metadata.get("swagger_enable"),"true")) {
                        routeHosts.add(route.getUri().getHost());
                        if (metadata.containsKey("resource_name") && Objects.nonNull(metadata.get("resource_name"))) {
                            resourceName.put(route.getUri().getHost(), String.valueOf(metadata.get("resource_name")));
                        }
                    }
                });

        Set<String> dealed = new HashSet<>();
        routeHosts.forEach(instance -> {
            String url = "/" + instance.toLowerCase() + OAS_30_URL;
            if (!dealed.contains(url)) {
                dealed.add(url);
                SwaggerResource swaggerResource = new SwaggerResource();
                swaggerResource.setUrl(url);
                if (resourceName.containsKey(instance)){
                    swaggerResource.setName(resourceName.get(instance));
                }else {
                    swaggerResource.setName(instance);
                }
                resources.add(swaggerResource);
            }
        });
        return resources;
    }
}
