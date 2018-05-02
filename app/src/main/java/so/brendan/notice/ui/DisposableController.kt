package so.brendan.notice.ui

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import io.reactivex.disposables.CompositeDisposable

abstract class DisposableController(bundle: Bundle? = null): Controller(bundle) {
    var bag = CompositeDisposable()

    override fun onDetach(view: View) {
        super.onDetach(view)

        // Clean up bag
        bag = CompositeDisposable()
    }
}