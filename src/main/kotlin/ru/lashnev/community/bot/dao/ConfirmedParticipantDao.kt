package ru.lashnev.community.bot.dao

import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

interface ConfirmedParticipantDao {
    fun addParticipant(user: User, event: Event)
    fun removeParticipant(user: User, event: Event)
}
