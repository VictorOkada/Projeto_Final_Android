package com.example.listacompras.views

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.listacompras.databinding.ActivityMainBinding
import com.example.listacompras.databinding.ItemBinding
import com.example.listacompras.model.Produto
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.listacompras.R
import com.example.listacompras.databinding.CardItemBinding
import com.example.listacompras.services.API
import retrofit2.*

class MainActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tratarLogin()

        binding.swipeRefresh.setOnRefreshListener {
            atualizarProdutos()
        }
    }

    override fun onResume(){
        super.onResume()
        atualizarProdutos()
    }


    fun atualizarProdutos(){

        val callback = object: Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                binding.progressBar.visibility = View.GONE
                binding.shimmer.visibility = View.GONE
                binding.shimmer.visibility = View.INVISIBLE
                binding.container.visibility = View.VISIBLE
                binding.shimmer.stopShimmer()
                binding.swipeRefresh.isRefreshing = false

                if(response.isSuccessful){
                    val listaProdutos = response.body()
                    atualizarUI(listaProdutos)
                }
                else{
                    //val error = response.errorBody().toString()
                    Snackbar.make(binding.container, "Não foi possivel se conectar ao servidor",
                        Snackbar.LENGTH_LONG).show()

                    Log.e("ERROR", response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.shimmer.visibility = View.INVISIBLE
                binding.container.visibility = View.VISIBLE
                binding.shimmer.stopShimmer()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        API.produto.listar().enqueue(callback)

        binding.progressBar.visibility = View.VISIBLE
        binding.container.visibility = View.INVISIBLE
        binding.shimmer.visibility = View.VISIBLE
        binding.shimmer.startShimmer()
        binding.swipeRefresh.isRefreshing = true

    }

    fun novoItem(lista: List<Produto>?){
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Detalhes")
            .setView(editText)
            .setPositiveButton("OK") { dialog, button ->
                Snackbar.make(binding.container, "Conectado",
                    Snackbar.LENGTH_LONG).show()
                //lista?.forEach{
                    //val cardBinding = CardItemBinding.inflate(layoutInflater)
                    //cardBinding.textTitulo.text = it.nomeProduto
                    //cardBinding.textDesc.text = it.precProduto.toString()
                //}
            }
            .show()
    }

    fun atualizarUI(lista: List<Produto>?) {
        binding.container.removeAllViews()

        lista?.forEach {
                val cardBinding = CardItemBinding.inflate(layoutInflater)
                cardBinding.textTitulo.text = it.nomeProduto
                cardBinding.textDesc.text = it.precProduto.toString()

                val shimmer = Shimmer.ColorHighlightBuilder()
                    .setAutoStart(true)
                    .setDuration(1000)
                    //.setBaseColor(getColor(R.color.placeholder_grey))
                    .setHighlightColor(Color.WHITE)
                    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                    .build()

                val shimmerDrawable = ShimmerDrawable()
                shimmerDrawable.setShimmer(shimmer)

                Picasso.get().load(
                    "https://oficinacordova.azurewebsites.net/android/rest/produto/image/${it.idProduto}"
                ).placeholder(shimmerDrawable).into(cardBinding.imagem)

                binding.container.addView(cardBinding.root)

                val categoria = arrayListOf<String>(
                    "Escolha uma categoria",
                    "Alimentícios",
                    "Eletrodomésticos",
                    "Eletrônicos"
                )
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoria)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spinner.adapter = adapter

                binding.spinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            position: Int,
                            p3: Long
                        ) {
                            val callback = object : Callback<List<Produto>> {
                                override fun onResponse(
                                    call: Call<List<Produto>>,
                                    response: Response<List<Produto>>
                                ) {

                                    when (position) {
                                        1 -> API.produto.pesquisar2(categoria[0].toString())
                                        2 -> API.produto.pesquisar2(categoria[1].toString())
                                        3 -> API.produto.pesquisar2(categoria[2].toString())
                                        //else -> binding.image.setImageDrawable(null)


                                    }
                                }

                                override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                                }


                            }
                            val call = API.produto.pesquisar2("Glicopan pet")
                            call.enqueue(callback)


                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }
                    }

                //binding.btnDetalhes.setOnClickListener {

                //}

        }
    }


    fun tratarLogin(){
        if(FirebaseAuth.getInstance().currentUser == null){
            val provedores = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            val intent = AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(provedores)
                .build()

            startActivityForResult(intent, 1)

        }
        else{
            conectarFirebase()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1 && resultCode == RESULT_OK){
            Toast.makeText(this, "Autenticado", Toast.LENGTH_LONG).show()
            conectarFirebase()
        }
        else{
            finishAffinity()
        }
    }

    fun conectarFirebase(){
        val user = FirebaseAuth.getInstance().currentUser

        database = FirebaseDatabase.getInstance().reference.child(user.uid)

        val listener  = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                atualizarProdutos()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"Falha de conexao", Toast.LENGTH_LONG).show()
                Log.e("MainActivity", "conectarFirebase", error.toException())
            }

        }

        database.child("produtos").addValueEventListener(listener)
    }

}