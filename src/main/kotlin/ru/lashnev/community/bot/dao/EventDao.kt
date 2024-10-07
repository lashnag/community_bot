package ru.lashnev.community.bot.dao

import ru.lashnev.community.bot.models.Event
import java.util.Optional

interface EventDao {
    fun getEventById(eventId: Long): Event?
    fun getEventsToPoll(): Set<Event>
    fun getEventsToConfirmationPoll(): Set<Event>
    fun getEventsToNotification(): Set<Event>

    fun getEventsByCommunityId(communityId: Long): Set<Event>
    fun getEventByPollId(pollId: String): Optional<Event>
    fun getEventByPollConfirmationId(confirmationPollId: String): Optional<Event>

    fun isPollIdExist(id: String): Boolean
    fun isPollConfirmationIdExist(id: String): Boolean

    fun save(event: Event)

    fun deleteById(eventId: Long)

    fun getPreviousEvent(event: Event): Event?
    fun getActiveEventsByUser(userId: Long): Set<Event>
    fun getActiveEventsByChatId(chatId: Long): Set<Event>
}
