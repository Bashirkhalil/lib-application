package com.example.testlib

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object RetrofitClient {
//    private var retrofit: Retrofit? = null
//
//    fun getClient(baseUrl: String): Retrofit {
//        if (retrofit == null) {
//            retrofit = Retrofit.Builder()
//                .baseUrl(baseUrl)  // This can be a dummy URL because we’ll use dynamic URL download.
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        }
//        return retrofit!!
//    }
//}

object RetrofitClient {
    fun getClient(baseUrl: String): Retrofit {

        val client = OkHttpClient.Builder()
            .addInterceptor(DownloadInterceptor()) // Add the interceptor here
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

class DownloadInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Modify the request to set the Accept header and User-Agent
        val newRequest = originalRequest.newBuilder()
            .addHeader("Accept", "application/octet-stream")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .build()

        return chain.proceed(newRequest)
    }
}
