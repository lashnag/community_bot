package ru.lashnev.community.bot.utils

import com.pengrad.telegrambot.model.Update
import java.util.Optional

fun Update.getUserId(): Long {
    return if (this.message() != null) {
        return this.message().from().id()
    } else {
        this.callbackQuery().from().id()
    }
}

fun Update.getChatId(): Long {
    return if (this.message() != null) {
        return this.message().chat().id()
    } else {
        this.callbackQuery().message().chat().id()
    }
}

fun Update.getChosenEvent(command: String): Optional<Long> {
    return if (this.callbackQuery() != null) {
        Optional.of(this.callbackQuery().data().replace(command, "").toLong())
    } else {
        Optional.empty()
    }
}
