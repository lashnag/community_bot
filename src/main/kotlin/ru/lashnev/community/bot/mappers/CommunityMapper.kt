package ru.lashnev.community.bot.mappers

import org.springframework.stereotype.Component
import ru.lashnev.community.bot.dao.models.CommunityEntity
import ru.lashnev.community.bot.models.Community

@Component
class CommunityMapper(private val userMapper: UserMapper) {
    fun toEntity(community: Community): CommunityEntity {
        return CommunityEntity(
            communityId = community.communityId,
            name = community.name,
            chatId = community.chatId,
            capacity = community.capacity,
            admins = community.admins.map { userMapper.toEntity(it) }.toSet()
        )
    }

    fun toBO(communityEntity: CommunityEntity): Community {
        return Community(
            communityId = communityEntity.communityId,
            name = communityEntity.name,
            chatId = communityEntity.chatId,
            capacity = communityEntity.capacity,
            admins = communityEntity.admins.map { userMapper.toBO(it) }.toSet(),
        )
    }
}