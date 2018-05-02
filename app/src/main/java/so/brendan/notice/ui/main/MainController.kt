package so.brendan.notice.ui.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.*
import kotlinx.android.synthetic.main.controller_main.view.*
import so.brendan.notice.R


class MainController : Controller(),
        BottomNavigationView.OnNavigationItemSelectedListener,
        ControllerChangeHandler.ControllerChangeListener {
    private val TAG = MainController::class.simpleName

    private fun pagerRouter(view: View) =
            getChildRouter(view.content_view, MainController::class.simpleName)

    private fun showController(controller: Controller) {
        pagerRouter(view!!).pushController(RouterTransaction.with(controller))
    }

    override fun onChangeStarted(to: Controller?, from: Controller?, isPush: Boolean, container: ViewGroup, handler: ControllerChangeHandler) {
        val view = view ?: return
        if (to == null) {
            return
        }

        val id = when (to) {
            is NotificationsController -> R.id.action_notifications
            is TodosController -> R.id.action_todos
            is SettingsController -> R.id.action_settings
            else -> return
        }

        view.bottom_navigation.apply {
            // Remove listener while setting id to stop infinite callback loop on back press
            setOnNavigationItemSelectedListener(null)
            selectedItemId = id
            setOnNavigationItemSelectedListener(this@MainController)
        }
    }

    override fun onChangeCompleted(to: Controller?, from: Controller?, isPush: Boolean, container: ViewGroup, handler: ControllerChangeHandler) {
        // Nothing, don't care.
        Log.d(TAG, "onChangeCompleted")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notifications -> showController(NotificationsController())
            R.id.action_todos -> showController(TodosController())
            R.id.action_settings -> showController(SettingsController())
            else -> throw Exception("Invalid id found")
        }

        return true
    }

    private var hasNavigationListener = false

    private fun bindNavigationListener(view: View) {
        if (!hasNavigationListener) {
            hasNavigationListener = true
            val mainActivity = activity as ActionBarControllerActivity
            val router = pagerRouter(view)
            router.addChangeListener(this)
            router.addChangeListener(mainActivity.actionBarChangeListener)
            view.bottom_navigation.setOnNavigationItemSelectedListener(this)
        }
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        bindNavigationListener(view)
    }

    override fun onAttach(view: View) {
        if (!hasNavigationListener) {
            bindNavigationListener(view)
            view.bottom_navigation.selectedItemId = R.id.action_notifications
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_main, container, false)
    }
}