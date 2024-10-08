package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "interested_participant")
data class InterestedParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val interestedParticipantId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,
)
