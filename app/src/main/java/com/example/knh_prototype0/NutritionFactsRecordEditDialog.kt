package com.example.knh_prototype0

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.EditText

class NutritionFactsRecordEditDialog(context : Context, val data : NutritionFactsRecord) {
    private val dlg = Dialog(context)
    private lateinit var btnOK : Button
    private lateinit var btnCancel : Button

    lateinit var intakeText : EditText
    lateinit var fnameText : EditText
    var listener : OKClickedListener? = null

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.nutritionfacts_record_edit_dialog)
        dlg.setCancelable(false)

        fnameText = dlg.findViewById(R.id.dFNameText)
        intakeText = dlg.findViewById(R.id.dIntakeText)

        fnameText.setText(data.nutritionFacts.fname)
        intakeText.setText(data.intake.toString())

        btnOK = dlg.findViewById(R.id.ok)
        btnOK.setOnClickListener {
            listener?.onOKClicked(intakeText)
            dlg.dismiss()
        }

        btnCancel = dlg.findViewById(R.id.cancel)
        btnCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

    interface OKClickedListener {
        fun onOKClicked(intakeText: EditText)
    }
}
