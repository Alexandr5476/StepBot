package org.competition.stepbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Update(
        @JsonProperty("update_id")
        long updateId,

        @JsonProperty("message")
        Message message) {
}
