package com.simba_studio.mywork.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.simba_studio.mywork.databinding.ProgressDialogLayoutBinding
import com.simba_studio.mywork.databinding.SignDialogBinding

object ProgressDialog {

    fun createProgressDialog(act: Activity): AlertDialog{
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}