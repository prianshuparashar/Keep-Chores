package com.chores.keepchores

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireActivity(), this, year, month, day)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance().apply { set(year, month, day) }
        (activity as? HomeScreen)?.onDateSet(c)
    }
}