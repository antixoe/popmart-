package com.example.popmart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat

class PopmartAdapter(
    private val onEdit: (PopmartItem) -> Unit,
    private val onDelete: (PopmartItem) -> Unit,
    private val canEdit: Boolean
) : RecyclerView.Adapter<PopmartAdapter.PopmartViewHolder>() {

    private val items = mutableListOf<PopmartItem>()
    private val currencyFormatter = NumberFormat.getCurrencyInstance()

    fun submitItems(newItems: List<PopmartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopmartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_popmart, parent, false)
        return PopmartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopmartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PopmartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvSeries: TextView = itemView.findViewById(R.id.tvSeries)
        private val tvRarity: TextView = itemView.findViewById(R.id.tvRarity)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvUnitPrice: TextView = itemView.findViewById(R.id.tvUnitPrice)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: PopmartItem) {
            tvName.text = item.name
            tvSeries.text = itemView.context.getString(R.string.series_format, item.series)
            tvRarity.text = itemView.context.getString(R.string.rarity_format, item.rarity)
            tvDate.text = itemView.context.getString(R.string.date_format, item.dateAcquired)
            tvQuantity.text = itemView.context.getString(R.string.quantity_format, item.quantity)
            tvUnitPrice.text = itemView.context.getString(
                R.string.unit_price_format,
                currencyFormatter.format(item.unitPrice)
            )
            val notesText = item.notes.ifBlank { itemView.context.getString(R.string.notes_empty) }
            tvNotes.text = itemView.context.getString(R.string.notes_format, notesText)

            // Hide edit/delete buttons if the user doesn't have permission
            btnEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
            btnDelete.visibility = if (canEdit) View.VISIBLE else View.GONE

            btnEdit.setOnClickListener { onEdit(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
