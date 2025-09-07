package org.competition.stepbot.telegram;

import lombok.extern.slf4j.Slf4j;
import org.competition.stepbot.Values;
import org.competition.stepbot.telegram.dto.MessageEntity;
import org.competition.stepbot.telegram.dto.Update;
import org.competition.stepbot.telegram.exceptions.UserError;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public record InputMessage(long chatId, String text, String command, List<String> userMentions) {

    /**
     * <p>Проверяет текст, команды, упоминания людей и вообще корректность объекта {@link Update},
     * который приходит из телеграма, когда пользователь отправляет что-то боту.
     * Файлы, или какие-то другие вещи, прикреплённые к сообщению не поддерживаются.</p>
     *
     * @param update обновление из телеграма, из которого нужно создать {@link InputMessage}
     * @return созданный (проверенный) {@link InputMessage}
     */
    static public InputMessage of(Update update) {
        log.debug("Creating new InputMessage from update: {}", update);

        validateUpdate(update);

        long chatId = update.message().chat().chatId();
        String text = normalizeText(update.message().text(), chatId);

        ParsedEntities parsedEntities = parseEntities(update.message().entities(), update.message().text(), text, chatId);

        return new InputMessage(chatId, parsedEntities.cleanedText, parsedEntities.command, parsedEntities.mentions);
    }

    /**
     *  <p>Проверяет, что нужные компоненты update корректны (сообщение или чат не null, и
     *  отправленный текст не слишком длинный).</p>
     *
     * @param update обновление, которое пришло из телеграма
     * @throws NullPointerException если невозможно получить id чата, из которого отправлено сообщение
     * @throws UserError если текст null или слишком длинный
     */
    private static void validateUpdate(Update update) {
        if (update == null || update.message() == null ||
                update.message().chat() == null || update.message().chat().chatId() == null) {
            log.error("Unknown critically error with update: {}", update);
            throw new NullPointerException("Get null while creating InputMessage");
        }
        long chatId = update.message().chat().chatId();
        if (update.message().text() == null) {
            log.error("Message is not presented");
            throw new UserError(chatId, Values.NO_MSG_TEXT);
        }
        if (update.message().text().length() > 4096) {
            log.error("Message text is too long");
            throw new UserError(chatId, Values.MSG_TEXT_TOO_LONG);
        }
    }

    /**
     * <p>Убирает из текста управляющие / невидимые символы и проверят, что текст не пустой (или не содержит
     * только пробелы или другие пустые символы). Потом убирает пробелы по концам и заменяет пробелы (и любые пробельные
     * символы), которых идёт больше двух подряд, на один обычный пробел.</p>
     *
     * @param text текст, который нужно обработать
     * @param chatId  id чата, из которого пришло сообщение (нужен только, чтобы бросить {@link UserError})
     * @return обработанный текст
     * @throws UserError если текст пустой (или содержит только пробельные / невидимые / управляющие) символы
     */
    private static String normalizeText(String text, long chatId) {
        text = text.replaceAll("\\p{C}", "");
        if (text.isBlank()) {
            log.error("Message text is empty");
            throw new UserError(chatId, Values.NO_MSG_TEXT);
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    private record ParsedEntities(String cleanedText, String command, List<String> mentions) {
    }

    /**
     * <p>Вынимает из текста команды и упоминания людей (с помощью {@link MessageEntity}). Проверяет, что если в тексте
     * есть команда, то она одна и лежит в самом начале текста, и если так, то убирает её из cleanedText</p>
     *
     * @param entities список всех объектов, которые содержаться в тексте
     * @param originalText текст из сообщения, который никак не обработан
     * @param cleanedText текст из сообщения, в который проверен и обработан методом {@link #normalizeText}
     * @param chatId id чата, из которого пришло сообщение (нужен только, чтобы бросить {@link UserError})
     * @return текст сообщения (который теперь без команды, если она была в нём), команду, список упоминаний людей
     * @throws UserError если в тексте больше одной команды или в тексте есть команда и она не в начале сообщения
     */
    private static ParsedEntities parseEntities(List<MessageEntity> entities, String originalText, String cleanedText, long chatId) {
        String command = "";
        List<String> mentions = new ArrayList<>(List.of());

        if (entities == null || entities.isEmpty()) {
            return new ParsedEntities(cleanedText, command, mentions);
        }

        if (entities.stream().filter(entity -> "bot_command".equals(entity.type())).count() > 1) {
            log.error("Message text contains more than 1 command");
            throw new UserError(chatId, Values.TOO_MANY_COMMANDS);
        }

        for (var entity : entities) {
            String strEntity = originalText.substring(entity.offset(), entity.offset() + entity.length());
            if ("mention".equals(entity.type())) {
                mentions.add(strEntity);
            } else if ("bot_command".equals(entity.type())) {
                command = strEntity;
                if (!cleanedText.startsWith(Values.TG_COMMAND_PREFIX)) {
                    throw new UserError(chatId, Values.MSG_TEXT_DOES_NOT_START_WITH_COMMAND);
                }
                cleanedText = cleanedText.substring(entity.length());
            }
        }
        return new ParsedEntities(cleanedText, command, mentions);
    }


    /// Проверка: была ли команда в сообщении
    public boolean containsCommand() {
        return !"".equals(command);
    }

    /// Проверка: состоял ли текст только из команды
    public boolean isOnlyCommand() {
        return "".equals(text) && containsCommand();
    }

    /// Проверка: были ли в тексте упоминания людей
    public boolean containsUserMentions() {
        return !userMentions.isEmpty();
    }
}