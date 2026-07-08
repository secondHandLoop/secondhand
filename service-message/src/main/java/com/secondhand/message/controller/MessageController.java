package com.secondhand.message.controller;

import com.secondhand.common.controller.RequestUserIdResolver;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.common.output.ApiErrorResponse;
import com.secondhand.message.dto.ConversationResponse;
import com.secondhand.message.dto.MessageRequest;
import com.secondhand.message.dto.MessageResponse;
import com.secondhand.message.service.MessageAddService;
import com.secondhand.message.service.MessageConversationListService;
import com.secondhand.message.service.MessageDeleteService;
import com.secondhand.message.service.MessageListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
@Tag(name = "Messages", description = "Direct messaging APIs for conversations and message history.")
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

    @Operation(
            summary = "Send a message",
            description = "Creates a new direct message from the authenticated user to another user and returns the stored message payload."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Message created successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The message request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/messages")
    public ApiResponse<MessageResponse> addMessage(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestBody MessageRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageAddService.addMessage(currentUserId, request.getReceiverId(), request.getContent());
    }

    @Operation(
            summary = "List conversations",
            description = "Returns the latest visible message for each conversation joined by the authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationResponse>> listConversations(@RequestHeader("Authorization") String authorizationHeader,
                                                                     @Parameter(description = "Page number starting from 1.") @RequestParam(required = false) Integer page,
                                                                     @Parameter(description = "Number of conversations per page.") @RequestParam(required = false) Integer limit) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageConversationListService.listConversations(currentUserId, page, limit);
    }

    @Operation(
            summary = "List messages in a conversation",
            description = "Returns a page of visible messages exchanged with the specified user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The conversation query is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/messages")
    public ApiResponse<List<MessageResponse>> listMessages(@RequestHeader("Authorization") String authorizationHeader,
                                                           @Parameter(description = "The other participant's user id.") @RequestParam Long otherUserId,
                                                           @Parameter(description = "Page number starting from 1.") @RequestParam(required = false) Integer page,
                                                           @Parameter(description = "Number of messages per page.") @RequestParam(required = false) Integer limit) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageListService.listMessages(currentUserId, otherUserId, page, limit);
    }

    @Operation(
            summary = "Delete a message for the current user",
            description = "Hides the specified message from the authenticated user's view without deleting it for the other participant."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Message hidden successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The message id is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Void> deleteMessage(@RequestHeader("Authorization") String authorizationHeader,
                                           @Parameter(description = "Message id stored in MongoDB.") @PathVariable String messageId) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return messageDeleteService.deleteMessage(currentUserId, messageId);
    }
}
