package hexlet.code.app.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;

import static hexlet.code.app.util.HeaderUtils.X_TOTAL_COUNT_HEADER_NAME;

@ControllerAdvice
public class XTotalCountHeaderAdvice implements ResponseBodyAdvice<Collection<?>> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Collection.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Collection<?> beforeBodyWrite(
            Collection<?> body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body != null) {
            response.getHeaders().add(X_TOTAL_COUNT_HEADER_NAME, String.valueOf(body.size()));
        }
        return body;
    }
}
