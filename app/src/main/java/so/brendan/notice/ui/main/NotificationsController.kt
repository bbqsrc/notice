package so.brendan.notice.ui.main

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.controller_notifications.view.*
import kotlinx.android.synthetic.main.view_notification.view.*
import so.brendan.notice.R
import so.brendan.notice.ui.DisposableController
import so.brendan.notice.ui.models.AppNotification
import so.brendan.notice.util.children
import so.brendan.notice.util.disposedBy
import java.util.*
import java.util.concurrent.TimeUnit

interface NotificationsView {
    fun showLoading()
    fun showNotifications(notifications: List<AppNotification>)
    fun handleError(throwable: Throwable)
}

class NotificationsPresenter(private val view: NotificationsView) {
    private fun bindNotifications(): Disposable {
        val n = listOf(
                AppNotification("First", "Description field blah", Date()),
                AppNotification("Second", "Description field blah", Date()),
                AppNotification("Third", "Description field blah", Date()),
                AppNotification("Fourth", "Description field blah", Date())
        )

        return Single.just(n)
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(view::showNotifications, view::handleError)
    }

    fun start(): Disposable {
        view.showLoading()

        return bindNotifications()
    }
}

class NotificationsController(args: Bundle? = null) : ChildController(args),
        NotificationsView,
        ActionBarLifecycle {
    override fun onActionBarAttach(toolbar: Toolbar, actionBar: ActionBar) {}
    override fun onActionBarDetach(toolbar: Toolbar, actionBar: ActionBar) {}

    override val title = "Notifications"

    inner class NotificationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(notification: AppNotification) {
            view.title_text.text = notification.title
        }
    }

    inner class NotificationsAdapter(notifications: List<AppNotification>) : RecyclerView.Adapter<NotificationViewHolder>() {
        var notifications: List<AppNotification> = notifications
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) =
                holder.bind(notifications[position])

        override fun getItemCount() = notifications.size
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_notifications, container, false)
    }

    private val view get() = this.getView() as? ViewGroup
    private val adapter: NotificationsAdapter = NotificationsAdapter(emptyList())
    private val presenter by lazy { NotificationsPresenter(this) }

    override fun onAttach(view: View) {
        super.onAttach(view)

        view.notifications_view.apply {
            setHasFixedSize(true)
            val manager = LinearLayoutManager(view.context)
            manager.reverseLayout = true
            layoutManager = manager
            adapter = this@NotificationsController.adapter
        }

        presenter.start().disposedBy(bag)
    }

    private fun showContainer(container: View) {
        view?.let {
            it.children.forEach { it.visibility = View.GONE }
            container.visibility = View.VISIBLE
        }
    }

    override fun showLoading() {
        view?.let { showContainer(it.loading_view) }
    }

    override fun showNotifications(notifications: List<AppNotification>) {
        view?.let {
            adapter.notifications = notifications

            if (notifications.isEmpty()) {
                showContainer(it.empty_view)
            } else {
                showContainer(it.content_view)
            }
        }
    }

    override fun handleError(throwable: Throwable) {
        val view = view ?: return

        val bar = Snackbar.make(view, throwable.localizedMessage, Snackbar.LENGTH_INDEFINITE)
        bar.setAction("Close", { bar.dismiss() })
    }
}
