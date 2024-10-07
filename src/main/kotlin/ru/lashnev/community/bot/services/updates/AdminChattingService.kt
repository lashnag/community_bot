package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.admin.AdminBot

@Service
class AdminChattingService(val adminBot: AdminBot) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (update.message()?.chat()?.type() == Chat.Type.Private || update.callbackQuery() != null) {
            adminBot.onUpdateReceived(update)
        }
    }
}
