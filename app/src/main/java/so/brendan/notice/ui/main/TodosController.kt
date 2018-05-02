package so.brendan.notice.ui.main

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.controller_todos.view.*
import kotlinx.android.synthetic.main.view_todo.view.*
import so.brendan.notice.App
import so.brendan.notice.R
import so.brendan.notice.model.AppDatabase
import so.brendan.notice.model.AppTodo
import so.brendan.notice.util.children
import so.brendan.notice.util.disposedBy
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

interface TodosView {
    fun showLoading()
    fun showTodos(todos: List<AppTodo>)
    fun handleError(throwable: Throwable)
}

class TodosPresenter(private val view: TodosView, private val db: AppDatabase) {
    private fun bindAllTodos(): Disposable {
        return db.todoDao().all()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.showTodos(it)
            }, view::handleError)
    }

    fun start(): Disposable {
        return CompositeDisposable(
                bindAllTodos()
        )
    }
}

class TodosController(args: Bundle? = null) : ChildController(args),
        TodosView,
        ActionBarLifecycle,
        ActionBarMenu {

    override val title = "Today's Priorities"
    override val actionBarMenu = R.menu.todos_action_bar

    override fun onActionBarAttach(toolbar: Toolbar, actionBar: ActionBar) {
        toolbar.setOnClickListener {
            activity?.router?.pushController(RouterTransaction.with(TodosPrioritySelectorController())
                    .pushChangeHandler(HorizontalChangeHandler())
                    .popChangeHandler(HorizontalChangeHandler()))
        }
    }

    override fun onActionBarDetach(toolbar: Toolbar, actionBar: ActionBar) {
        toolbar.setOnClickListener(null)
    }

    override fun onActionBarItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {}
        }

        return true
    }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT)

    inner class TodoViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var todo: AppTodo? = null
            set(value) {
                field = value
                if (value == null) {
                    return
                }
                view.title.text = value.title
                view.due_date.text = dateFormatter.format(value.dueDate)
            }
    }

    inner class TodosAdapter(todos: List<AppTodo>) : RecyclerView.Adapter<TodoViewHolder>() {

        var todos: List<AppTodo> = todos
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_todo, parent, false)

            val holder = TodoViewHolder(view)

            view.setOnClickListener { v ->
                holder.todo?.let {
                    router.pushController(RouterTransaction.with(TodoDetailController(it))
                            .pushChangeHandler(VerticalChangeHandler())
                            .popChangeHandler(VerticalChangeHandler()))
                }
            }

            view.setOnCreateContextMenuListener { menu, v, menuInfo ->
                val delayMenu = menu.addSubMenu("Delay for…")
                delayMenu.add("1 Month")
                delayMenu.add("1 Day")
                delayMenu.add("6 Hours")
                delayMenu.add("1 Hour")

                menu.add("Move back one item")
            }

            return holder
        }

        override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
            holder.todo = todos[position]
        }

        override fun getItemCount() = todos.size
    }

    private val view get() = this.getView() as? ViewGroup
    private val adapter = TodosAdapter(emptyList())
    private val presenter by lazy { TodosPresenter(this, (applicationContext as App).db) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_todos, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        view.todos_view.apply {
            setHasFixedSize(true)
            val manager = LinearLayoutManager(view.context)
            manager.reverseLayout = true
            layoutManager = manager
            adapter = this@TodosController.adapter
        }

        view.fab.setOnClickListener {
            (applicationContext as App).db
                    .todoDao()
                    .insert(AppTodo("A new one", Date()))
        }

//        showTodos(listOf(AppTodo("Test Todo 1", Date()), AppTodo("Test Todo 2", Date()), AppTodo("Test Todo 3", Date())))

        presenter.start().disposedBy(bag)
    }

    private fun showContainer(container: View) {
        view?.let {
            it.children.forEach { it.visibility = View.GONE }
            container.visibility = View.VISIBLE
        }
    }

    override fun showLoading() {
        view?.let {
            showContainer(it.loading_view)
            it.fab.visibility = View.GONE
        }
    }

    override fun showTodos(todos: List<AppTodo>) {
        view?.let {
            adapter.todos = todos

            if (todos.isEmpty()) {
                showContainer(it.empty_view)
            } else {
                showContainer(it.content_view)
            }

            it.fab.visibility = View.VISIBLE
        }
    }

    override fun handleError(throwable: Throwable) {
        val view = view ?: return

        val bar = Snackbar.make(view, throwable.localizedMessage, Snackbar.LENGTH_INDEFINITE)
        bar.setAction("Close", { bar.dismiss() })
    }
}
