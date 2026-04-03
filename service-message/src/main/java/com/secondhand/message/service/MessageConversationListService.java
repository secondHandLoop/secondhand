package com.secondhand.message.service;

import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.message.entity.Message;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageConversationListService extends ApiResponseHelper {

    private final MongoTemplate mongoTemplate;

    public MessageConversationListService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> service(Long currentUserId, Integer page, Integer limit) {
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
        LinkedHashMap<String, Map<String, Object>> conversations = new LinkedHashMap<>();

        for (Message message : messages) {
            if (conversations.containsKey(message.getConversationId())) {
                continue;
            }

            Long otherUserId = currentUserId.equals(message.getSenderId())
                    ? message.getReceiverId()
                    : message.getSenderId();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("conversationId", message.getConversationId());
            item.put("otherUserId", otherUserId);
            item.put("lastMessageId", message.getId());
            item.put("lastMessage", message.getContent());
            item.put("lastSenderId", message.getSenderId());
            item.put("lastReceiverId", message.getReceiverId());
            item.put("createAt", message.getCreateAt());
            item.put("updateAt", message.getUpdateAt());
            conversations.put(message.getConversationId(), item);
        }

        List<Map<String, Object>> all = new ArrayList<>(conversations.values());
        int total = all.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        Map<String, Object> pageInfo = new LinkedHashMap<>();
        pageInfo.put("page", pageNo);
        pageInfo.put("limit", pageSize);
        pageInfo.put("total", total);

        return success(all.subList(fromIndex, toIndex), pageInfo);
    }
}
