package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.ConfirmedParticipantDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.dao.impl.EventDaoImpl
import ru.lashnev.community.bot.models.User

@Service
class PollConfirmationAnswersService(
    val eventDao: EventDaoImpl,
    val userDao: UserDAO,
    val confirmedParticipantDao: ConfirmedParticipantDao,
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

        if (eventDao.isPollConfirmationIdExist(update.pollAnswer().pollId())) {
            val pollAnswer = update.pollAnswer()
            val pollAnswerYes = pollAnswer.optionIds().isNotEmpty() && pollAnswer.optionIds()[0] == 0
            val event = eventDao.getEventByPollConfirmationId(pollAnswer.pollId()).orElseThrow()
            val user = userDao.getByTelegramId(pollAnswer.user().id()).orElseThrow()
            if (pollAnswerYes) {
                confirmedParticipantDao.addParticipant(user, event)
            } else {
                confirmedParticipantDao.removeParticipant(user, event)
            }
        }
    }
}
