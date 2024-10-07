package ru.lashnev.community.bot.admin

enum class AdminCommand(val commandName: String) {
    CREATE_APPOINTMENT("/create_appointment"),
    FETCH_EVENTS("/fetch_events"),
    FETCH_EVENTS_SHORT("/fetch_events_short"),
    DELETE_EVENT("/delete_event"),
    UNKNOWN_COMMAND("unknown");
}

fun String.toCommand(): AdminCommand {
    for (value in AdminCommand.values()) {
        if (value.commandName == this) {
            return value
        }
    }

    return AdminCommand.UNKNOWN_COMMAND
}
