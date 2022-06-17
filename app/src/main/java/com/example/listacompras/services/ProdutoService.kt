package com.example.listacompras.services

import com.example.listacompras.model.Produto
import retrofit2.Call
import retrofit2.http.*

interface ProdutoService {

    @GET("/android/rest/produto")
    fun listar(): Call<List<Produto>>

    @GET("/android/rest/produto/{nome}")
    fun pesquisar(@Path("nome") nome: String): Call<List<Produto>>

    @GET("/android/rest/produto")
    fun pesquisar2(@Query("nome") nome: String): Call<List<Produto>>

    @POST("/android/rest/produto")
    fun inserir(@Body produto: Produto): Call<Produto>

    @PUT("/android/rest/produto")
    fun atualizar(@Body produto: Produto,
        @Query("id") id: Int): Call<Produto>

    @DELETE("/android/rest/produto")
    fun excluir(@Query("id") id: Int): Call<Produto>
}