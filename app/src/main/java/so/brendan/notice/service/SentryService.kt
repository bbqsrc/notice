package so.brendan.notice.service


// Generated. Do not edit.

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.openid.appauth.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Target(AnnotationTarget.TYPE)
@MustBeDocumented
annotation class Format(val value: String)

interface JsonEnum {
    val value: String
}

sealed class SentryServer(private val urlPattern: String) {
    object V0: SentryServer("https://sentry.io/api/0/")

    override fun toString(): String = this.urlPattern
}


/**
 * Paste the following line into your AndroidManifest.xml to enable the callback activity.
 *
<activity
android:name=".OAuth2AuthorizationCodeOAuthActivity">
<intent-filter>
<action android:name="android.intent.action.VIEW"/>
<category android:name="android.intent.category.DEFAULT"/>
<category android:name="android.intent.category.BROWSABLE"/>
<data android:scheme="https"
android:host="[your callback host]"
android:path="[your callback path]"/>
</intent-filter>
<intent-filter>
<action android:name="android.intent.action.VIEW"/>
<category android:name="android.intent.category.DEFAULT"/>
<category android:name="android.intent.category.BROWSABLE"/>
<data android:scheme="[your custom scheme, eg com.example.oauth]"/>
</intent-filter>
</activity>
 */
class OAuth2AuthorizationCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        OAuth2AuthorizationCodeOAuth.subject.onNext(resp to ex)
        finish()
    }
}

class OAuth2AuthorizationCodeOAuth(val clientId: String, val clientSecret: String, val redirectUri: Uri) {
    companion object {
        internal val subject = PublishSubject.create<Pair<AuthorizationResponse?, AuthorizationException?>>()
    }

    enum class Scope(val value: String) {
        PROJECT_READ("project:read"),
        PROJECT_WRITE("project:write"),
        PROJECT_ADMIN("project:admin"),
        PROJECT_RELEASES("project:releases"),
        TEAM_READ("team:read"),
        TEAM_WRITE("team:write"),
        TEAM_ADMIN("team:admin"),
        EVENT_READ("event:read"),
        EVENT_ADMIN("event:admin"),
        ORG_READ("org:read"),
        ORG_WRITE("org:write"),
        ORG_ADMIN("org:admin"),
        MEMBER_READ("member:read"),
        MEMBER_ADMIN("member:admin");

        override fun toString() = value
    }

    private val authorizeUri = Uri.parse("https://sentry.io/oauth/authorize/")!!
    private val accessTokenUri = Uri.parse("https://sentry.io/oauth/token/")!!
//
//    class OAuthInterceptor(val client: OAuth2AuthorizationCodeOAuth,
//                           var tokenResponse: TimestampedTokenResponse,
//                           val onNewToken: (TimestampedTokenResponse) -> Unit
//    ) : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val req = chain.request()
//            val response = tokenResponse.response
//
//            val newReq = req.newBuilder()
//                    .header("Authorization", "Bearer ${response.accessToken}")
//                    .build()
//            return chain.proceed(newReq)
//        }
//    }
//
//    fun interceptor(response: TimestampedTokenResponse, onNewToken: (TimestampedTokenResponse) -> Unit): OAuthInterceptor =
//            OAuthInterceptor(this, response, onNewToken)
//
//    private fun Call.single(): Single<Response> {
//        return Single.create { single ->
//            single.setCancellable(this::cancel)
//
//            this.enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    single.onError(e)
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        single.onSuccess(response)
//                    } catch (t: Throwable) {
//                        single.onError(t)
//                    }
//                }
//            })
//        }
//    }

    fun authorize(activity: Activity, scopes: List<Scope>? = null): Single<TokenResponse> {
        val service = AuthorizationService(activity)
        val config = AuthorizationServiceConfiguration(authorizeUri, accessTokenUri)

        val observer = subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .take(1)
                .singleOrError()
                .flatMap {
                    if (it.second != null) {
                        Single.error<AuthorizationResponse>(it.second)
                    } else {
                        Single.just(it.first!!)
                    }
                }
                .flatMap {
                    val tokenReq = TokenRequest.Builder(config, clientId)
                            .setAuthorizationCode(it.authorizationCode)
                            .setRedirectUri(redirectUri)
                            .build()
                    Single.create<TokenResponse> { emitter ->
                        service.performTokenRequest(tokenReq, ClientSecretPost(clientSecret), { res, ex ->
                            if (ex != null) {
                                emitter.onError(ex)
                            } else {
                                emitter.onSuccess(res!!)
                            }
                        })
                    }
                }
                .doFinally { service.dispose() }

        val req = AuthorizationRequest.Builder(config,
                    clientId,
                    ResponseTypeValues.CODE,
                    redirectUri)
                .setScopes(scopes?.map { it.toString() })
                .build()

        val pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                Intent(activity, OAuth2AuthorizationCodeActivity::class.java),
                0)

        service.performAuthorizationRequest(req, pendingIntent)

        return observer
    }
}

private fun Class<*>.isExtendedEnum(): Boolean {
    if (!isEnum) {
        return false
    }

    if (declaredConstructors.isNotEmpty()) {
        return !declaredConstructors[0].toGenericString().endsWith("()]")
    }

    return false
}

private fun createGson(): Gson {
    fun createDateFormatter(pattern: String, tz: String): SimpleDateFormat {
        val df = SimpleDateFormat(pattern, Locale.ROOT)
        df.timeZone = TimeZone.getTimeZone(tz)
        return df
    }

    class EnumTypeAdapter<T>(private val type: Class<T>) : TypeAdapter<T>() {
        private fun rawValue(value: T) = (value as JsonEnum).value

        override fun write(writer: JsonWriter, value: T) {
            writer.value(rawValue(value))
        }

        override fun read(reader: JsonReader): T {
            val s = reader.nextString()
            return type.enumConstants.first { rawValue(it) == s }
                    ?: throw Exception("Invalid value: $s")
        }
    }


    class DateAdapter(format: String) : TypeAdapter<Date>() {
        private val formatter = when (format) {
            "date" -> createDateFormatter("yyyy-MM-dd", "UTC")
            else -> createDateFormatter("yyyy-MM-dd'T'HH:mm:ss'Z'", "UTC")
        }

        override fun write(writer: JsonWriter, value: Date) {
            writer.value(formatter.format(value))
        }

        override fun read(reader: JsonReader): Date {
            return formatter.parse(reader.nextString())
        }
    }

    class DateAdapterFactory : TypeAdapterFactory {
        override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
            if (type.rawType != Date::class.java) {
                return null
            }

            val format = type.rawType.getAnnotation(Format::class.java)?.value ?: "date-time"
            return DateAdapter(format).nullSafe() as TypeAdapter<T>
        }
    }

    class EnumTypeAdapterFactory : TypeAdapterFactory {
        override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
            if (type.rawType.isEnum && !type.rawType.interfaces.contains(JsonEnum::class.java)) {
                throw Exception("Type ${type.rawType} does not declare safe enum interface for JSON.")
            }

            if (!type.rawType.isExtendedEnum()) {
                return null
            }

            return EnumTypeAdapter(type.rawType) as TypeAdapter<T>
        }
    }

    class UriTypeAdapter: TypeAdapter<Uri>() {
        override fun write(out: JsonWriter, value: Uri) {
            out.value(value.toString())
        }

        override fun read(reader: JsonReader): Uri {
            return Uri.parse(reader.nextString())!!
        }
    }

    return GsonBuilder()
            .registerTypeAdapterFactory(EnumTypeAdapterFactory())
//            .registerTypeAdapterFactory(DateAdapterFactory())
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()
}

interface SentryService {
    companion object {
        fun create(server: SentryServer, vararg interceptors: Interceptor) =
                create(server.toString(), *interceptors)

        fun create(baseUrl: String, vararg interceptors: Interceptor): SentryService =
                Retrofit.Builder()
                        .client(interceptors.fold(OkHttpClient.Builder(), { acc, cur -> acc.addInterceptor(cur) }).build())
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create(createGson()))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                        .build()
                        .create(SentryService::class.java)
    }

    @GET("projects/{organization_slug}/{project_slug}/hooks/{hook_id}/")
    fun getServiceHook(@Path("organization_slug") organizationSlug: String,
                       @Path("project_slug") projectSlug: String,
                       @Path("hook_id") hookId: String): Observable<ServiceHook>

    @POST("projects/{organization_slug}/{project_slug}/hooks/")
    fun registerServiceHook(@Path("organization_slug") organizationSlug: String,
                            @Path("project_slug") projectSlug: String,
                            @Body body: ServiceHookRequest): Observable<ServiceHook>
}



enum class ServiceHookEvent(override val value: String): JsonEnum {
    EVENT_ALERT("event.alert"),
    EVENT_CREATED("event.created");

    override fun toString() = this.value
}

data class ServiceHook(
        @SerializedName("TODO")
        val todo: Boolean?
) : Serializable {
}

data class ServiceHookRequest(
        val url: @Format("url") Uri,
        val events: List<ServiceHookEvent>
) : Serializable {
}

