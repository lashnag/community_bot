package ru.lashnev.community.bot.dao

import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

interface ParticipantDao {
    fun addParticipant(user: User, event: Event)
}
