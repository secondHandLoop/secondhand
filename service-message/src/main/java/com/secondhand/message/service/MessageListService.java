package com.secondhand.message.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
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
@Document(desc = "消息列表", module = "message", name = "消息列表", author = "Jeffrey",
        platform = {Platform.APP, Platform.MANAGE, Platform.APPLETS}, creatAt = "2026-03-18",
        request = {
                @ApiParam(name = "otherUserId", value = "会话对方用户ID", dataType = "Long"),
                @ApiParam(name = "page", value = "页码", dataType = "Integer"),
                @ApiParam(name = "limit", value = "每页条数", dataType = "Integer")
        })
public class MessageListService extends ApiResponseHelper {

    private final MongoTemplate mongoTemplate;

    public MessageListService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> service(Long currentUserId, Long otherUserId, Integer page, Integer limit) {
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
        List<Map<String, Object>> body = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", message.getId());
            item.put("senderId", message.getSenderId());
            item.put("receiverId", message.getReceiverId());
            item.put("content", message.getContent());
            item.put("createAt", message.getCreateAt());
            body.add(item);
        }

        Map<String, Object> pageInfo = new LinkedHashMap<>();
        pageInfo.put("page", pageNo);
        pageInfo.put("limit", pageSize);
        pageInfo.put("total", total);

        return success(body, pageInfo);
    }
}
