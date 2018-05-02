package so.brendan.notice.util

import android.os.Bundle
import java.io.Serializable


class BundleBuilder(private val bundle: Bundle = Bundle()) {
    fun putSerializable(key: String, value: Serializable): BundleBuilder {
        bundle.putSerializable(key, value)
        return this
    }

    fun build(): Bundle {
        return bundle.clone() as Bundle
    }
}