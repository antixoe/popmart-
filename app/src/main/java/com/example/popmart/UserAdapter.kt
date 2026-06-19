package com.example.popmart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class UserAdapter(
    private val onEdit: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val users = mutableListOf<User>()

    fun submitUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, onEdit, onDelete)
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val chipRole: Chip = itemView.findViewById(R.id.chipRole)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditUser)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteUser)

        fun bind(user: User, onEdit: (User) -> Unit, onDelete: (User) -> Unit) {
            tvUsername.text = user.username
            tvEmail.text = user.email
            chipRole.text = user.role
            
            btnEdit.setOnClickListener { onEdit(user) }
            btnDelete.setOnClickListener { onDelete(user) }
        }
    }
}
