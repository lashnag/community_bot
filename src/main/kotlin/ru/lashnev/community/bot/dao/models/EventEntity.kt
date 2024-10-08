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

    @OneToMany(mappedBy = "event")
    val interestedParticipants: Set<InterestedParticipantEntity> = emptySet(),
    @OneToMany(mappedBy = "event")
    val confirmedParticipants: Set<ConfirmedParticipantEntity> = emptySet(),
    @OneToMany(mappedBy = "event")
    val participants: Set<ParticipantEntity> = emptySet(),
    @OneToMany(mappedBy = "event")
    val reservedParticipants: Set<ReservedParticipantEntity> = emptySet(),
    @OneToMany(mappedBy = "event")
    val replacedParticipants: Set<ReplacedParticipantEntity> = emptySet(),
)
