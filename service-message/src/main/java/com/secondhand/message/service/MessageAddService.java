package com.secondhand.message.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.message.dto.ApiResponse;
import com.secondhand.message.dto.MessageResponse;
import com.secondhand.message.entity.Message;
import com.secondhand.message.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Persists direct messages and normalizes the conversation identifier used by both users.
 */
@Service
public class MessageAddService {

    private final MessageRepository messageRepository;

    public MessageAddService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Creates a new message in the conversation between the sender and receiver.
     */
    public ApiResponse<MessageResponse> addMessage(Long senderId, Long receiverId, String content) {
        AssertUtil.notNull(senderId, "用户未登录");
        AssertUtil.notNull(receiverId, "接收方不能为空");
        AssertUtil.isFalse(senderId.equals(receiverId), "不能给自己发送消息");
        AssertUtil.notNullOrBlank(content, "消息内容不能为空");

        long now = Instant.now().getEpochSecond();
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .conversationId(conversationId(senderId, receiverId))
                .content(content.trim())
                .createAt(now)
                .updateAt(now)
                .build();

        messageRepository.save(message);

        MessageResponse body = new MessageResponse();
        body.setId(message.getId());
        body.setSenderId(message.getSenderId());
        body.setReceiverId(message.getReceiverId());
        body.setConversationId(message.getConversationId());
        body.setContent(message.getContent());
        body.setCreateAt(message.getCreateAt());
        return ApiResponse.success(body);
    }

    /**
     * Produces a stable two-user conversation id regardless of sender order.
     */
    static String conversationId(Long a, Long b) {
        long left = Math.min(a, b);
        long right = Math.max(a, b);
        return left + "_" + right;
    }
}
