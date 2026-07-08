package com.secondhand.message.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.message.entity.Message;
import com.secondhand.message.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Hides messages per user by appending that user to the deleted set.
 */
@Service
public class MessageDeleteService {

    private final MessageRepository messageRepository;

    public MessageDeleteService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Marks a message as deleted for the current participant only.
     */
    public ApiResponse<Void> deleteMessage(Long currentUserId, String messageId) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        AssertUtil.notNullOrBlank(messageId, "消息ID不能为空");

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));

        boolean participant = currentUserId.equals(message.getSenderId()) || currentUserId.equals(message.getReceiverId());
        AssertUtil.isTrue(participant, "无权限删除该消息");

        if (message.getDeletedBy() == null) {
            message.setDeletedBy(new java.util.HashSet<>());
        }
        message.getDeletedBy().add(currentUserId);
        message.setUpdateAt(Instant.now().getEpochSecond());
        messageRepository.save(message);

        return ApiResponse.success();
    }
}
