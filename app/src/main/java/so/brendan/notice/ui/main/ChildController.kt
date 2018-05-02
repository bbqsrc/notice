package so.brendan.notice.ui.main

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.bluelinelabs.conductor.Controller
import io.reactivex.disposables.CompositeDisposable


abstract class ChildController(args: Bundle?): Controller(args), ActionBarLifecycle {
    protected var bag = CompositeDisposable()

    val activity: MainActivity? get() = getActivity() as? MainActivity

    override fun onActionBarAttach(toolbar: Toolbar, actionBar: ActionBar) {}
    override fun onActionBarDetach(toolbar: Toolbar, actionBar: ActionBar) {}

    override fun onDetach(view: View) {
        super.onDetach(view)
        bag = CompositeDisposable()
    }
}
