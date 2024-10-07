package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.model.Update

interface UpdatesService {
    fun processUpdates(update: Update)
}
