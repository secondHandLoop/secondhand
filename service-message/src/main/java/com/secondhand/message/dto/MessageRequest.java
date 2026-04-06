package com.secondhand.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload used to send a direct message.")
public class MessageRequest {
    @Schema(description = "Receiver's user id.", example = "34")
    private Long receiverId;
    @Schema(description = "Plain-text message content.", example = "Hi, is this item still available?")
    private String content;

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
