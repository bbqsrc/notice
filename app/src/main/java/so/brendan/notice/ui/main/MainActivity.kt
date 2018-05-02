package so.brendan.notice.ui.main

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ActionMenuView
import android.widget.ImageButton
import com.bluelinelabs.conductor.*
import kotlinx.android.synthetic.main.activity_main.*
import so.brendan.notice.R
import so.brendan.notice.util.children

fun Toolbar.setTintColor(@ColorInt color: Int) {
    val filter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)

    this.post {
        for (view in children) {
            if (view is ImageButton) {
                view.drawable.colorFilter = filter
//                view.invalidate()
            }

            if (view is ActionMenuView) {
                for (innerView in view.children) {
                    if (innerView is ActionMenuItemView) {
                        innerView.compoundDrawables.forEach {
//                            innerView.post { it.colorFilter = filter }
                            it.colorFilter = filter
                        }
                    }
                }
            }
        }
    }

    setTitleTextColor(color)
    setSubtitleTextColor(color)
    overflowIcon?.colorFilter = filter
}

interface ActionBarThemable {
    val actionBarTheme: ActionBarTheme
}

interface ActionBarLifecycle {
    val title: String
    fun onActionBarAttach(toolbar: Toolbar, actionBar: ActionBar)
    fun onActionBarDetach(toolbar: Toolbar, actionBar: ActionBar)
}

interface ActionBarMenu {
    val actionBarMenu: Int
    fun onActionBarItemSelected(item: MenuItem): Boolean
}

data class ActionBarTheme(
        @ColorInt val backgroundColor: Int,
        @ColorInt val tintColor: Int,
        val isShowingBackButton: Boolean = false
) {
    fun applyTheme(toolbar: Toolbar, actionBar: ActionBar) {
        toolbar.setBackgroundColor(backgroundColor)
        toolbar.setTintColor(tintColor)

        actionBar.setHomeButtonEnabled(isShowingBackButton)
        actionBar.setDisplayHomeAsUpEnabled(isShowingBackButton)
    }
}

abstract class ActionBarControllerActivity(
        val defaultTheme: ActionBarTheme = ActionBarTheme(Color.WHITE, Color.BLACK)
): AppCompatActivity() {
    abstract val toolbar: Toolbar
    abstract val contentView: Int

    lateinit var router: Router
        private set

    private var actionBarMenuId: Int? = null
    private var actionBarItemSelectedCallback: ((MenuItem) -> Boolean)? = null

    inner class ActionBarControllerChangeListener(
            private val toolbar: Toolbar,
            private val actionBar: ActionBar,
            private val defaultTheme: ActionBarTheme
    ) : ControllerChangeHandler.ControllerChangeListener {
        private val TAG = ActionBarThemable::class.simpleName

        override fun onChangeStarted(to: Controller?, from: Controller?, isPush: Boolean, container: ViewGroup, handler: ControllerChangeHandler) {
            // Run detach woot
            if (from is ActionBarLifecycle) {
                from.onActionBarDetach(toolbar, actionBar)
            }

            val controller = to ?: return

            // Add menu if exists
            if (controller is ActionBarMenu) {
                actionBarMenuId = controller.actionBarMenu
                actionBarItemSelectedCallback = controller::onActionBarItemSelected
            } else {
                actionBarMenuId = null
                actionBarItemSelectedCallback = null
            }

            // Needed to redraw options
            invalidateOptionsMenu()

            // Apply the theme
            if (controller is ActionBarThemable) {
                Log.d(TAG, "Applying theme: ${controller.actionBarTheme}")
                controller.actionBarTheme.applyTheme(toolbar, actionBar)
            } else {
                Log.d(TAG, "Applying default theme: $defaultTheme")
                defaultTheme.applyTheme(toolbar, actionBar)
            }
            toolbar.refreshDrawableState()

            // Run attach woot
            if (controller is ActionBarLifecycle) {
                toolbar.title = controller.title
                controller.onActionBarAttach(toolbar, actionBar)
            }
        }

        override fun onChangeCompleted(to: Controller?, from: Controller?, isPush: Boolean, container: ViewGroup, handler: ControllerChangeHandler) {

        }
    }

    val actionBarChangeListener by lazy {
        ActionBarControllerChangeListener(
            toolbar, supportActionBar!!, defaultTheme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
        setSupportActionBar(toolbar)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        router.addChangeListener(actionBarChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return actionBarMenuId?.let {
            menuInflater.inflate(it, menu)
            true
        } ?: false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.onBackPressed()
            return true
        }

        return actionBarItemSelectedCallback?.let {
            it(item)
        } ?: super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}

class MainActivity : ActionBarControllerActivity() {
    override val toolbar: Toolbar
        get() = app_toolbar

    override val contentView: Int
        get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(MainController()))
        }
    }
}

//class MainActivity : AppCompatActivity() {
//    internal lateinit var router: Router
//        private set
//
//    internal var actionBarMenuId: Int = R.menu.main_action_bar
//        set(value) {
//            field = value
//            invalidateOptionsMenu()
//        }
//
//    internal var actionBarItemSelectedCallback: ((MenuItem) -> Boolean)? = null
//
//    internal val toolbar: Toolbar get() = app_toolbar
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        setSupportActionBar(app_toolbar)
//
//        router = Conductor.attachRouter(this, container, savedInstanceState)
//        router.addChangeListener(ActionBarControllerChangeListener(
//                toolbar, supportActionBar!!, ActionBarTheme(Color.WHITE, Color.BLACK)))
//
//        if (!router.hasRootController()) {
//            router.setRoot(RouterTransaction.with(MainController()))
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(actionBarMenuId, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            this.onBackPressed()
//            return true
//        }
//
//        return actionBarItemSelectedCallback?.let {
//            it(item)
//        } ?: super.onOptionsItemSelected(item)
//    }
//
//    override fun onBackPressed() {
//        if (!router.handleBack()) {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onNavigateUp(): Boolean {
//        finish()
//        return true
//    }
//}