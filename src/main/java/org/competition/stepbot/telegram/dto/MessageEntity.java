package org.competition.stepbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageEntity(
        @JsonProperty("offset")
        int offset,

        @JsonProperty("length")
        int length,

        @JsonProperty("type")
        String type) {
}
