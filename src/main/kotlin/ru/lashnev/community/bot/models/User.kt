package ru.lashnev.community.bot.models

data class User(
    val userId: Long? = null,
    val telegramId: Long,
    val telegramUsername: String?,
    val firstName: String?,
    val lastName: String?,
    val isSuperuser: Boolean = false,
)
