package com.example.data.remote.supabase

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {
    fun getSupabaseUrl(): String {
        return try {
            val url = BuildConfig.SUPABASE_URL.trim()
            if (url.isNotBlank() && url != "https://your-project.supabase.co") {
                if (url.endsWith("/")) url else "$url/"
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun getSupabaseAnonKey(): String {
        return try {
            val key = BuildConfig.SUPABASE_ANON_KEY.trim()
            if (key.isNotBlank() && key != "YOUR_SUPABASE_ANON_KEY") {
                key
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun isConfigured(): Boolean {
        return getSupabaseUrl().isNotBlank() && getSupabaseAnonKey().isNotBlank()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private var cachedApi: SupabaseApi? = null
    private var cachedUrl: String? = null

    fun getApi(): SupabaseApi? {
        val url = getSupabaseUrl()
        if (url.isBlank()) return null
        if (cachedApi != null && cachedUrl == url) {
            return cachedApi
        }
        val api = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
        cachedApi = api
        cachedUrl = url
        return api
    }
}
