package so.brendan.notice.ui.main

import android.graphics.Color
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import so.brendan.notice.R


class TodosPrioritySelectorController: ChildController(null),
        ActionBarLifecycle,
        ActionBarThemable {
    override val title: String = "Select Priority List"
    override val actionBarTheme: ActionBarTheme
        get() = ActionBarTheme(resources!!.getColor(R.color.colorAccent), Color.WHITE, true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_priority_selector, container, false)
    }
}