package org.competition.stepbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Message(
        @JsonProperty("message_id")
        long messageId,

        @JsonProperty("text")
        String text,

        @JsonProperty("chat")
        Chat chat,

        @JsonProperty("entities")
        List<MessageEntity> entities
) {
}
