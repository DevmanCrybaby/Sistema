package com.example.mission // Mantenha o pacote que já está no seu arquivo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import AdapterDeRealizacoes
import androidx.recyclerview.widget.ItemTouchHelper
import android.content.Intent
import androidx.core.content.ContextCompat
import android.graphics.Canvas
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.Collections

class Objectives : AppCompatActivity() {

    private val listaDeRealizacoes = mutableListOf<String>()

    // Declaramos o adapter aqui em cima para a classe toda enxergar
    private lateinit var adapter: AdapterDeRealizacoes

    // --- ADIÇÃO PARA O SWIPE ---
    // Carrega o ícone que você acabou de adicionar
    private val iconeExcluir by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_delete_sweep)!!
    }

    private val iconeEditar by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_edit)!!
    }
    private val fundoAzul = Color.BLUE.toDrawable()
    // Define a cor de fundo vermelha
    private val fundoVermelho = Color.RED.toDrawable()
    // -------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.objectives)

        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionar)
        val recyclerViewRealizacoes = findViewById<RecyclerView>(R.id.recyclerViewRealizacoes)

        // 1. Carrega os dados da memória ANTES de criar o adapter
        carregarLista()

        // 2. Configura o Adapter (agora ele já nasce com os dados carregados!)
        adapter = AdapterDeRealizacoes(listaDeRealizacoes) { realizacaoClicada ->
            // --- ISSO É O QUE ACONTECE QUANDO CLICAR ---

            // 1. Cria o "carteiro" (Intent) para a nova tela
            val intent = Intent(this, MainMissions::class.java)

            // 2. Coloca o "ID do Objetivo" na encomenda
            // Isso é a "chave dinâmica" que falamos!
            intent.putExtra("ID_OBJETIVO", realizacaoClicada)

            // 3. Envia o carteiro
            startActivity(intent)
        }
        recyclerViewRealizacoes.layoutManager = LinearLayoutManager(this)
        recyclerViewRealizacoes.adapter = adapter

        // 3. Configurar o clique
        fab.setOnClickListener {
            abrirDialogoDeRealizacao(null)
        }

        // 1. As Regras (O "Callback")
        // Aqui dizemos o que fazer quando arrastar ou deslizar
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, // Direções de ARRASTAR (cima e baixo)
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT// Direções de "SWIPE" (0 = desativado)
        ) {

            /** Chamado quando um item é arrastado e troca de lugar com outro */
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // 1. Pega as posições
                val posInicial = viewHolder.bindingAdapterPosition
                val posFinal = target.bindingAdapterPosition

                // 2. ATUALIZA A LISTA EM MEMÓRIA
                // (java.util.Collections é um atalho ótimo para trocar itens)
                Collections.swap(listaDeRealizacoes, posInicial, posFinal)

                // 3. AVISA O ADAPTER (para ele animar a mudança)
                adapter.notifyItemMoved(posInicial, posFinal)

                return true // Confirma que o movimento foi feito
            }

            /** Chamado quando você SOLTA o item que estava arrastando */
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                // ESSA É A PARTE MÁGICA:
                // Salvamos a nova ordem no SharedPreferences!
                salvarLista()
            }

            /** Chamado se o item for "arrastado para o lado" (swipe) */
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition // Pega a posição do item

                when (direction) {
                    // Caso para a ESQUERDA (Excluir)
                    ItemTouchHelper.LEFT -> {
                        listaDeRealizacoes.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        salvarLista()
                    }

                    // Caso para a DIREITA (Editar)
                    ItemTouchHelper.RIGHT -> {
                        // 1. Abre o diálogo de edição
                        abrirDialogoDeRealizacao(position)

                        // 2. Avisa o adapter para "resetar" a view do item
                        // Isso faz ele voltar ao normal depois do swipe
                        adapter.notifyItemChanged(position)
                    }
                }
            }

            // Chamado a cada frame do swipe, é aqui que desenhamos o ícone!
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, // O quanto o usuário arrastou no eixo X
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // Chama o "super" para que o item se mova na tela
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView

                // dX < 0 (Arrastando para a Esquerda = Excluir)
                if (dX < 0) {
                    fundoVermelho.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    fundoVermelho.draw(c)

                    val margemIcone = (itemView.height - iconeExcluir.intrinsicHeight) / 2
                    val iconeTop = itemView.top + margemIcone
                    val iconeBottom = iconeTop + iconeExcluir.intrinsicHeight
                    val iconeLeft = itemView.right - margemIcone - iconeExcluir.intrinsicWidth
                    val iconeRight = itemView.right - margemIcone

                    iconeExcluir.setBounds(iconeLeft, iconeTop, iconeRight, iconeBottom)
                    iconeExcluir.draw(c)

                    // dX > 0 (Arrastando para a Direita = Editar)
                } else if (dX > 0) {
                    fundoAzul.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    fundoAzul.draw(c)

                    val margemIcone = (itemView.height - iconeEditar.intrinsicHeight) / 2
                    val iconeTop = itemView.top + margemIcone
                    val iconeBottom = iconeTop + iconeEditar.intrinsicHeight
                    val iconeLeft = itemView.left + margemIcone
                    val iconeRight = itemView.left + margemIcone + iconeEditar.intrinsicWidth

                    iconeEditar.setBounds(iconeLeft, iconeTop, iconeRight, iconeBottom)
                    iconeEditar.draw(c)
                }
            }
        }

        // 2. O "Segurança" (O Helper)
        // Criamos o Ajudante e damos a ele as regras (callback)
        val itemTouchHelper = ItemTouchHelper(callback)

        // 3. Anexar ao RecyclerView
        // Dizemos ao segurança para começar a vigiar o seu RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerViewRealizacoes)
    }

    private fun salvarLista() {
        val sharedPreferences = getSharedPreferences("MinhaListaDeMissoes", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Transforma a lista ["Comer", "Dormir"] em uma string "Comer;;;Dormir"
        // Usamos ";;;" como separador para evitar bugs se você digitar uma vírgula no texto
        val textoParaSalvar = listaDeRealizacoes.joinToString(separator = ";;;")

        editor.putString("LISTA_SALVA", textoParaSalvar)
        editor.apply() // Salva de verdade
    }

    private fun carregarLista() {
        val sharedPreferences = getSharedPreferences("MinhaListaDeMissoes", MODE_PRIVATE)

        // Tenta ler. Se não tiver nada, retorna vazio ""
        val textoSalvo = sharedPreferences.getString("LISTA_SALVA", "") ?: ""

        if (textoSalvo.isNotEmpty()) {
            listaDeRealizacoes.clear() // Limpa para não duplicar

            // Quebra o textão de volta em pedacinhos usando o separador ";;;"
            val itens = textoSalvo.split(";;;")
            listaDeRealizacoes.addAll(itens)
        }
    }

    private fun abrirDialogoDeRealizacao(position: Int?) {
        val viewDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(viewDialogo)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val etMensagem = viewDialogo.findViewById<EditText>(R.id.etNovaMensagem)
        val btnSalvar = viewDialogo.findViewById<ImageButton>(R.id.btnSalvar)

        // --- LÓGICA DE EDIÇÃO ---
        // Se a posição NÃO for nula, estamos Editando.
        if (position != null) {
            // 1. Pega o texto atual...
            val textoAtual = listaDeRealizacoes[position]
            // 2. ...e coloca no EditText.
            etMensagem.setText(textoAtual)
        }
        // Se a posição FOR nula, o campo fica vazio (modo Adicionar)
        // -----------------------

        btnSalvar.setOnClickListener {
            val textoDigitado = etMensagem.text.toString()

            if (textoDigitado.isNotEmpty()) {

                // --- LÓGICA DE SALVAR ---
                if (position != null) {
                    // Modo EDIÇÃO: Atualiza o item na posição específica
                    listaDeRealizacoes[position] = textoDigitado
                    adapter.notifyItemChanged(position)
                    Toast.makeText(this, "Item atualizado!", Toast.LENGTH_SHORT).show()
                } else {
                    // Modo ADICIONAR: Adiciona no fim da lista
                    listaDeRealizacoes.add(textoDigitado)
                    adapter.notifyItemInserted(listaDeRealizacoes.size - 1)
                    Toast.makeText(this, "Salvo! Itens na lista: ${listaDeRealizacoes.size}", Toast.LENGTH_SHORT).show()
                }
                // ----------------------

                // Salva em ambos os casos
                salvarLista()
                dialog.dismiss()
            } else {
                etMensagem.error = "Digite algo!"
            }
        }

        dialog.show()
    }
}