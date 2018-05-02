package so.brendan.notice

import android.app.Application
import android.arch.persistence.room.Room
import so.brendan.notice.model.AppDatabase
import so.brendan.notice.model.AppTodo
import so.brendan.notice.model.clearAllTables
import java.util.*

class App: Application() {
    val db by lazy {
        Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "appdb"
        ).allowMainThreadQueries().build()
    }

    override fun onCreate() {
        super.onCreate()

        db.clearAllTables()
        db.todoDao().insert(AppTodo("A new todo", Date()))
    }
}