package com.example.listacompras.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object API {
    private val retrofit: Retrofit
        get(){
            val okHttp = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit
                .Builder()
                .baseUrl("https://oficinacordova.azurewebsites.net")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttp)
                .build()
        }

    val produto: ProdutoService
        get(){
            return retrofit.create(ProdutoService::class.java)
        }

}