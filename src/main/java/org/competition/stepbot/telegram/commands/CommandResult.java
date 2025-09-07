package org.competition.stepbot.telegram.commands;

import org.competition.stepbot.telegram.MessageInfo;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public sealed interface CommandResult permits CommandResult.NextStep, CommandResult.Finish {

    default Mono<Void> applyStateUpdate(ConcurrentMap<Long, Command> currentCommand, long chatId) {
        if (this instanceof NextStep) {
            return Mono.fromRunnable(() -> currentCommand.put(chatId, currentCommand()));
        } else if (this instanceof Finish) {
            return Mono.fromRunnable(() -> currentCommand.remove(chatId));
        }
        return Mono.empty();
    }


        List<Mono<MessageInfo>> messages();
        Command currentCommand();

        record NextStep(Command currentCommand, List<Mono<MessageInfo>> messages) implements CommandResult { }

        record Finish(Command currentCommand, List<Mono<MessageInfo>> messages) implements CommandResult { }

        @SafeVarargs
        static CommandResult nextStep(Command currentCommand, Mono<MessageInfo>... messages) {
            return new NextStep(currentCommand, List.of(messages));
        }

        @SafeVarargs
        static CommandResult finish(Command currentCommand, Mono<MessageInfo>... messages) {
            return new Finish(currentCommand, List.of(messages));
        }
}
