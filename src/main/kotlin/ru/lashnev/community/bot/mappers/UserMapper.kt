package ru.lashnev.community.bot.mappers

import org.springframework.stereotype.Component
import ru.lashnev.community.bot.dao.models.UserEntity
import ru.lashnev.community.bot.models.User

@Component
class UserMapper {
    fun toBO(userEntity: UserEntity): User {
        return User(
            userId = userEntity.userId,
            telegramId = userEntity.telegramId,
            telegramUsername = userEntity.telegramUsername,
            firstName = userEntity.firstName,
            lastName = userEntity.lastName,
            isSuperuser = userEntity.isSuperuser,
        )
    }

    fun toEntity(user: User): UserEntity {
        return UserEntity(
            userId = user.userId,
            telegramId = user.telegramId,
            telegramUsername = user.telegramUsername,
            firstName = user.firstName,
            lastName = user.lastName,
            isSuperuser = user.isSuperuser,
        )
    }
}