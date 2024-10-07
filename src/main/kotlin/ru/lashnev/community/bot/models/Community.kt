package ru.lashnev.community.bot.models

data class Community(
    val communityId: Long? = null,
    val chatId: String,
    val capacity: Int,
    val name: String,
    val admins: Set<User> = emptySet(),
)
