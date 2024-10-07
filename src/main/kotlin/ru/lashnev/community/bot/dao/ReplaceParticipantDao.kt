package ru.lashnev.community.bot.dao

import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

interface ReplaceParticipantDao {
    fun replaceParticipant(event: Event, canceledUser: User, replaceUser: User?)
}
