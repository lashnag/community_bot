package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event")
data class EventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val eventId: Long? = null,
    val eventDescription: String,
    val pollId: String? = null,
    val pollConfirmationId: String? = null,
    val eventDate: LocalDateTime,
    val pollDate: LocalDateTime,
    val pollConfirmationDate: LocalDateTime?,
    val notificationDate: LocalDateTime,
    val notificationWasSent: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "community_id")
    val community: CommunityEntity,
)
