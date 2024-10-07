package ru.lashnev.community.bot.dao.hibernate

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.models.CommunityEntity

@Repository
interface CommunityRepository : JpaRepository<CommunityEntity, Long> {
    fun findByAdminsUserId(userId: Long): Set<CommunityEntity>
    fun findByChatId(chatId: String): CommunityEntity?
}