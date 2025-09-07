package org.competition.stepbot.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;
import static org.competition.stepbot.Values.JSON_MAPPER;

@Slf4j
@Service
public class TelegramBot {
    private final WebClient webClient = WebClient.create("https://api.telegram.org");

    @Value("${bot.token}")
    private String botToken;

    /**
     * <p>Обрабатывает ответ <i>Telegram</i>'а после отправки сообщения.</p>
     *
     * @param response ответ, полученный от <i>Telegram</i>'а
     * @return объект класса {@link MessageInfo}, содержащий информацию об отправленном сообщении
     * @throws RuntimeException если ответ <i>Telegram</i>'а сообщает об ошибки (то есть если отправленное сообщение
     * не было успешно доставлено пользователю)
     */
    private Mono<MessageInfo> responseHandler(ClientResponse response) {
        log.debug("Response handler has started");
        return response.bodyToMono(String.class).flatMap(body -> {
            try {
                // Проверка кода ответа
                if (!response.statusCode().is2xxSuccessful()) {
                    log.error("Telegram response code is {}", response.statusCode());
                    return Mono.error(new RuntimeException("Telegram HTTP error: " + body));
                }

                // Проверка успешности доставки сообщения
                JsonNode jsonRoot = JSON_MAPPER.readTree(body);
                if (!jsonRoot.path("ok").asBoolean()) {
                    log.error("Telegram response is false");
                    return Mono.error(new RuntimeException("Telegram error: " + jsonRoot.path("description").asText()));
                }

                // Извлечение из ответа информации об отправленном сообщении
                JsonNode result = jsonRoot.path("result");
                MessageInfo messageInfo = new MessageInfo(
                        result.path("text").asText(),
                        result.path("chat").path("id").asLong(),
                        result.path("message_id").asLong());
                log.info("Telegram response is success: {}", messageInfo);
                return Mono.just(messageInfo);

            } catch (Exception e) { // Если структура ответа не соответствует ожидаемой
                log.error("Unknown response format: {}", body);
                return Mono.error(new RuntimeException("Failed to parse response", e));
            }
        });
    }

    /**
     * <p>Метод для отправки сообщения пользователю.</p>
     *
     * @param chatId id пользователя в <i>Telegram</i>'е, которому нужно отправить сообщение
     * @param text текст сообщения для отправки
     * @return информацию об успешности доставки сообщения пользователю
     */
    public Mono<MessageInfo> sendMessage(long chatId, String text) {
        log.debug("Start to send a message '{}' to user {}", text, chatId);
        return webClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("chat_id", chatId, "text", text))
                .exchangeToMono(this::responseHandler);
    }
}
