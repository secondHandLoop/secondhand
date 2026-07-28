package com.secondhand.message.service;

import com.secondhand.message.entity.Message;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Marks all unread messages in a conversation as read for the current user.
 */
@Service
public class MessageMarkReadService {

    private final MongoTemplate mongoTemplate;

    public MessageMarkReadService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void markAsRead(Long currentUserId, Long otherUserId) {
        String conversationId = MessageAddService.conversationId(currentUserId, otherUserId);
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("conversationId").is(conversationId),
                Criteria.where("receiverId").is(currentUserId),
                Criteria.where("isRead").is(false)
        ));
        Update update = new Update().set("isRead", true);
        mongoTemplate.updateMulti(query, update, Message.class);
    }
}
