package com.okeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.okeeper.config.OpenAiConfig;
import com.okeeper.controller.dto.MessageItemDTO;
import com.okeeper.entity.Chat;
import com.okeeper.entity.ChatMessage;
import com.okeeper.mapper.ChatMapper;
import com.okeeper.service.ChatMessageService;
import com.okeeper.service.ChatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zy
 * @since 2023-03-10
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private OpenAiConfig openAiConfig;

    @Override
    public Chat loadOrNewChat(Long userId, Integer chatType) {
        synchronized (userId) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("chat_type", chatType);
            Chat chat = this.getBaseMapper().selectOne(queryWrapper);
            if(chat == null) {
                chat = new Chat();
                chat.setChatType(chatType);
                chat.setUserId(userId);
                chat.setCreateTime(new Date());
                chat.setUpdateTime(new Date());
                this.save(chat);
            }
            return chat;
        }

    }
}
