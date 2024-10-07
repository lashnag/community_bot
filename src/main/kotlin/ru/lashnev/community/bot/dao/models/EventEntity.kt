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

    @OneToMany
    @JoinTable(
        name = "interested_participants",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    val interestedParticipants: Set<UserEntity> = emptySet(),
    @OneToMany
    @JoinTable(
        name = "confirmed_participants",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    val confirmedParticipants: Set<UserEntity> = emptySet(),
    @OneToMany
    @JoinTable(
        name = "participants",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    val participants: Set<UserEntity> = emptySet(),
    @OneToMany
    @JoinTable(
        name = "reserve_participants",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    val reserveParticipants: Set<UserEntity> = emptySet(),
    @OneToMany
    @JoinTable(
        name = "replace_participants",
        joinColumns = [JoinColumn(name = "event_id")],
    )
    val replacedParticipants: Set<ReplacedParticipantEntity> = emptySet(),
)
