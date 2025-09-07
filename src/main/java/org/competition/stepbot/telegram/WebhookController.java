package org.competition.stepbot.telegram;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.competition.stepbot.telegram.dto.Update;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
public class WebhookController {
    private final MessageHandler messageHandler;

    /**
     * <p>Endpoint для приёма сообщений (или других обновлений), отправленных боту в <i>Telegram</i>'е.
     * Этот метод просто принимает обновление из <i>Telegram</i>'а, и вызывает метод
     * {@link MessageHandler#handleUpdate}, где аргументом будет это обновление.</p>
     * @param update обновление, отправленное боту в <i>Telegram</i>'е
     * @return
     */
    @PostMapping("/webhook")
    public Mono<Void> onUpdateReceived(@RequestBody Update update) {
        log.info("Update received: {}", update);
        return messageHandler.handleUpdate(update);
    }
}
