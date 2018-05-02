package so.brendan.notice.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import java.text.SimpleDateFormat
import java.util.*

@Database(entities = [AppTodo::class], version = 1)
@TypeConverters(AppDatabaseConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun todoDao(): AppTodoDao


}

fun AppDatabase.clearAllTables() {
    todoDao().dropTable()
}

class AppDatabaseConverters {
    val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ROOT)

    @TypeConverter
    fun fromDateString(dateString: String?): Date? {
        return dateString?.let {
            isoDateFormatter.parse(it)
        }
    }

    @TypeConverter
    fun toDateString(date: Date?): String? {
        return date?.let { isoDateFormatter.format(it) }
    }
}
