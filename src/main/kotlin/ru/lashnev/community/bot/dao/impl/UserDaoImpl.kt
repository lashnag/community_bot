package ru.lashnev.community.bot.dao.impl

import jakarta.annotation.PostConstruct
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.dao.hibernate.UserRepository
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.User
import java.util.Optional

@Repository
class UserDaoImpl(private val userRepository: UserRepository, private val userMapper: UserMapper) : UserDAO {
    override fun save(user: User) {
        if(userRepository.findByTelegramId(user.telegramId) != null) {
            return
        }

        userRepository.save(userMapper.toEntity(user))
    }

    override fun getById(userId: Long): Optional<User> {
        return mapToOptional(userRepository.findByIdOrNull(userId)?.let { userMapper.toBO(it) })
    }

    private fun mapToOptional(user: User?): Optional<User> {
        return if (user == null) {
            Optional.empty<User>()
        } else {
            return Optional.of(user)
        }
    }

    override fun getByUsername(username: String): Optional<User> {
        return mapToOptional(userRepository.getByTelegramUsername(username)?.let { userMapper.toBO(it) })
    }

    override fun getByTelegramId(telegramId: Long): Optional<User> {
        return mapToOptional(userRepository.findByTelegramId(telegramId)?.let { userMapper.toBO(it) })
    }

    override fun getAdminsByChatId(chatId: Long): Set<User> {
        return userRepository.findByCommunitiesChatId(chatId.toString()).map { userMapper.toBO(it) }.toSet()
    }
}
