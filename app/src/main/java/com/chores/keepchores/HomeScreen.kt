package com.chores.keepchores

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chores.keepchores.databinding.ActivityHomeScreenBinding
import com.chores.keepchores.databinding.DialogBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

const val DB_NAME = "keepchores.db"
const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
class HomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var viewModel: ToDoViewModel
    private lateinit var adapter: ToDoAdapter

    private var allTasks: List<ToDoModel> = emptyList()
    private var currentQuery: String = ""

    private var finalDate = 0L
    private var finalTime = 0L
    private var hourOfDay = 0
    private var minute = 0
    private lateinit var date: Calendar
    private lateinit var time: Calendar

    private lateinit var searchDebouncer: Debouncer<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Build repository + ViewModel via factory (testable, decoupled from Application)
        val dao = AppDatabase.getDatabase(applicationContext).toDoDao()
        val repository = ToDoRepository(dao)
        val factory = ToDoViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ToDoViewModel::class.java]

        adapter = ToDoAdapter(
            emptyList(),
            onFinish = { id -> viewModel.finishTask(id) },
            onDelete = { id -> viewModel.deleteTask(id) },
            onUpdateRequest = { pos -> updateViaBottomSheet(pos) }
        ).apply { setHasStableIds(true) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen)
            adapter = this@HomeScreen.adapter
        }

        viewModel.tasks.observe(this) { tasks ->
            allTasks = tasks
            applyFilterAndRender()
        }

        searchDebouncer = Debouncer(lifecycleScope, 300L)

        binding.editText.addTextChangedListener(SimpleAfterTextWatcher { text ->
            searchDebouncer.submit(text) { q ->
                currentQuery = q
                applyFilterAndRender()
            }
        })

        binding.fabAdd.setOnClickListener { openCreateBottomSheet() }
    }

    private fun applyFilterAndRender() {
        val filtered = if (currentQuery.isBlank()) allTasks else allTasks.filter { t ->
            t.title.contains(currentQuery, true) || t.description.contains(currentQuery, true)
        }
        binding.emptyAnim.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(filtered)
    }

    private fun openCreateBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val dialogBinding = DialogBottomSheetBinding.inflate(LayoutInflater.from(this))

        dialogBinding.setDate.setOnClickListener { DatePickerFragment().show(supportFragmentManager, "datePicker") }
        dialogBinding.setTime.setOnClickListener { TimePickerFragment().show(supportFragmentManager, "timePicker") }

        dialogBinding.addTaskBtn.setOnClickListener {
            val title = dialogBinding.title.text.toString()
            val description = dialogBinding.description.text.toString()
            when {
                title.isEmpty() -> toast("Title cannot be empty.")
                finalDate == 0L -> toast("Please set the date.")
                finalTime == 0L -> toast("Please set the time.")
                else -> {
                    viewModel.insertTask(ToDoModel(title, description, finalDate, finalTime))
                    bottomSheetDialog.dismiss()
                }
            }
        }

        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.show()
    }

    @SuppressLint("NewApi")
    fun onDateSet(setDate: Calendar) { date = setDate; finalDate = date.timeInMillis }
    @SuppressLint("NewApi")
    fun onTimeSet(setTime: Calendar, hourOfDay: Int, minute: Int) {
        time = setTime
        this.hourOfDay = hourOfDay
        this.minute = minute
        finalTime = time.timeInMillis
    }

    private fun applyTaskValuesForUpdate(task: ToDoModel) { finalDate = task.date; finalTime = task.time }

    fun updateViaBottomSheet(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val dialogBinding = DialogBottomSheetBinding.inflate(LayoutInflater.from(this))
        val task = adapter.getItem(position)
        applyTaskValuesForUpdate(task)

        dialogBinding.header.text = "Update Task"
        dialogBinding.title.setText(task.title)
        dialogBinding.description.setText(task.description)
        dialogBinding.addTaskBtn.text = "Update Task"

        dialogBinding.setDate.setOnClickListener { DatePickerFragment().show(supportFragmentManager, "datePicker") }
        dialogBinding.setTime.setOnClickListener { TimePickerFragment().show(supportFragmentManager, "timePicker") }

        dialogBinding.addTaskBtn.setOnClickListener {
            val title = dialogBinding.title.text.toString()
            val description = dialogBinding.description.text.toString()
            if (title.isEmpty()) {
                toast("Title cannot be empty.")
            } else {
                viewModel.updateTask(task.copy(title = title, description = description, date = finalDate, time = finalTime))
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.show()
    }

    override fun onDestroy() {
        // cancel pending debounce to avoid running after activity is gone
        if (this::searchDebouncer.isInitialized) searchDebouncer.cancel()
        super.onDestroy()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/*
@SuppressLint("NewApi")
private fun setAlarm(toDo: ToDoModel) {
    val intent = Intent(applicationContext, AlarmReceiver::class.java)
    val c = Calendar.getInstance().apply {
        timeInMillis = toDo.date
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    val alarmPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        toDo.id.toInt(), // use unique id if multiple alarms
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(alarmPendingIntent)
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, alarmPendingIntent)
}

@SuppressLint("NewApi")
private fun updateAlarm(toDo: ToDoModel) {
    // For now, same as setAlarm; kept separate for semantic clarity
    setAlarm(toDo)
}

private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

private fun cancelAlarm(toDo: ToDoModel) {
    val intent = Intent(applicationContext, AlarmReceiver::class.java)
    val alarmPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        toDo.id.toInt(),
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(alarmPendingIntent)
    alarmPendingIntent.cancel()
}
*/

private class SimpleAfterTextWatcher(val after: (String) -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: android.text.Editable?) { after(s?.toString() ?: "") }
}