package so.brendan.notice.ui.models

import java.util.*

data class AppNotification(
        val title: String,
        val description: String,
        val receivedDate: Date)