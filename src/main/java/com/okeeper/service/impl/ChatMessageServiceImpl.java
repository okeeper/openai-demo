package com.okeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.okeeper.entity.ChatMessage;
import com.okeeper.mapper.ChatMessageMapper;
import com.okeeper.service.ChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zy
 * @since 2023-03-09
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Override
    public List<ChatMessage> queryBeforeMessageList(Long chatId, int limit) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("chat_id", chatId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("limit " + limit);
        List<ChatMessage> result = this.getBaseMapper().selectList(queryWrapper);
        Collections.sort(result, (o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        return result;
    }

    @Override
    public List<ChatMessage> queryHistryMessageList(Long chatId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("chat_id", chatId);
        queryWrapper.orderByAsc("create_time");
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public ChatMessage appendMessage(Long userId, Long chatId, String role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        chatMessage.setChatId(chatId);
        chatMessage.setUserId(userId);
        chatMessage.setCreateTime(new Date());
        chatMessage.setUpdateTime(new Date());
        this.save(chatMessage);
        return chatMessage;
    }
}
