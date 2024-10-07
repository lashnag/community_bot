package ru.lashnev.community.bot.dao.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.CommunityDao
import ru.lashnev.community.bot.dao.hibernate.CommunityRepository
import ru.lashnev.community.bot.mappers.CommunityMapper
import ru.lashnev.community.bot.models.Community
import ru.lashnev.community.bot.models.User

@Repository
class CommunityDaoImpl(
    private val communityRepository: CommunityRepository,
    private val communityMapper: CommunityMapper,
) : CommunityDao {
    override fun findById(communityId: Long): Community {
        return communityMapper.toBO(communityRepository.findByIdOrNull(communityId) ?: throw IllegalStateException())
    }

    override fun getAllCommunitiesByAdmin(user: User): Set<Community> {
        return if (user.isSuperuser) {
            communityRepository.findAll().map { communityMapper.toBO(it) }.toSet()
        } else {
            communityRepository.findByAdminsUserId(user.userId!!).map { communityMapper.toBO(it) }.toSet()
        }
    }

    override fun save(community: Community) {
        if (alreadyCreated(community.chatId)) {
            return
        }
        communityRepository.save(communityMapper.toEntity(community))
    }

    private fun alreadyCreated(chatId: String): Boolean {
        return communityRepository.findByChatId(chatId) != null
    }
}
