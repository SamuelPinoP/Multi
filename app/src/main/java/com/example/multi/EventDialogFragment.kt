package com.example.multi

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class EventDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = requireArguments().getString(ARG_DATE) ?: ""
        return AlertDialog.Builder(requireContext())
            .setMessage(date)
            .setPositiveButton("Events") { _, _ ->
                val intent = Intent(requireContext(), EventsActivity::class.java)
                intent.putExtra(EXTRA_DATE, date)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        private const val ARG_DATE = "date"
        fun newInstance(date: String): EventDialogFragment {
            val f = EventDialogFragment()
            val args = Bundle()
            args.putString(ARG_DATE, date)
            f.arguments = args
            return f
        }
    }
}
