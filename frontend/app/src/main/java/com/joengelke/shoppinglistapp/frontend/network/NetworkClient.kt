package com.joengelke.shoppinglistapp.frontend.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object NetworkClient {

    private fun getSafeOkHttpClient(context: Context): OkHttpClient {
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
            .hostnameVerifier { hostname, _ -> hostname == "192.168.1.38" } // 192.168.1.60 for RaspberryPi
            .build()
    }

    fun createRetrofit(context: Context): Retrofit {

        val gson: Gson = GsonBuilder().create()
        val baseUrl = "https://192.168.1.38:8443/api/" // https://192.168.1.60:8443/api/ for RaspberryPi
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getSafeOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}