import com.example.mission.R

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.text.get

// O Adapter recebe a lista de dados (que deve ser a mesma da Activity)
class AdapterDeRealizacoes(private val listaDeRealizacoes: List<String>,
                           private val onItemClick: (realizacao: String) -> Unit) :
    RecyclerView.Adapter<AdapterDeRealizacoes.MensagemViewHolder>() {

    /**
     * 1. ViewHolder: O "segurador de views"
     * É responsável por guardar as referências dos componentes (TextView, ImageView, etc.)
     * do seu list_item_message.xml para evitar que o Android as procure repetidamente.
     */


    inner class MensagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Encontra a TextView declarada no XML acima
        val mensagemText: TextView = itemView.findViewById(R.id.tvMensagem)

        // Adicionamos o clique no "init"
        init {
            itemView.setOnClickListener {
                // Pega a posição do item clicado
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Pega o texto daquela posição
                    val realizacaoClicada = listaDeRealizacoes[position]
                    // Chama a função de callback (que a Activity nos deu)
                    onItemClick(realizacaoClicada)
                }
            }
        }
    }

    /**
     * 2. onCreateViewHolder: Cria o layout do item.
     * Ocorre quando o RecyclerView precisa de um novo ViewHolder para representar um item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensagemViewHolder {
        // Infla o XML que você criou (item_mensagem.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_message, parent, false)
        return MensagemViewHolder(view)
    }

    /**
     * 3. onBindViewHolder: Preenche a View com os dados.
     * Ocorre sempre que o RecyclerView precisa mostrar dados em um ViewHolder existente.
     */
    override fun onBindViewHolder(holder: MensagemViewHolder, position: Int) {
        // Pega a mensagem correta na lista, usando a 'position' fornecida
        val mensagemAtual = listaDeRealizacoes[position]

        // Coloca o texto da mensagem na TextView do ViewHolder
        holder.mensagemText.text = mensagemAtual
    }

    /**
     * 4. getItemCount: Conta quantos itens existem na lista.
     * O RecyclerView usa isso para saber quantos itens deve desenhar.
     */
    override fun getItemCount(): Int {
        return listaDeRealizacoes.size
    }
}