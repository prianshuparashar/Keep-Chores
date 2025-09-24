package com.chores.keepchores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chores.keepchores.databinding.ChoresItemViewBinding
import java.text.SimpleDateFormat
import java.util.*

class ToDoAdapter(
    private var list: List<ToDoModel>,
    private val onFinish: (Long) -> Unit,
    private val onDelete: (Long) -> Unit,
    private val onUpdateRequest: (Int) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    fun getItem(position: Int): ToDoModel = list[position]

    fun submitList(newList: List<ToDoModel>) {
        list = newList
        notifyDataSetChanged() // TODO replace with DiffUtil
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val cardBinding = ChoresItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ToDoViewHolder(cardBinding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
        holder.viewBinding.expandableLinearLayout.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

        holder.viewBinding.cardContainer.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }

        holder.viewBinding.descriptionCard.visibility = if (item.description.isEmpty()) View.GONE else View.VISIBLE

        holder.viewBinding.delete.setOnClickListener {
            onDelete(item.id)
        }

        holder.viewBinding.update.setOnClickListener {
            onUpdateRequest(position)
        }

        holder.viewBinding.done.setOnClickListener {
            onFinish(item.id)
        }
    }

    override fun getItemId(position: Int): Long = list[position].id

    class ToDoViewHolder (val viewBinding: ChoresItemViewBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(toDoModel: ToDoModel) {
            viewBinding.titleCard.text = toDoModel.title
            viewBinding.descriptionCard.text = toDoModel.description
            updateDate(toDoModel.date)
            updateTime(toDoModel.time)
        }

        private fun updateDate(dateLong: Long) {
            val date = Date(dateLong)
            val sdfDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val sdfDay = SimpleDateFormat("EEEE", Locale.getDefault())
            viewBinding.dateCard.text = sdfDate.format(date)
            viewBinding.dayCard.text = sdfDay.format(date)
        }

        private fun updateTime(timeLong: Long) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            viewBinding.timeCard.text = sdf.format(Date(timeLong))
        }
    }
}