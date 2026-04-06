package com.secondhand.message.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.message.dto.ApiResponse;
import com.secondhand.message.dto.ConversationResponse;
import com.secondhand.message.dto.PageInfoResponse;
import com.secondhand.message.entity.Message;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Builds the conversation list shown in the messaging inbox.
 */
@Service
public class MessageConversationListService {

    private final MongoTemplate mongoTemplate;

    public MessageConversationListService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Returns the latest visible message for each conversation joined by the current user.
     */
    public ApiResponse<List<ConversationResponse>> listConversations(Long currentUserId, Integer page, Integer limit) {
        AssertUtil.notNull(currentUserId, "用户未登录");

        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (limit == null || limit < 1) ? 20 : limit;

        Criteria participant = new Criteria().orOperator(
                Criteria.where("senderId").is(currentUserId),
                Criteria.where("receiverId").is(currentUserId)
        );
        Criteria visible = Criteria.where("deletedBy").ne(currentUserId);

        Query query = new Query(new Criteria().andOperator(participant, visible))
                .with(Sort.by(Sort.Direction.DESC, "updateAt"));

        List<Message> messages = mongoTemplate.find(query, Message.class);
        LinkedHashMap<String, ConversationResponse> conversations = new LinkedHashMap<>();

        for (Message message : messages) {
            if (conversations.containsKey(message.getConversationId())) {
                continue;
            }

            Long otherUserId = currentUserId.equals(message.getSenderId())
                    ? message.getReceiverId()
                    : message.getSenderId();

            ConversationResponse item = new ConversationResponse();
            item.setConversationId(message.getConversationId());
            item.setOtherUserId(otherUserId);
            item.setLastMessageId(message.getId());
            item.setLastMessage(message.getContent());
            item.setLastSenderId(message.getSenderId());
            item.setLastReceiverId(message.getReceiverId());
            item.setCreateAt(message.getCreateAt());
            item.setUpdateAt(message.getUpdateAt());
            conversations.put(message.getConversationId(), item);
        }

        List<ConversationResponse> all = new ArrayList<>(conversations.values());
        int total = all.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        PageInfoResponse pageInfo = new PageInfoResponse();
        pageInfo.setPage(pageNo);
        pageInfo.setLimit(pageSize);
        pageInfo.setTotal((long) total);

        return ApiResponse.success(all.subList(fromIndex, toIndex), pageInfo);
    }
}
