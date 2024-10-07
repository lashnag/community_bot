package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long? = null,
    val telegramId: Long,
    val telegramUsername: String?,
    val firstName: String?,
    val lastName: String?,
    val isSuperuser: Boolean = false,

    @ManyToMany(mappedBy = "admins")
    val communities: Set<CommunityEntity> = emptySet()
)
