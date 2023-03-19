package com.okeeper.controller.dto;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Result<T> implements Serializable {
    private T data;
    /**
     * 通信状态
     */
    private boolean success = true;

    private String msg;

    public Result(T data) {
        this.data = data;
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    /**
     * 通过静态方法获取实例
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(null, false, msg);
    }

}
