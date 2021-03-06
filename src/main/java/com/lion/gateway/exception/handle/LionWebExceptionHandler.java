package com.lion.gateway.exception.handle;

import com.lion.core.ResultData;
import com.lion.core.common.enums.ResultDataState;
import com.lion.utils.JsonUtil;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @description: 自定义全局异返回错误信息
 * @author: Mr.Liu
 * @create: 2020-02-07 12:44
 */
@Component
@Primary
@Order(-2)
public class LionWebExceptionHandler implements WebExceptionHandler {

    @SneakyThrows
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type","application/stream+json;charset=UTF-8");
        ResultData resultData = new ResultData();
        resultData.setStatus(ResultDataState.NOT_FOUND_SERVER.getKey());
        resultData.setMessage(ex.getMessage());
        String json = JsonUtil.convertToJsonString(resultData);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(wrap));
    }
}
