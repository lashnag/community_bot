package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "community")
data class CommunityEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val communityId: Long? = null,
    val chatId: String,
    val capacity: Int,
    val name: String,

    @ManyToMany
    @JoinTable(
        name = "community_admins",
        joinColumns = [JoinColumn(name = "community_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val admins: Set<UserEntity> = emptySet(),
)
