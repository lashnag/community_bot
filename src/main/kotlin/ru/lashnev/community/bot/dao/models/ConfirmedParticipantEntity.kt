package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "confirmed_participant")
data class ConfirmedParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val confirmedParticipantId: Long? = null,
    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,
)
