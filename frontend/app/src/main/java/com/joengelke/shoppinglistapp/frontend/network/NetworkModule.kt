package com.joengelke.shoppinglistapp.frontend.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.NetworkInterface
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


/**
 * Provides and manages Retrofit APIs with dynamic base URL switching
 * and custom SSL handling based on network (local or public)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private var localRetrofit: Retrofit? = null
    private var publicRetrofit: Retrofit? = null
    private var currentBaseUrl: String = ""
    private var lastUsedBaseUrl: String = ""

    private var authApi: AuthApi? = null
    private var shoppingListApi: ShoppingListApi? = null
    private var shoppingItemApi: ShoppingItemApi? = null
    private var itemSetApi: ItemSetApi? = null
    private var userApi: UserApi? = null

    private var serverURL = "https://shopit.ddnss.de:8443/api/"
         //"https://192.168.1.38:8443/api/" // dev server on .38 (.23 WiFi) raspberryPi on .60

    // checks if device is in local network or not
    // !!! just for local networks starting with 192.168.1. !!!
    private fun isInLocalServer(): Boolean {
        val ip =
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .filter { it.name.startsWith("wlan") || it.name.startsWith("eth") }
                .flatMap { it.inetAddresses.asSequence() }
                .filter { !it.isLoopbackAddress && it is Inet4Address }
                .map { it.hostAddress }
                .firstOrNull() ?: ""
        return (ip.startsWith("192.168.188."))
    }

    // custom certificate handling
    private fun getSafeOkHttpClient(context: Context, ip: String): OkHttpClient {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = context.assets.open("mycert.crt").use {
            certificateFactory.generateCertificate(it)
        }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("backendserver", certificate)
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
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, _ -> hostname == ip }
            .build()
    }

    private fun createRetrofitInstance(context: Context, baseUrl: String): Retrofit {
        val gson: Gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getSafeOkHttpClient(context, baseUrl.split(":")[1].removePrefix("//")))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun getRetrofit(context: Context): Retrofit {
        return createRetrofitInstance(context, serverURL)
        /*
        return if (isInLocalServer()) {
            if (localRetrofit == null) {
                localRetrofit = createRetrofitInstance(
                    context,
                    serverURL
                )
            }
            localRetrofit!!
        } else {
            if (publicRetrofit == null) {
                publicRetrofit =
                    createRetrofitInstance(context, "https://dlmdla.ddnss.de:8443/api/")
            }
            publicRetrofit!!
        }

         */
    }

    // Updates all Retrofit API interfaces
    private fun updateApis(context: Context) {
        val retrofit = getRetrofit(context)
        authApi = retrofit.create(AuthApi::class.java)
        shoppingListApi = retrofit.create(ShoppingListApi::class.java)
        shoppingItemApi = retrofit.create(ShoppingItemApi::class.java)
        itemSetApi = retrofit.create(ItemSetApi::class.java)
        userApi = retrofit.create(UserApi::class.java)
    }

    fun getAuthApi(context: Context): AuthApi {
        /*
        // checks if api was created before and if the connection changed
        if (authApi == null || lastUsedBaseUrl != currentBaseUrl) {
            updateApis(context)
            lastUsedBaseUrl = currentBaseUrl
        }
        return authApi!!
         */
        return createRetrofitInstance(context, serverURL).create(AuthApi::class.java)
    }

    fun getShoppingListApi(context: Context): ShoppingListApi {
        /*
        // checks if api was created before and if the connection changed
        currentBaseUrl =
            if (isInLocalServer()) serverURL else "https://dlmdla.ddnss.de:8443/api/"
        if (shoppingListApi == null || lastUsedBaseUrl != currentBaseUrl) {
            updateApis(context)
            lastUsedBaseUrl = currentBaseUrl
        }
        return shoppingListApi!!
         */
        return createRetrofitInstance(context, serverURL).create(ShoppingListApi::class.java)
    }

    fun getShoppingItemApi(context: Context): ShoppingItemApi {
        /*
        // checks if api was created before and if the connection changed
        if (shoppingItemApi == null || lastUsedBaseUrl != currentBaseUrl) {
            updateApis(context)
            lastUsedBaseUrl = currentBaseUrl
        }
        return shoppingItemApi!!
         */
        return createRetrofitInstance(context, serverURL).create(ShoppingItemApi::class.java)
    }

    fun getItemSetApi(context: Context): ItemSetApi {
        /*
        // checks if api was created before and if the connection changed
        if (itemSetApi == null || lastUsedBaseUrl != currentBaseUrl) {
            updateApis(context)
            lastUsedBaseUrl = currentBaseUrl
        }
        return itemSetApi!!
         */
        return createRetrofitInstance(context, serverURL).create(ItemSetApi::class.java)
    }

    fun getUserApi(context: Context): UserApi {
        /*
        // checks if api was created before and if the connection changed
        if (itemSetApi == null || lastUsedBaseUrl != currentBaseUrl) {
            updateApis(context)
            lastUsedBaseUrl = currentBaseUrl
        }
        return userApi!!
         */
        return createRetrofitInstance(context, serverURL).create(UserApi::class.java)
    }
}