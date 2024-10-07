package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.InterestedParticipantDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.dao.impl.EventDaoImpl
import ru.lashnev.community.bot.models.User

@Service
class PollAnswersService(
    val eventDao: EventDaoImpl,
    val userDao: UserDAO,
    val interestedParticipantDao: InterestedParticipantDao
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (update.pollAnswer() == null) {
            return
        }

        val telegramUser = update.pollAnswer().user()
        userDao.save(
            User(
                telegramUsername = telegramUser.username(),
                telegramId = telegramUser.id(),
                firstName = telegramUser.firstName(),
                lastName = telegramUser.lastName(),
            )
        )

        if (eventDao.isPollIdExist(update.pollAnswer().pollId())) {
            val pollAnswer = update.pollAnswer()
            val pollAnswerYes = pollAnswer.optionIds().isNotEmpty() && pollAnswer.optionIds()[0] == 0
            val event = eventDao.getEventByPollId(pollAnswer.pollId()).orElseThrow()
            val user = userDao.getByTelegramId(pollAnswer.user().id()).orElseThrow()
            if (pollAnswerYes) {
                interestedParticipantDao.addParticipant(user, event)
            } else {
                interestedParticipantDao.removeParticipant(user, event)
            }
        }
    }
}
