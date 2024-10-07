package ru.lashnev.community.bot.dao.hibernate

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.models.UserEntity

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByTelegramId(telegramId: Long): UserEntity?
    fun getByTelegramUsername(telegramUsername: String): UserEntity?
    fun findByCommunitiesChatId(chatId: String): Set<UserEntity>
}