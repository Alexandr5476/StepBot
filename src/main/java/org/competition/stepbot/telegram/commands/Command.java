package org.competition.stepbot.telegram.commands;

import org.competition.stepbot.telegram.dto.Update;
import reactor.core.publisher.Mono;

import static org.competition.stepbot.Values.TG_COMMAND_PREFIX;

public interface Command {

    static boolean commandContained(Update update) {
        return update.message().text().startsWith(TG_COMMAND_PREFIX);
    }

    Mono<CommandResult> run (long chatId);
}
