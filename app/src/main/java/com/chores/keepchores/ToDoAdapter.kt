package com.chores.keepchores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chores.keepchores.databinding.ChoresItemViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ToDoAdapter(val context: Context, var list: List<ToDoModel>) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    val db by lazy {
        AppDatabase.getDatabase(context)
    }
    val homeScreenInstance = context as HomeScreen

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        // Binding choresItemViews & Inflating the layout
        val cardBinding = ChoresItemViewBinding.inflate(
                LayoutInflater.from(parent.context)
        )
        return ToDoViewHolder(cardBinding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(list[position])
        val isExpanded = list[position].isExpanded
        holder.viewBinding.expandableLinearLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.viewBinding.cardContainer.setOnClickListener {
            list[position].isExpanded = !list[position].isExpanded
            notifyItemChanged(position)
        }

        holder.viewBinding.descriptionCard.visibility = if (list[position].description.isEmpty()) View.GONE else View.VISIBLE

        holder.viewBinding.delete.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                db.toDoDao().deleteTask(list[position].id)
            }
            homeScreenInstance.cancelAlarm(position)
            notifyItemChanged(position)
        }

        holder.viewBinding.update.setOnClickListener {
            homeScreenInstance.updateViaBottomSheet(position)
        }

        holder.viewBinding.done.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                db.toDoDao().finishTask(list[position].id)
            }
            homeScreenInstance.cancelAlarm(position)
            notifyItemChanged(position)
        }
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    class ToDoViewHolder (val viewBinding: ChoresItemViewBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(toDoModel: ToDoModel) {
            viewBinding.titleCard.text = toDoModel.title
            viewBinding.descriptionCard.text = toDoModel.description
            updateDate(toDoModel.date)
            updateTime(toDoModel.time)
        }

        private fun updateDate(date: Long) {
            // Format: 02 September 2019 & Wednesday
            val date = Date(date)
            val sdf_date = SimpleDateFormat("dd MMMM yyyy")
            val sdf_day = SimpleDateFormat("EEEE")
            viewBinding.dateCard.text = sdf_date.format(date)
            viewBinding.dayCard.text = sdf_day.format(date)
        }

        private fun updateTime(time: Long) {
            // Format: 7:15 AM or 10:10 PM
            val sdf = SimpleDateFormat("h:mm a")
            viewBinding.timeCard.text = sdf.format(Date(time))
        }
    }
}