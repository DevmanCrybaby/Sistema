package com.example.mission

import AdapterDeRealizacoes
import androidx.recyclerview.widget.ItemTouchHelper
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Data class for Mission with Name and Score
data class Missao(var nome: String, var pontos: Int)

class MainActivity : AppCompatActivity() {

    private val listaDeMissoes = mutableListOf<Missao>()
    private lateinit var adapter: AdapterDeRealizacoes
    private lateinit var scoreTextView: TextView

    private val PREFS_NAME = "MissionPrefs"
    private val KEY_LISTA_COMPLETA = "LISTA_COMPLETA"
    private val KEY_SCORE = "score_text"

    //Para o Swipe
    private val fundoAzul = Color.BLUE.toDrawable()
    // Define a cor de fundo vermelha
    private val fundoVermelho = Color.RED.toDrawable()
    private val iconeExcluir by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_delete_sweep)!!
    }
    private val iconeEditar by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_edit)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreTextView = findViewById(R.id.scoreView)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMainActivity)
        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionarMissao)
        val btnObjectives = findViewById<android.widget.Button>(R.id.button_main_line)


        carregarDados()

        // We use a modified version of the adapter or handle the string conversion
        // For now, let's use the strings to keep it simple as you did before,
        // but we'll format it as "Name - Score pts"
        val listaStrings = listaDeMissoes.map { "${it.nome} - ${it.pontos} pts" }.toMutableList()

        adapter = AdapterDeRealizacoes(listaStrings) { itemClicado ->
            // Logic for clicking an item (maybe mark as done?)
            marcarComoConcluida(itemClicado)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            abrirDialogoMissao()
        }

        btnObjectives.setOnClickListener {
            startActivity(Intent(this, Objectives::class.java))
        }

        // ... existing setup (adapter, layoutManager) ...

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
                java.util.Collections.swap(listaDeMissoes, posInicial, posFinal)

                // 3. AVISA O ADAPTER (para ele animar a mudança)
                adapter.notifyItemMoved(posInicial, posFinal)

                return true // Confirma que o movimento foi feito
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition // Pega a posição do item

                when (direction) {
                    // Caso para a ESQUERDA (Excluir)
                    ItemTouchHelper.LEFT -> {
                        listaDeMissoes.removeAt(position) // Remove da Activity
                        adapter.removeItem(position)     // Remove do Adapter e notifica
                        salvarDados()
                    }

                    // Caso para a DIREITA (Editar)
                    ItemTouchHelper.RIGHT -> {
                        // 1. Abre o diálogo de edição
                        abrirDialogoMissao(position)

                        // 2. Avisa o adapter para "resetar" a view do item
                        // Isso faz ele voltar ao normal depois do swipe
                        adapter.notifyItemChanged(position)
                    }
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                // Save the new order to SharedPreferences once the drag is finished
                salvarDados()
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

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun carregarDados() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        scoreTextView.text = prefs.getInt(KEY_SCORE, 0).toString()

        val textoSalvo = prefs.getString(KEY_LISTA_COMPLETA, "") ?: ""
        if (textoSalvo.isNotEmpty()) {
            listaDeMissoes.clear()
            textoSalvo.split(";;;").forEach {
                val partes = it.split("|")
                if (partes.size == 2) {
                    listaDeMissoes.add(Missao(partes[0], partes[1].toInt()))
                }
            }
        }
    }

    private fun salvarDados() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val textoParaSalvar = listaDeMissoes.joinToString(";;;") { "${it.nome}|${it.pontos}" }

        prefs.edit()
            .putString(KEY_LISTA_COMPLETA, textoParaSalvar)
            .putInt(KEY_SCORE, scoreTextView.text.toString().toInt())
            .apply()

        // Notify Widget
        val intent = Intent(this, MissionWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        sendBroadcast(intent)
    }

    // Recebe uma posição opcional. Se não passar nada, ela é nula (modo criar)
    private fun abrirDialogoMissao(posicao: Int? = null) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_w_points, null)

        val etNome = dialogLayout.findViewById<EditText>(R.id.etNovaMensagem)
        val etPontos = dialogLayout.findViewById<EditText>(R.id.etPontos)
        val btnSalvar = dialogLayout.findViewById<ImageButton>(R.id.btnSalvar)

        // LÓGICA DE PREENCHIMENTO (Se for Edição)
        if (posicao != null) {
            val missaoExistente = listaDeMissoes[posicao]
            etNome.setText(missaoExistente.nome)
            etPontos.setText(missaoExistente.pontos.toString())
        }

        val dialog = builder.setView(dialogLayout).create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString()
            val pontosStr = etPontos.text.toString()

            if (nome.isNotEmpty() && pontosStr.isNotEmpty()) {
                val pontos = pontosStr.toInt()

                if (posicao == null) {
                    // MODO CRIAR: Adiciona um novo
                    listaDeMissoes.add(Missao(nome, pontos))
                } else {
                    // MODO EDITAR: Atualiza o existente na posição correta
                    val missaoEditada = listaDeMissoes[posicao]
                    missaoEditada.nome = nome
                    missaoEditada.pontos = pontos
                    // Nota: Não precisamos dar "add", pois já alteramos o objeto na memória
                }

                atualizarInterface()
                dialog.dismiss()
            } else {
                if (nome.isEmpty()) etNome.error = "Digite a missão"
                if (pontosStr.isEmpty()) etPontos.error = "Digite os pontos"
            }
        }

        dialog.show()

        // Se estiver editando e o usuário cancelar/fechar o diálogo sem salvar,
        // precisamos avisar o adapter para redesenhar o item (tirar o fundo azul do swipe)
        dialog.setOnDismissListener {
            if (posicao != null) {
                adapter.notifyItemChanged(posicao)
            }
        }
    }

    private fun marcarComoConcluida(itemTexto: String) {
        // Find the mission, add points, remove from list
        val nomeMissao = itemTexto.split(" - ")[0]
        val missao = listaDeMissoes.find { it.nome == nomeMissao }

        if (missao != null) {
            val pontosAtuais = scoreTextView.text.toString().toInt()
            scoreTextView.text = (pontosAtuais + missao.pontos).toString()
            listaDeMissoes.remove(missao)
            atualizarInterface()
            Toast.makeText(this, "Missão concluída!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarListaNoAdapter() {
        val field = adapter.javaClass.getDeclaredField("listaDeRealizacoes")
        field.isAccessible = true
        val listInAdapter = field.get(adapter) as MutableList<String>
        listInAdapter.clear()
        listInAdapter.addAll(listaDeMissoes.map { "${it.nome} - ${it.pontos} pts" })
    }

    private fun atualizarInterface() {
        atualizarListaNoAdapter()
        adapter.notifyDataSetChanged()
        salvarDados()
    }
}