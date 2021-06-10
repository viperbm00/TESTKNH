package com.example.knh_prototype0

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.EditText

//운동 기록 항목을 누르면 팝업되는 다이얼로그.
class ExerciceRecordEditDialog(context : Context, val data : ExerciseRecord) {

    private val dlg = Dialog(context)
    private lateinit var btnOK : Button
    private lateinit var btnCancel : Button

    lateinit var weightText : EditText
    lateinit var timeText : EditText
    lateinit var enameText : EditText
    var listener : OKClickedListener? = null

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.excersice_record_edit_dialog)
        dlg.setCancelable(false)

        enameText = dlg.findViewById(R.id.dENameText)
        weightText = dlg.findViewById(R.id.dWeightText)
        timeText = dlg.findViewById(R.id.dETimeText)

        enameText.setText(data.exercise.ename)
        weightText.setText(data.weight.toString())
        timeText.setText(data.etime.toString())

        btnOK = dlg.findViewById(R.id.ok)
        btnOK.setOnClickListener {
            listener?.onOKClicked(weightText, timeText)
            dlg.dismiss()
        }

        btnCancel = dlg.findViewById(R.id.cancel)
        btnCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

    interface OKClickedListener {
        fun onOKClicked(eweightText: EditText, etimeText: EditText)
    }
}