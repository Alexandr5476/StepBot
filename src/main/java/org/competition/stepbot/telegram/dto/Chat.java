package org.competition.stepbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Chat(
        @JsonProperty("id")
        Long chatId
) {
}
