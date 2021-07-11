package com.chores.keepchores

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chores.keepchores.databinding.ActivityHomeScreenBinding
import com.chores.keepchores.databinding.DialogBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DB_NAME = "keepchores.db"
const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
class HomeScreen : AppCompatActivity() {

    lateinit var binding: ActivityHomeScreenBinding
    val list = arrayListOf<ToDoModel>()
    var adapter = ToDoAdapter(this, list)
    var finalDate = 0L
    var finalTime = 0L
    var hourOfDay = 0
    var minute = 0
    lateinit var date: Calendar
    lateinit var time: Calendar
    val db by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Custom Toolbar as the App Bar for the activity
        setSupportActionBar(binding.toolbar)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen)
            adapter = this@HomeScreen.adapter
        }

        db.toDoDao().getTask().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                binding.emptyAnim.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }else{
                list.clear()
                binding.emptyAnim.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }
        })

        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do Nothing
            }

            override fun afterTextChanged(s: Editable?) {
                filteredDisplay(s.toString())
                binding.editText.clearFocus()
            }
        })

        binding.fabAdd.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

            // Binding Dialog Views & Inflating the layout
            val dialogBottomSheetBinding = DialogBottomSheetBinding.inflate(
                    LayoutInflater.from(applicationContext)
            )

            val db by lazy {
                AppDatabase.getDatabase(this)
            }

            // Setting up the onClickListeners and actions
            dialogBottomSheetBinding.setDate.setOnClickListener {
                DatePickerFragment().show(supportFragmentManager, "datePicker")
            }
            dialogBottomSheetBinding.setTime.setOnClickListener {
                TimePickerFragment().show(supportFragmentManager, "timePicker")
            }
            dialogBottomSheetBinding.addTaskBtn.setOnClickListener {
                val title = dialogBottomSheetBinding.title.text.toString()
                val description = dialogBottomSheetBinding.description.text.toString()

                when {
                    title.isEmpty() -> {
                        Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show()
                    }
                    finalDate == 0L -> {
                        Toast.makeText(this, "Please set the date.", Toast.LENGTH_SHORT).show()
                    }
                    finalTime == 0L -> {
                        Toast.makeText(this, "Please set the time.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val toDo = ToDoModel(title, description, finalDate, finalTime)
                        GlobalScope.launch(Dispatchers.Main) {
                            val id = withContext(Dispatchers.IO) {
                                return@withContext db.toDoDao().insertTask(toDo)
                            }
                        }

                        setAlarm(toDo)

                        // Dismissing the dialog after adding task
                        bottomSheetDialog.dismiss()
                    }
                }
            }

            bottomSheetDialog.setContentView(dialogBottomSheetBinding.root)
            bottomSheetDialog.show()
        }

        createNotificationChannel()
    }

    @SuppressLint("NewApi")
    private fun setAlarm(toDo: ToDoModel) {
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val c = Calendar.getInstance()
        c.apply {
            timeInMillis = toDo.date
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val alarmPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
        Log.d("AlarmTag", list.indexOf(toDo).toString() + " : SET")
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, alarmPendingIntent)
    }

    private fun filteredDisplay(keyword: String) {
        db.toDoDao().getTask().observe(this, Observer {
            if (it.isNotEmpty()) {
                list.clear()
                list.addAll(it.filter {
                    toDo -> toDo.title.contains(keyword, true) || toDo.description.contains(keyword, true)
                })
                adapter.notifyDataSetChanged()
            }
        })
    }

    @SuppressLint("NewApi")
    fun onDateSet(setDate: Calendar) {
        date = setDate
        finalDate = date.timeInMillis
    }

    @SuppressLint("NewApi")
    fun onTimeSet(setTime: Calendar, hourOfDay: Int, minute: Int) {
        time = setTime
        this.hourOfDay = hourOfDay
        this.minute = minute
        finalTime = time.timeInMillis
    }

    fun updateViaBottomSheet(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

        // Binding Dialog Views & Inflating the layout
        val dialogBottomSheetBinding = DialogBottomSheetBinding.inflate(
                LayoutInflater.from(applicationContext)
        )

        val db by lazy {
            AppDatabase.getDatabase(this)
        }

        // Update Texts & Set up the onClickListeners and actions
        dialogBottomSheetBinding.header.text = "Update Task"
        dialogBottomSheetBinding.title.setText(list[position].title)
        dialogBottomSheetBinding.description.setText(list[position].description)
        dialogBottomSheetBinding.addTaskBtn.text = "Update Task"
        finalDate = list[position].date
        finalTime = list[position].time

        dialogBottomSheetBinding.setDate.setOnClickListener {
            DatePickerFragment().show(supportFragmentManager, "datePicker")
        }
        dialogBottomSheetBinding.setTime.setOnClickListener {
            TimePickerFragment().show(supportFragmentManager, "timePicker")
        }
        dialogBottomSheetBinding.addTaskBtn.setOnClickListener {
            val title = dialogBottomSheetBinding.title.text.toString()
            val description = dialogBottomSheetBinding.description.text.toString()

            when {
                title.isEmpty() -> {
                    Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val toDo = ToDoModel(title, description, finalDate, finalTime, id = list[position].id)
                    GlobalScope.launch(Dispatchers.IO) {
                        db.toDoDao().updateTask(toDo)
                    }

                    updateAlarm(toDo)

                    // Dismissing the dialog after adding task
                    bottomSheetDialog.dismiss()
                }
            }
        }

        bottomSheetDialog.setContentView(dialogBottomSheetBinding.root)
        bottomSheetDialog.show()
    }

    @SuppressLint("NewApi")
    private fun updateAlarm(toDo: ToDoModel) {
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val c = Calendar.getInstance()
        c.apply {
            timeInMillis = toDo.date
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val alarmPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, alarmPendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun cancelAlarm(position: Int) {
        val toDo = list[position]
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d("AlarmTag", toDo.id.toInt().toString() + " : CANCEL")
        alarmManager.cancel(alarmPendingIntent)
        alarmPendingIntent.cancel()
    }
}