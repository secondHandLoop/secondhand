package com.secondhand.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of one direct-message conversation.")
public class ConversationResponse {
    @Schema(description = "Stable conversation id shared by both participants.", example = "12_34")
    private String conversationId;
    @Schema(description = "User id of the other participant.", example = "34")
    private Long otherUserId;
    @Schema(description = "Id of the latest visible message.", example = "6610f21b7f8d1c2a99c8d123")
    private String lastMessageId;
    @Schema(description = "Content of the latest visible message.", example = "Is this still available?")
    private String lastMessage;
    @Schema(description = "Sender id of the latest message.", example = "12")
    private Long lastSenderId;
    @Schema(description = "Receiver id of the latest message.", example = "34")
    private Long lastReceiverId;
    @Schema(description = "Unix timestamp in seconds when the conversation was created.", example = "1711988800")
    private Long createAt;
    @Schema(description = "Unix timestamp in seconds when the latest message was updated.", example = "1711992400")
    private Long updateAt;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }
    public String getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(String lastMessageId) { this.lastMessageId = lastMessageId; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Long getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(Long lastSenderId) { this.lastSenderId = lastSenderId; }
    public Long getLastReceiverId() { return lastReceiverId; }
    public void setLastReceiverId(Long lastReceiverId) { this.lastReceiverId = lastReceiverId; }
    public Long getCreateAt() { return createAt; }
    public void setCreateAt(Long createAt) { this.createAt = createAt; }
    public Long getUpdateAt() { return updateAt; }
    public void setUpdateAt(Long updateAt) { this.updateAt = updateAt; }
}
