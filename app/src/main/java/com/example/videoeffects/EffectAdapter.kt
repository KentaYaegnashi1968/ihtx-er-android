package com.example.videoeffects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EffectAdapter(
    private val items: MutableList<EffectConfig>,
    private val onRemove: (EffectConfig) -> Unit
) : RecyclerView.Adapter<EffectAdapter.EffectViewHolder>() {

    class EffectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.effectTitle)
        val meta: TextView = itemView.findViewById(R.id.effectMeta)
        val removeButton: Button = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_effect, parent, false)
        return EffectViewHolder(view)
    }

    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        val effect = items[position]
        val label = if (effect.isVideo) "Video • ${effect.name}" else "Audio • ${effect.name}"
        holder.title.text = label
        holder.meta.text = "repetitions=${effect.repetitions}, start=${effect.startMs}ms, duration=${effect.durationMs}ms"
        holder.removeButton.setOnClickListener { onRemove(effect) }
    }

    override fun getItemCount(): Int = items.size
}
