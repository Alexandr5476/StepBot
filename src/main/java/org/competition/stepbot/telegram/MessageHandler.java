package org.competition.stepbot.telegram;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.competition.stepbot.Values;
import org.competition.stepbot.telegram.commands.Command;
import org.competition.stepbot.telegram.commands.CommandResult;
import org.competition.stepbot.telegram.dto.Update;
import org.competition.stepbot.telegram.exceptions.UserError;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@AllArgsConstructor
public class MessageHandler {
    private final Command[] commands;
    private final ConcurrentMap<Long, Command> currentCommand = new ConcurrentHashMap<>();
    private final TelegramBot telegramBot;

    private Mono<Void> applyResult(long chatId, CommandResult result) {
        return result.applyStateUpdate(currentCommand, chatId)
                .thenMany(Flux.fromIterable(result.messages()))
                .flatMap(msgMono -> msgMono
                                    //.doOnNext(msg -> log.info("Message sent: {}", msg))
                                    .doOnError(err -> log.error("Error sending message from command", err))
                )
                .then(); // Возвращает Mono<Void> на самый верх

    }

    private Mono<Void> handleError(Long chatId, Throwable error) {
        log.debug("Error handling: {}", error.getMessage());

        String message;
        if (error instanceof UserError e) {
            if (e.getUserMessage().isEmpty()) {
                return Mono.empty();
            } else {
                chatId = e.getChatId();
                message = e.getUserMessage();
            }
        } else if (chatId == null) {
            log.error("Unknown error! {}", error.getMessage());
            return Mono.empty();
        } else {
            message = "Неизвестная ошибка";
        }
        return telegramBot.sendMessage(chatId, message)
                .doOnError(err -> log.error("Error sending message from handleError", err))
                .then();
    }

    public Mono<Void> handleUpdate(Update update) {
        log.debug("Start message handler with update: {}", update);

        return Mono.defer(() -> {
            InputMessage inputMessage;
            try {
                inputMessage = InputMessage.of(update);
            } catch (Exception e) {
                return handleError(null, e);
            }
            long chatId = inputMessage.chatId();

            Mono<CommandResult> commandResult;
            if (currentCommand.containsKey(chatId)) {
                commandResult = currentCommand.get(chatId).run(chatId);
            } else {
                commandResult = Flux.fromArray(commands)
                    .filter(cmd -> cmd.toString().equals(inputMessage.command()))
                    .next() // первая найденная команда
                    .flatMap(cmd -> {
                        log.debug("Command {} is found", cmd);
                        return cmd.run(chatId);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.debug("No command found in message: {}", inputMessage.text());
                        return Mono.error(new UserError(chatId, Values.NO_COMMAND_BOX));
                    }));
            }
            return commandResult
                    .flatMap(result -> applyResult(chatId, result))
                    .onErrorResume(err -> handleError(chatId, err));
        });
    }
}