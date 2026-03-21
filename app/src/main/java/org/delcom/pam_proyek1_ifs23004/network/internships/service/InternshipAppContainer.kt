package org.delcom.pam_proyek1_ifs23004.network.internships.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.delcom.pam_proyek1_ifs23004.BuildConfig
import java.util.concurrent.TimeUnit

interface IInternshipAppContainer {
    val repository: IInternshipRepository
}

class InternshipAppContainer : IInternshipAppContainer {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    val okHttpClient = OkHttpClient.Builder().apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(loggingInterceptor)
        }
        connectTimeout(2, TimeUnit.MINUTES)
        readTimeout(2, TimeUnit.MINUTES)
        writeTimeout(2, TimeUnit.MINUTES)
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    private val retrofitService: InternshipApiService by lazy {
        retrofit.create(InternshipApiService::class.java)
    }

    override val repository: IInternshipRepository by lazy {
        InternshipRepository(retrofitService)
    }
}