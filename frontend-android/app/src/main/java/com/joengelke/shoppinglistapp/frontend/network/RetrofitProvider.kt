package com.joengelke.shoppinglistapp.frontend.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.joengelke.shoppinglistapp.frontend.datastore.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Dns
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Singleton
class RetrofitProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null

    suspend fun initialize() {
        val baseUrl = SettingsDataStore.serverUrlFlow(context).firstOrNull()
            ?: throw IllegalStateException("Server URL is not set")

        val gson: Gson = GsonBuilder().create()

        okHttpClient = getSafeOkHttpClient(context, baseUrl.split(":")[1].removePrefix("//"))

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient!!)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun getSafeOkHttpClient(context: Context, ip: String): OkHttpClient {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = context.assets.open("mycertoracle.crt").use {
            certificateFactory.generateCertificate(it)
        }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("backendserveroracle", certificate)
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        ).apply {
            init(keyStore)
        }

        val trustManager = trustManagerFactory.trustManagers[0] as X509TrustManager

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustManagerFactory.trustManagers, null)
        }

        return OkHttpClient.Builder()
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return Dns.SYSTEM.lookup(hostname).filterIsInstance<Inet4Address>()
                }
            })
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, _ -> hostname == ip } // TODO change for DEV
            .build()
    }

    fun getAuthApi(): AuthApi {
        return retrofit!!.create(AuthApi::class.java)
    }

    fun getShoppingListApi(): ShoppingListApi {
        return retrofit!!.create(ShoppingListApi::class.java)
    }

    fun getShoppingItemApi(): ShoppingItemApi {
        return retrofit!!.create(ShoppingItemApi::class.java)
    }

    fun getItemSetApi(): ItemSetApi {
        return retrofit!!.create(ItemSetApi::class.java)
    }

    fun getUserApi(): UserApi {
        return retrofit!!.create(UserApi::class.java)
    }

    fun getRecipeApi(): RecipeApi{
        return retrofit!!.create(RecipeApi::class.java)
    }

    fun getOkHttpClient(): OkHttpClient {
        return okHttpClient ?: throw IllegalStateException("OkHttpClient not initialized")
    }
}
