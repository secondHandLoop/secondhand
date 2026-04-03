package com.secondhand.message.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.message.entity.Message;
import com.secondhand.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Document(desc = "发送消息", module = "message", name = "发送消息", author = "Jeffrey",
        platform = {Platform.APP, Platform.MANAGE, Platform.APPLETS}, creatAt = "2026-03-18",
        request = {
                @ApiParam(name = "receiverId", value = "接收方用户ID", dataType = "Long"),
                @ApiParam(name = "content", value = "消息内容", dataType = "String")
        },
        response = {
                @ApiParam(name = "id", value = "消息ID", dataType = "String"),
                @ApiParam(name = "conversationId", value = "会话ID", dataType = "String")
        })
public class MessageAddService extends ApiResponseHelper {

    private final MessageRepository messageRepository;

    public MessageAddService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Map<String, Object> service(Long senderId, Long receiverId, String content) {
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

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", message.getId());
        body.put("senderId", message.getSenderId());
        body.put("receiverId", message.getReceiverId());
        body.put("conversationId", message.getConversationId());
        body.put("content", message.getContent());
        body.put("createAt", message.getCreateAt());
        return success(body);
    }

    static String conversationId(Long a, Long b) {
        long left = Math.min(a, b);
        long right = Math.max(a, b);
        return left + "_" + right;
    }
}
