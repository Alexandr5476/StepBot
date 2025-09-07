package org.competition.stepbot.telegram.exceptions;

import lombok.Getter;

@Getter
public class UserError extends RuntimeException {
    private final long chatId;
    private final String userMessage;

    public UserError(long chatId, String message, String userMessage) {
        super(message);
        this.chatId = chatId;
        this.userMessage = userMessage;
    }

    public UserError(long chatId, ErrorStrBox errorStrBox) {
        super(errorStrBox.excMessage());
        this.chatId = chatId;
        this.userMessage = errorStrBox.userMessage();
    }
}
