package so.brendan.notice.util

import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.disposedBy(bag: CompositeDisposable) {
    bag.add(this)
}

class ViewCollection(private val viewGroup: ViewGroup): Collection<View> {
    override val size: Int get() = viewGroup.childCount
    override fun contains(element: View): Boolean = viewGroup.indexOfChild(element) != -1
    override fun containsAll(elements: Collection<View>): Boolean =
            elements.all { viewGroup.indexOfChild(it) != -1 }
    override fun isEmpty(): Boolean = size == 0

    private inner class ViewIterator: Iterator<View> {
        private var i = 0

        override fun hasNext(): Boolean = i != size

        override fun next(): View {
            val view = viewGroup.getChildAt(i)
            i += 1
            return view
        }
    }

    override fun iterator(): Iterator<View> = ViewIterator()
}

val ViewGroup.children: Collection<View> get() = ViewCollection(this)