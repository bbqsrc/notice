package so.brendan.notice.ui.main

import android.net.Uri
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.view_settings.view.*
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import so.brendan.notice.R
import so.brendan.notice.service.*
import so.brendan.notice.util.disposedBy


class SettingsController : ChildController(null) {
    private val TAG = SettingsController::class.simpleName

    override val title = "Settings"
    val oauthBag = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.view_settings, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        view.button_sentry_oauth.setOnClickListener {
            val oauth = OAuth2AuthorizationCodeOAuth(
                    "9741ae2d4c2645fc8f17c2a202aaa66ff56b23e9f9604bcab7dcccbf09d091fe",
                    "8039f6f66a334b92aba38db8fa30643f6ad7ad6c55c44fa9a2c2a88885719ae8",
                    Uri.parse("https://tcj3uvekj8.execute-api.eu-west-1.amazonaws.com/prod/so.brendan.notice.sentry")!!)

            oauth.authorize(activity!!,
                    listOf(OAuth2AuthorizationCodeOAuth.Scope.EVENT_ADMIN,
                            OAuth2AuthorizationCodeOAuth.Scope.EVENT_READ,
                            OAuth2AuthorizationCodeOAuth.Scope.PROJECT_ADMIN,
                            OAuth2AuthorizationCodeOAuth.Scope.PROJECT_READ))
                    .flatMap ({ result ->
                        Log.d(TAG, result.jsonSerializeString())
                        SentryService.create(SentryServer.V0, Interceptor {
                            val newReq = it.request().newBuilder()
                                    .header("Authorization", "Bearer ${result.accessToken}")
                                    .build()

                            it.proceed(newReq)
                        }, HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                                .getServiceHook("bbqsrc", "sami-android", "64c0b484d84e4e0b9b32ebeb5b99dfd2")
                                .take(1).singleOrError()
//                                .registerServiceHook("bbqsrc", "sami-android",
//                                ServiceHookRequest(Uri.parse("https://notice.brendan.so/webhook/sentry")!!,
//                                listOf(ServiceHookEvent.EVENT_ALERT, ServiceHookEvent.EVENT_CREATED))
//                        ).take(1).singleOrError()
                    }).subscribe({ doop ->
                        Log.d(TAG, "WOO")
                    })
                    .disposedBy(oauthBag)
        }
    }
}