package com.chores.keepchores

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    @SuppressLint("NewApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog, set the minimum date (if required) and return it
        val datePickerDialog = DatePickerDialog(this.requireActivity(), this, year, month, day)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        return datePickerDialog
    }

    @SuppressLint("NewApi")
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, day)
        val homeScreenInstance = activity as HomeScreen
        homeScreenInstance.onDateSet(c)
    }
}