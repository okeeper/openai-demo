package com.okeeper.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.okeeper.controller.dto.MessageItemDTO;
import com.okeeper.openai.LogInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OpenAIClient {

    private static final String BASE_URL = "https://api.openai.com";
    private String token;
    private OkHttpClient client;

    public OpenAIClient(String token,String proxy) {
        this.token = token;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .pingInterval(10, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new LogInterceptor())
                .connectionPool(new ConnectionPool(30, 5, TimeUnit.SECONDS));
        if(StringUtils.isNotEmpty(proxy)) {
            // 设置代理地址
            SocketAddress sa = new InetSocketAddress(proxy.split(":")[0], Integer.valueOf(proxy.split(":")[1]));
            builder.proxy(new Proxy(Proxy.Type.SOCKS, sa));
            log.info("添加代理成功：{}", proxy);
        }
        this.client = builder.build();
    }

    public WebSocket newWebSocket(String prompt, long length, String openId, OpenAIWebSocketListener socketListener) {

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text-davinci-003");
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", length);
        requestBody.put("user", openId);
        requestBody.put("stream", true);
        requestBody.put("echo", false);
        Request request = new Request.Builder()
            .url(BASE_URL + "/v1/completions").addHeader("Authorization", "Bearer " + token)
            .method("GET", RequestBody.create(MediaType.parse("application/json"), requestBody.toJSONString()))
            .build();
        return client.newWebSocket(request, socketListener);
    }

    public void newStreamRequest(String prompt, long length, String openId, EventSourceListener eventSourceListener) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text-davinci-003");
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", Math.min(Double.valueOf((prompt.length() + length) * 2.2).intValue(), 4000));
        requestBody.put("user", openId);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.6);
        requestBody.put("n", 1);
        requestBody.put("echo", false);
        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/completions")
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toJSONString()))
                .build();

        EventSource.Factory factory = EventSources.createFactory(client);
        //创建事件
        factory.newEventSource(request, eventSourceListener);
    }


    public void newChatStreamRequest(List<MessageItemDTO> messages, String openId, EventSourceListener eventSourceListener) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);
        requestBody.put("user", openId);
        requestBody.put("stream", true);
        requestBody.put("temperature", 1);
        requestBody.put("n", 1);
        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toJSONString()))
                .build();

        EventSource.Factory factory = EventSources.createFactory(client);
        //创建事件
        factory.newEventSource(request, eventSourceListener);
    }


    public JSONObject newRequest(String prompt, long length, String openId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text-davinci-003");
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", (int)(length * 2.5));
        requestBody.put("temperature", 0.8);
        requestBody.put("n", 1);
        requestBody.put("user", openId);
        requestBody.put("echo", false);
        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/completions")
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toJSONString()))
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful() && Objects.nonNull(response.body())) {
                JSONObject responseBody = (JSONObject)JSON.parse(response.body().string());
                return responseBody;
            }
            throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
