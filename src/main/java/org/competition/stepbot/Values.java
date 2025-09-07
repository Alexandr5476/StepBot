package org.competition.stepbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.competition.stepbot.telegram.exceptions.ErrorStrBox;


@UtilityClass
public class Values {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static final String TG_COMMAND_PREFIX = "/";
    public static final ErrorStrBox NO_COMMAND_BOX = new ErrorStrBox(
            "User sent message without command",
            "Ошибка: сообщение должно начинаться с команды"
    );
    public static final ErrorStrBox UNKNOWN_ERR = new ErrorStrBox(
            "Unknown error while creating InputMessage: ",
            "Возникла неизвестная ошибка при обработке вашего сообщения"
    );
    public static final ErrorStrBox NO_MSG_TEXT = new ErrorStrBox(
            "User sent null text",
            "Ошибка: не смог получить ваше сообщение. Проверьте его и повторите отправку"
    );
    public static final ErrorStrBox EMPTY_MSG_TEXT = new ErrorStrBox(
        "User sent empty text",
        "Ошибка: получил пустое сообщение. Проверьте его и повторите отправку"
    );
    public static final ErrorStrBox MSG_TEXT_TOO_LONG = new ErrorStrBox(
            "User sent too long text",
            "Ошибка: вы отправляете слишком длинное сообщение"
    );
    public static final ErrorStrBox TOO_MANY_COMMANDS = new ErrorStrBox(
            "User sent more than 1 command",
            "Ошибка: в сообщении не может быть больше двух команд"
    );
    public static final ErrorStrBox MSG_TEXT_DOES_NOT_START_WITH_COMMAND = new ErrorStrBox(
            "User sent a message that does not start with a command",
            "Ошибка: команда должна находиться в начале сообщения"
    );
}
