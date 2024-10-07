package ru.lashnev.community.bot.utils

import ru.lashnev.community.bot.models.User

fun Set<User>.toNumberedMentionedList(): String {
    return this.withIndex().joinToString(separator = "") {
        "\n" + it.index.plus(1).toString() + ". " + it.value.toMention()
    }
}

fun User.toMention(): String {
    return if (this.telegramUsername != null) {
        "@${this.telegramUsername}"
    } else {
        "<a href=\"tg://user?id=${this.telegramId}\">${this.firstName ?: ""} ${this.lastName ?: ""}</>"
    }
}
