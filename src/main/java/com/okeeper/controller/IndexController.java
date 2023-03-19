package com.okeeper.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.okeeper.client.OpenAIClient;
import com.okeeper.client.OpenAIEventSourceAdapter;
import com.okeeper.client.OpenAIEventSourceHolder;
import com.okeeper.client.PollDataDTO;
import com.okeeper.config.OpenAiConfig;
import com.okeeper.controller.dto.*;
import com.okeeper.entity.Chat;
import com.okeeper.entity.ChatMessage;
import com.okeeper.entity.User;
import com.okeeper.openai.OpenAiService;
import com.okeeper.service.ChatMessageService;
import com.okeeper.service.ChatService;
import com.okeeper.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/")
public class IndexController {

    @Autowired
    private OpenAIClient openAIClient;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private OpenAiConfig openAiConfig;

    @Autowired
    private UserService userService;

    /**
     * 拉取结果
     * @return
     */
    @RequestMapping("polling")
    public Result<PollDataDTO> polling(@Valid @NotNull String requestId) throws Throwable {
        OpenAIEventSourceAdapter openAIEventSourceAdapter = OpenAIEventSourceHolder.get(requestId);
        if(openAIEventSourceAdapter != null) {
            PollDataDTO pollDataDTO = openAIEventSourceAdapter.pollText();
            if(pollDataDTO.isFinished()) {
                OpenAIEventSourceHolder.remove(requestId);
            }
            return Result.success(pollDataDTO);
        }
        return Result.fail("requestId not exists or finished.");
    }

    /**
     * 通用写作文本生成
     * @return
     */
    @PostMapping("generateCommon")
    public Result<String> generateCommon(@Valid @RequestBody CommonGenerateReq req) {
        User user = userService.getById(req.getUserId());
        if(user.getRemainTimes() <= 0) {
            return Result.fail("剩余次数已用完");
        }
        if(req.getChatType() != null) {
            Chat chat = chatService.loadOrNewChat(req.getUserId(), req.getChatType());
            OpenAIEventSourceAdapter openAIEventSourceAdapter = new OpenAIEventSourceAdapter(true);
            openAIEventSourceAdapter.setOnFinishedCallback(responseText -> {
                chatMessageService.appendMessage(req.getUserId(), chat.getId(), ChatMessageBuilder.ROLE_ASSISTANT, responseText);
            });
            openAIEventSourceAdapter.setErrorCallback(throwable -> {
                //出错时加回去次数
                userService.rewardRemainTimes(req.getUserId(), 1);
            });
            ChatMessageBuilder builder = ChatMessageBuilder.builder();
            builder.appendMessage(ChatMessageBuilder.ROLE_USER, req.getPrompt());
            openAIClient.newChatStreamRequest(builder.build(), req.getUnionId(), openAIEventSourceAdapter);
            chatMessageService.appendMessage(req.getUserId(), chat.getId(), ChatMessageBuilder.ROLE_USER, req.getPrompt());
            String requestId = OpenAIEventSourceHolder.register(openAIEventSourceAdapter);
            userService.decreaseRemainTimes(req.getUserId(), 1);
            return Result.success(requestId);
        }else {
            //兼容老版本
            OpenAIEventSourceAdapter openAIEventSourceAdapter = new OpenAIEventSourceAdapter(true);
            ChatMessageBuilder builder = ChatMessageBuilder.builder();
            builder.appendMessage(ChatMessageBuilder.ROLE_USER, req.getPrompt());
            openAIClient.newChatStreamRequest(builder.build(), req.getUnionId(), openAIEventSourceAdapter);
            String requestId = OpenAIEventSourceHolder.register(openAIEventSourceAdapter);
            userService.decreaseRemainTimes(req.getUserId(), 1);
            return Result.success(requestId);
        }
    }

    /**
     * chat
     * @return
     */
    @PostMapping("newChat")
    public Result<Long> newChat(@Valid @RequestBody NewChatReq req) {
        Chat chat = new Chat();
        chat.setChatType(req.getChatType());
        chat.setUserId(req.getUserId());
        chat.setCreateTime(new Date());
        chat.setUpdateTime(new Date());
        chatService.save(chat);
        return Result.success(chat.getId());
    }

    /**
     * 初始化一个聊天窗口
     * @return
     */
    @PostMapping("loadOrNewChat")
    public Result<Map> loadOrNewChat(@Valid @RequestBody NewChatReq req) {
        Chat chat = chatService.loadOrNewChat(req.getUserId(), req.getChatType());
        Map<String, Object> response = Maps.newHashMap();
        List<ChatMessage> chatMessageList = Lists.newArrayList();
        OpenAiConfig.ChatInitMessageDTO chatInitMessageDTO = openAiConfig.getChatInit().get(req.getChatType());
        if(chatInitMessageDTO != null) {
            chatMessageList.add(ChatMessage.builder().role(ChatMessageBuilder.ROLE_TIPS).content(chatInitMessageDTO.getTips()).build());
        }
        chatMessageList.addAll(chatMessageService.queryHistryMessageList(chat.getId()));
        response.put("historyMessageList", chatMessageList);
        response.put("chatId", chat.getId());
        return Result.success(response);
    }


    /**
     * 聊天
     * @return
     */
    @PostMapping("chat")
    public Result<String> chat(@Valid @RequestBody ChatReq req) {

        User user = userService.getById(req.getUserId());
        if(user.getRemainTimes() <= 0) {
            return Result.fail("次数已用完");
        }

        OpenAIEventSourceAdapter openAIEventSourceAdapter = new OpenAIEventSourceAdapter(true);
        openAIEventSourceAdapter.setOnFinishedCallback(responseText -> {
            chatMessageService.appendMessage(req.getUserId(), req.getChatId(), ChatMessageBuilder.ROLE_ASSISTANT, responseText);
        });
        openAIEventSourceAdapter.setErrorCallback(throwable -> {
            //出错是加回去次数
            userService.rewardRemainTimes(req.getUserId(), 1);
        });

        Chat chat = chatService.getById(req.getChatId());
        if(chat == null || !req.getUserId().equals(chat.getUserId())) {
            return Result.fail("chat not exists");
        }

        List<ChatMessage> messageList = Lists.newArrayList();
        OpenAiConfig.ChatInitMessageDTO chatInitMessageDTO = openAiConfig.getChatInit().get(chat.getChatType());
        if(chatInitMessageDTO != null) {
            messageList.add(ChatMessage.builder().role(ChatMessageBuilder.ROLE_SYSTEM).content(chatInitMessageDTO.getTips()).build());
        }
        messageList.addAll(chatMessageService.queryBeforeMessageList(req.getChatId(), 2));
        messageList.add(chatMessageService.appendMessage(req.getUserId(), req.getChatId(), ChatMessageBuilder.ROLE_USER, req.getUserMessage()));
        openAIClient.newChatStreamRequest(ChatMessageBuilder.builder().fromMessageList(messageList).build(), req.getUnionId(), openAIEventSourceAdapter);
        userService.decreaseRemainTimes(req.getUserId(), 1);

        String requestId = OpenAIEventSourceHolder.register(openAIEventSourceAdapter);
        return Result.success(requestId);
    }

}