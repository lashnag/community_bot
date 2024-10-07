package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.ChatMember
import com.pengrad.telegrambot.model.ChatMemberUpdated
import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.config.BotProperties
import ru.lashnev.community.bot.dao.CommunityDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.models.Community

@Service
class AddingToTheGroupService(
    val communityDao: CommunityDao,
    val userDao: UserDAO,
    val botProperties: BotProperties
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if(update.myChatMember() !is ChatMemberUpdated) {
            return
        }

        val newChatMember = update.myChatMember().newChatMember().user()
        val status = update.myChatMember().newChatMember().status()
        if(status != ChatMember.Status.member || newChatMember.isBot == false || newChatMember.username() != botProperties.userName) {
            return
        }

        val userAdding = update.myChatMember().from()
        val chat = update.myChatMember().chat()
        val user = userDao.getByTelegramId(userAdding.id())
        if (user.isPresent && user.get().isSuperuser) {
            communityDao.save(
                Community(
                    chatId = chat.id().toString(),
                    capacity = UNLIMITED_GROUP_CAPACITY,
                    name = chat.title().orEmpty(),
                    admins = emptySet(),
                )
            )
        }
    }

    companion object {
        private const val UNLIMITED_GROUP_CAPACITY = 1000
    }
}
