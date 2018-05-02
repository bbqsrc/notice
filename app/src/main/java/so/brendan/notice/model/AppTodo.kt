package so.brendan.notice.model

import android.arch.persistence.room.*
import io.reactivex.Flowable
import java.io.Serializable
import java.util.*

@Entity(tableName = "todos")
data class AppTodo(
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "due_date") val dueDate: Date,
        @PrimaryKey(autoGenerate = true) val id: Int? = null
): Serializable

@Dao
interface AppTodoDao {
    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    fun byId(id: Int): Flowable<AppTodo>

    @Query("SELECT * FROM todos")
    fun all(): Flowable<List<AppTodo>>

    @Insert
    fun insert(todo: AppTodo): Long

    @Insert
    fun insert(vararg todos: AppTodo): Array<Long>

    @Query("DELETE FROM todos")
    fun dropTable()
}

interface TodoItem<T> {
    val model: T
    fun markCompleted()
}