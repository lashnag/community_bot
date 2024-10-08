package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "participant")
data class ParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val participantId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,
)
