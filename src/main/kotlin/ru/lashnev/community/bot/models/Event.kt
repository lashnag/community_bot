package ru.lashnev.community.bot.models

import java.lang.StringBuilder
import java.time.LocalDateTime

data class Event(
    val eventId: Long? = null,
    val eventDescription: String,
    val pollId: String? = null,
    val pollConfirmationId: String? = null,
    val community: Community,
    val interestedParticipants: Set<User> = emptySet(),
    val confirmedParticipants: Set<User> = emptySet(),
    val participants: Set<User> = emptySet(),
    val reserveParticipants: Set<User> = emptySet(),
    val replacedParticipants: Set<ReplacedParticipant> = emptySet(),
    val eventDate: LocalDateTime,
    val pollDate: LocalDateTime,
    val pollConfirmationDate: LocalDateTime?,
    val notificationDate: LocalDateTime,
    val notificationWasSent: Boolean = false,
) {
    fun toTelegramBotMessage(): String {
        val sb = StringBuilder()
        sb.appendLine("Название сообщества: ${community.name}")
        sb.appendLine("Описание мероприятия #$eventId: $eventDescription")
        sb.appendLine("Дата проведения мероприятия: $eventDate")
        sb.appendLine("Дата проведения опроса желающих участвовать: $pollDate")
        if (pollConfirmationDate != null) sb.appendLine("Дата подтверждения участия: $pollConfirmationDate")
        sb.appendLine("Дата публикации итогового списка участвующих: $notificationDate")

        return sb.toString().trimIndent()
    }
}
