package ru.lashnev.community.bot.dao

import ru.lashnev.community.bot.models.Community
import ru.lashnev.community.bot.models.User
import java.util.Optional

interface UserDAO {
    fun save(user: User)
    fun getById(userId: Long): Optional<User>
    fun getByUsername(username: String): Optional<User>
    fun getByTelegramId(telegramId: Long): Optional<User>
    fun getAdminsByChatId(chatId: Long): Set<User>
}
