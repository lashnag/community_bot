package ru.lashnev.community.bot.utils

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message

fun Message.isGroupMessage(): Boolean {
    return this.chat().type() == Chat.Type.group || this.chat().type() == Chat.Type.supergroup
}
