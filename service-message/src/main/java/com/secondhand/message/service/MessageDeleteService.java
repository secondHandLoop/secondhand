package com.secondhand.message.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.message.entity.Message;
import com.secondhand.message.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Document(desc = "删除消息", module = "message", name = "删除消息", author = "Jeffrey",
        platform = {Platform.APP, Platform.MANAGE, Platform.APPLETS}, creatAt = "2026-03-18",
        request = {
                @ApiParam(name = "messageId", value = "消息ID", dataType = "String")
        })
public class MessageDeleteService extends ApiResponseHelper {

    private final MessageRepository messageRepository;

    public MessageDeleteService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Map<String, Object> service(Long currentUserId, String messageId) {
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

        return success();
    }
}
