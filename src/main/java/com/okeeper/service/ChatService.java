package com.okeeper.service;

import com.okeeper.entity.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.okeeper.entity.ChatMessage;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zy
 * @since 2023-03-10
 */
public interface ChatService extends IService<Chat> {

    Chat loadOrNewChat(Long userId, Integer chatType);

}
