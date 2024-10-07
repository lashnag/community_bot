package ru.lashnev.community.bot.models

data class ReplacedParticipant(
    val user: User,
    val replaceUser: User? = null,
)
