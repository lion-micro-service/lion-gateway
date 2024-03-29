package com.lion.gateway.exception;

import com.lion.core.ResultData;
import com.lion.core.common.enums.ResultDataState;
import com.lion.utils.BeanToMapUtil;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * @description: 自定义全局异返回错误信息
 * @author: Mr.Liu
 * @create: 2020-02-05 20:45
 */
//@Component
//@Primary
//@Order()
@Deprecated //屏蔽此做法不能修改 response.setStatusCode(HttpStatus.OK);
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        ServerHttpResponse response = request.exchange().getResponse();
        response.setStatusCode(HttpStatus.OK);
        ResultData resultData = new ResultData();
        resultData.setStatus(ResultDataState.NOT_FOUND_SERVER.getKey());
        resultData.setMessage("找不到服务！请稍后重试");
        return BeanToMapUtil.transBeanToMap(resultData);
    }
}
