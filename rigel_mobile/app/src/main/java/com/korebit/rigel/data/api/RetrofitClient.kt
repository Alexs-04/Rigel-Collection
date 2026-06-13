package com.korebit.rigel.data.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Shared Retrofit Client.
 *
 * Note: During local network development we allow HTTP (cleartext) connections.
 * In production `res/xml/network_security_config.xml` must be removed or modified
 * to require HTTPS.
 */
object RetrofitClient {
    private const val BASE_URL =
        "http://192.168.0.2:8080/" // TODO: Configure this URL for different environments

    // Interceptor that adds Authorization header when a token is available
    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest: Request = chain.request()
            val token = TokenManager.getToken()

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val requestWithAuth = requestBuilder.build()
            return chain.proceed(requestWithAuth)
        }
    }

    /** Small in-memory token manager.
     * - During the session the app can set the token with `setToken`.
     * - It is the responsibility of the login flow/persistent storage to synchronize
     * this value (for example using SharedPreferences). Here it is added so that
     * the interceptor can access the token easily.
     */
    object TokenManager {
        @Volatile
        private var token: String? = null

        fun setToken(value: String?) {
            token = value
        }

        fun getToken(): String? = token

        fun clear() {
            token = null
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        // AuthInterceptor must go before logging so that the header is seen in logs
        .addInterceptor(AuthInterceptor())
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)

    // Helpers so that the authentication flow can update the token
    fun setAuthToken(token: String?) = TokenManager.setToken(token)
    fun clearAuthToken() = TokenManager.clear()
}

