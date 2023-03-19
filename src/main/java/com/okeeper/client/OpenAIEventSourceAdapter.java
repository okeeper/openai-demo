package com.okeeper.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Slf4j
public class OpenAIEventSourceAdapter extends EventSourceListener {

    private volatile boolean isFinished;
    private StringBuilder fullText = new StringBuilder();
    private StringBuilder pollingText = new StringBuilder();
    private CountDownLatch downLatch = new CountDownLatch(1);
    private volatile Throwable throwable;
    private boolean isChat;
    private boolean isFirstEvent = true;
    private Consumer<String> onFinishedCallback;
    private Consumer<Throwable> onErrorCallback;

    public OpenAIEventSourceAdapter() {
        super();
    }

    public OpenAIEventSourceAdapter(boolean isChat) {
        super();
        this.isChat = isChat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(final EventSource eventSource, final Response response) {
        log.info("建立sse连接... response: {}", response.message());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEvent(final EventSource eventSource, final String
            id, final String type, final String data) {
        //log.info("onEvent {}: {}", id, data);
        if(StringUtils.isNotEmpty(data) && !"[DONE]".equals(data)) {
            JSONObject res = (JSONObject) JSON.parse(data);
            if(isChat) {
                JSONObject deltaObj = res.getJSONArray("choices").getJSONObject(0).getJSONObject("delta");
                if(deltaObj != null && !deltaObj.isEmpty() && StringUtils.isNotEmpty(deltaObj.getString("content"))) {
                    String text = deltaObj.getString("content");
                    appendText(wrapText(text));
                    isFirstEvent = false;
                }
            }else {
                String text = res.getJSONArray("choices").getJSONObject(0).getString("text");
                appendText(wrapText(text));
                isFirstEvent = false;
            }

        }
    }

    private String wrapText(String text) {
        if(text != null && text.matches("\\n+") && isFirstEvent) {
            log.warn("ignore '\\n', {}", text);
            return "";
        }else {
            return text;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClosed(final EventSource eventSource) {
        finish();
        if(onFinishedCallback != null) {
            onFinishedCallback.accept(fullText.toString());
        }
        log.info("sse onClosed. responseText={}", fullText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFailure(final EventSource eventSource, final Throwable t, final Response response) {
        try {
            log.error("使用事件源时出现异常... [响应：{}]...", JSON.toJSON(response), t);
            if(response != null && response.body() != null) {
                JSONObject jsonObject = JSON.parseObject(response.body().string(), JSONObject.class);
                throwable = new RuntimeException("OpenAI:" + jsonObject.getJSONObject("error").getString("message"));
            }else {
                throwable = t;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(onErrorCallback != null) {
                onErrorCallback.accept(throwable);
            }
            finish();
        }
    }

    private void finish() {
        downLatch.countDown();
        isFinished = true;
    }


    private synchronized void appendText(String text) {
        fullText.append(text);
        pollingText.append(text);
    }

    public synchronized PollDataDTO pollText() throws Throwable {
        String tmpText = pollingText.toString();
        PollDataDTO.PollDataDTOBuilder builder = PollDataDTO
                .builder()
                .text(tmpText)
                .isFinished(isFinished)
                ;
        if(StringUtils.isNotEmpty(tmpText)) {
            pollingText.delete(0, pollingText.length());
            builder.text(tmpText);
        }
        if(throwable != null) {
            builder.errorMsg(throwable.getMessage());
        }
        return builder.build();
    }

    public String getAllResponseText() {
        if(downLatch != null) {
            try {
                downLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return fullText.toString();
        }else {
            throw new RuntimeException("Not connect yet.");
        }
    }

    public void setOnFinishedCallback(Consumer<String> onFinishedCallback) {
        this.onFinishedCallback = onFinishedCallback;
    }

    public void setErrorCallback(Consumer<Throwable> onErrorCallback) {
        this.onErrorCallback = onErrorCallback;
    }
}
