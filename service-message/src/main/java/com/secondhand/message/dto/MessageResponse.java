package com.secondhand.message.dto;

import com.secondhand.message.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Message payload returned by message endpoints.")
public class MessageResponse {
    @Schema(description = "MongoDB message id.", example = "6610f21b7f8d1c2a99c8d123")
    private String id;
    @Schema(description = "Sender user id.", example = "12")
    private Long senderId;
    @Schema(description = "Receiver user id.", example = "34")
    private Long receiverId;
    @Schema(description = "Stable conversation id shared by both users.", example = "12_34")
    private String conversationId;
    @Schema(description = "Plain-text message content.", example = "Hi, is this item still available?")
    private String content;
    @Schema(description = "Unix timestamp in seconds when the message was created.", example = "1711992400")
    private Long createAt;

    /**
     * Builds a response from a persisted message document.
     *
     * @param message the source message
     * @return the populated response
     */
    public static MessageResponse from(Message message) {
        MessageResponse item = new MessageResponse();
        item.setId(message.getId());
        item.setSenderId(message.getSenderId());
        item.setReceiverId(message.getReceiverId());
        item.setConversationId(message.getConversationId());
        item.setContent(message.getContent());
        item.setCreateAt(message.getCreateAt());
        return item;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCreateAt() { return createAt; }
    public void setCreateAt(Long createAt) { this.createAt = createAt; }
}
