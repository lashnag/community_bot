package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "replaced_participant")
data class ReplacedParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val replacementParticipantId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne
    @JoinColumn(name = "replace_user_id")
    val replaceUser: UserEntity? = null,
)
