package com.okeeper.openai;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp Interceptor that adds an authorization token header
 */
@Slf4j
public class DynamicTimeoutInterceptor implements Interceptor {

    DynamicTimeoutInterceptor() {
    }

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //核心代码!!!
        final Invocation tag = request.tag(Invocation.class);
        final Method method = tag != null ? tag.method() : null;
        final DynamicTimeout timeout = method != null ? method.getAnnotation(DynamicTimeout.class) : null;

        log.info("invocation",tag!= null ? tag.toString() : "");

        if(timeout !=null && timeout.timeout() > 0){

            Response proceed = chain.withConnectTimeout(timeout.timeout(), TimeUnit.SECONDS)
                    .withReadTimeout(timeout.timeout(), TimeUnit.SECONDS)
                    .withWriteTimeout(timeout.timeout(), TimeUnit.SECONDS)
                    .proceed(request);
            return proceed;
        }

        return chain.proceed(request);
    }
}
