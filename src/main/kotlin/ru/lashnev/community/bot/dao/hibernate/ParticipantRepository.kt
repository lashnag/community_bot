package ru.lashnev.community.bot.dao.hibernate

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.models.ParticipantEntity

@Repository
interface ParticipantRepository : JpaRepository<ParticipantEntity, Long>