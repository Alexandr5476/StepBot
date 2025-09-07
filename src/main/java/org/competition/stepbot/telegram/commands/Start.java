package org.competition.stepbot.telegram.commands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.competition.stepbot.telegram.MessageInfo;
import org.competition.stepbot.telegram.TelegramBot;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static org.competition.stepbot.Values.TG_COMMAND_PREFIX;

@Slf4j
@Component
@AllArgsConstructor
public class Start implements Command{
    private final TelegramBot telegramBot;

    @Override
    public Mono<CommandResult> run(long chatId) {
        log.debug("Command {} is running", this);
        Mono<MessageInfo> message = telegramBot.sendMessage(chatId, "Привет! Это проверка команд.");
        return Mono.just(CommandResult.finish(this, message));
    }

    @Override
    public String toString() {
        return TG_COMMAND_PREFIX + "start";
    }
}
