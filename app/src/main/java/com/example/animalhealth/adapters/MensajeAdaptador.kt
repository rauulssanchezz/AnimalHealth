package com.example.animalhealth.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.clases.Mensaje
import com.example.animalhealth.clases.Utilities

class MensajeAdaptador(private val lista_mensajes: List<Mensaje>, last_pos: Int) : RecyclerView.Adapter<MensajeAdaptador.MensajeViewHolder>() {
    private lateinit var contexto: Context
    private var last_pos = last_pos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val vista_item =
            LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje, parent, false)
        //Para poder hacer referencia al contexto de la aplicacion
        contexto = parent.context

        return MensajeViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        val item_actual = lista_mensajes[position]

        holder.pendientes.visibility = View.INVISIBLE

        if (last_pos<lista_mensajes.size-1&&last_pos!=1&&last_pos!=100000&&position==last_pos){
            holder.pendientes.visibility = View.VISIBLE
        }


        if(item_actual.id_emisor==item_actual.id_receptor){
            //ES MIO,ASIGNAR A LA DERECHA Y YO
            holder.otro.text=""
            holder.hora_otro.text=""
            holder.imagen_otro.visibility= View.INVISIBLE
            holder.imagen_yo.visibility= View.VISIBLE
            Glide.with(contexto)
                .load(item_actual.imagen_emisor)
                .apply(Utilities.glideOptions(contexto))
                .transition(Utilities.transition)
                .into(holder.imagen_yo)
            holder.hora_yo.text=item_actual.fecha_hora
            holder.yo.text=item_actual.contenido

        }else{
            //ES DE OTRO ASIGNAR A LA IZQUIERDA Y NOMBRE
            holder.yo.text=""
            holder.hora_yo.text=""
            holder.imagen_yo.visibility= View.INVISIBLE
            holder.imagen_otro.visibility= View.VISIBLE
            Glide.with(contexto)
                .load(item_actual.imagen_emisor)
                .apply(Utilities.glideOptions(contexto))
                .transition(Utilities.transition)
                .into(holder.imagen_otro)
            holder.hora_otro.text=item_actual.fecha_hora
            holder.otro.text=item_actual.contenido
        }




    }

    override fun getItemCount(): Int = lista_mensajes.size


    class MensajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val yo: TextView = itemView.findViewById(R.id.yo)
        val otro: TextView = itemView.findViewById(R.id.otro)
        val imagen_otro: ImageView = itemView.findViewById(R.id.imagen_otro)
        val imagen_yo: ImageView = itemView.findViewById(R.id.imagen_yo)
        val hora_yo: TextView = itemView.findViewById(R.id.hora_yo)
        val hora_otro: TextView = itemView.findViewById(R.id.hora_otro)
        val pendientes: ConstraintLayout = itemView.findViewById(R.id.pendientes_mens)
    }
}