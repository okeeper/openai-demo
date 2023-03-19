package com.okeeper.openai;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;

/**
 * OkHttp Interceptor that adds an authorization token header
 */
@Slf4j
public class LogInterceptor implements Interceptor {

    public LogInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            long st = System.currentTimeMillis();
            Response response = chain.proceed(request);
            String responseBody = response.peekBody(1024).string();
            log.info("{} {} request body {}, response {}, cost {}", request.method(), request.url(), bodyToString(request), responseBody, System.currentTimeMillis() - st);
            return response;
        }catch (Throwable e) {
            throw e;
        }
    }

    private String bodyToString(Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            RequestBody body = copy.body();
            if (body != null) {
                body.writeTo(buffer);
            }
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "error";
        }
    }
}
