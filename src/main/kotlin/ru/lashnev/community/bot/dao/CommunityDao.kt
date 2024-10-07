package ru.lashnev.community.bot.dao

import ru.lashnev.community.bot.models.Community
import ru.lashnev.community.bot.models.User

interface CommunityDao {
    fun findById(communityId: Long): Community
    fun getAllCommunitiesByAdmin(user: User): Set<Community>
    fun save(community: Community)
}
