package com.secondhand.message.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    private Long senderId;

    private Long receiverId;

    private String conversationId;

    private String content;

    @Builder.Default
    private long createAt = Instant.now().getEpochSecond();

    @Builder.Default
    private long updateAt = Instant.now().getEpochSecond();

    @Builder.Default
    private Set<Long> deletedBy = new HashSet<>();
}
