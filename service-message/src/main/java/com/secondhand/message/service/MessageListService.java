package com.secondhand.message.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.message.dto.ApiResponse;
import com.secondhand.message.dto.MessageResponse;
import com.secondhand.message.dto.PageInfoResponse;
import com.secondhand.message.entity.Message;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads paginated messages for a two-user conversation.
 */
@Service
public class MessageListService {

    private final MongoTemplate mongoTemplate;

    public MessageListService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Returns one page of visible messages exchanged with another user.
     */
    public ApiResponse<List<MessageResponse>> listMessages(Long currentUserId, Long otherUserId, Integer page, Integer limit) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        AssertUtil.notNull(otherUserId, "会话对象不能为空");
        AssertUtil.isFalse(currentUserId.equals(otherUserId), "不能查询与自己的会话");

        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (limit == null || limit < 1) ? 20 : limit;
        String conversationId = MessageAddService.conversationId(currentUserId, otherUserId);

        Criteria criteria = Criteria.where("conversationId").is(conversationId)
                .and("deletedBy").ne(currentUserId);

        Query countQuery = new Query(criteria);
        long total = mongoTemplate.count(countQuery, Message.class);

        Query listQuery = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createAt"))
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize);

        List<Message> messages = mongoTemplate.find(listQuery, Message.class);
        List<MessageResponse> body = new ArrayList<>();
        for (Message message : messages) {
            body.add(MessageResponse.from(message));
        }

        PageInfoResponse pageInfo = new PageInfoResponse();
        pageInfo.setPage(pageNo);
        pageInfo.setLimit(pageSize);
        pageInfo.setTotal(total);

        return ApiResponse.success(body, pageInfo);
    }
}
