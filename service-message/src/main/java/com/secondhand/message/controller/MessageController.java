package com.secondhand.message.controller;

import com.secondhand.common.controller.RequestUserIdResolver;
import com.secondhand.message.service.MessageAddService;
import com.secondhand.message.service.MessageConversationListService;
import com.secondhand.message.service.MessageDeleteService;
import com.secondhand.message.service.MessageListService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final RequestUserIdResolver userIdResolver;
    private final MessageAddService messageAddService;
    private final MessageConversationListService messageConversationListService;
    private final MessageListService messageListService;
    private final MessageDeleteService messageDeleteService;

    public MessageController(RequestUserIdResolver userIdResolver,
                             MessageAddService messageAddService,
                             MessageConversationListService messageConversationListService,
                             MessageListService messageListService,
                             MessageDeleteService messageDeleteService) {
        this.userIdResolver = userIdResolver;
        this.messageAddService = messageAddService;
        this.messageConversationListService = messageConversationListService;
        this.messageListService = messageListService;
        this.messageDeleteService = messageDeleteService;
    }

    @PostMapping("/messages")
    public Map<String, Object> addMessage(@RequestHeader("Authorization") String authorizationHeader,
                                          @RequestBody MessageAddRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageAddService.service(currentUserId, request.getReceiverId(), request.getContent());
    }

    @GetMapping("/conversations")
    public Map<String, Object> listConversations(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer limit) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageConversationListService.service(currentUserId, page, limit);
    }

    @GetMapping("/messages")
    public Map<String, Object> listMessages(@RequestHeader("Authorization") String authorizationHeader,
                                            @RequestParam Long otherUserId,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer limit) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageListService.service(currentUserId, otherUserId, page, limit);
    }

    @DeleteMapping("/messages/{messageId}")
    public Map<String, Object> deleteMessage(@RequestHeader("Authorization") String authorizationHeader,
                                             @PathVariable String messageId) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageDeleteService.service(currentUserId, messageId);
    }

    public static class MessageAddRequest {
        private Long receiverId;
        private String content;

        public Long getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Long receiverId) {
            this.receiverId = receiverId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
