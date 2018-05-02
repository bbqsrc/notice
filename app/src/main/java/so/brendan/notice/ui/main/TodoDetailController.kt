package so.brendan.notice.ui.main

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import so.brendan.notice.R
import so.brendan.notice.model.AppTodo
import so.brendan.notice.util.BundleBuilder


private const val KEY_TODO = "so.brendan.notice.key.appTodo"

class TodoDetailController(args: Bundle): ChildController(args), ActionBarThemable, ActionBarMenu {
    override val actionBarMenu = R.menu.todo_detail_action_bar

    override fun onActionBarItemSelected(item: MenuItem): Boolean {
        return true
    }

    override val title: String get() = todo.title
    override val actionBarTheme = ActionBarTheme(Color.LTGRAY, Color.BLACK, true)

    private val todo: AppTodo = args.getSerializable(KEY_TODO) as? AppTodo
            ?: throw RuntimeException("No todo saved in bundle")

    constructor(todo: AppTodo) : this(BundleBuilder()
            .putSerializable(KEY_TODO, todo)
            .build())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_todo_detail, container, false)
    }
}