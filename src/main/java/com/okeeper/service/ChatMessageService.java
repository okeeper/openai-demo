package com.okeeper.service;

import com.okeeper.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zy
 * @since 2023-03-09
 */
public interface ChatMessageService extends IService<ChatMessage> {

    List<ChatMessage> queryBeforeMessageList(Long chatId, int limit);

    List<ChatMessage> queryHistryMessageList(Long chatId);

    ChatMessage appendMessage(Long userId, Long chatId, String role, String content);
}
