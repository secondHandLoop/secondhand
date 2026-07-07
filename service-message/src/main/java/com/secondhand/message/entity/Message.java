package com.secondhand.message.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "messages")
@CompoundIndexes({
        // Conversation history + inbox "last message" lookups.
        @CompoundIndex(name = "conv_created", def = "{'conversationId': 1, 'createAt': -1}"),
        // Inbox scan for either side of a conversation.
        @CompoundIndex(name = "sender_created", def = "{'senderId': 1, 'createAt': -1}"),
        @CompoundIndex(name = "receiver_created", def = "{'receiverId': 1, 'createAt': -1}")
})
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
