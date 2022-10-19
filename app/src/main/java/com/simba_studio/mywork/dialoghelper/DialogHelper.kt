package com.simba_studio.mywork.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.simba_studio.mywork.MainActivity
import com.simba_studio.mywork.R
import com.simba_studio.mywork.accounthelper.AccountHelper
import com.simba_studio.mywork.databinding.SignDialogBinding

class DialogHelper(val act: MainActivity) {
    val accHelper = AccountHelper(act)


    fun createSignDialog(index: Int){
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        setDialogState(index, rootDialogElement)

        val dialog = builder.create()
        rootDialogElement.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, rootDialogElement, dialog)
        }

        rootDialogElement.btForgetPass.setOnClickListener {
            setOnClickResetPassword(rootDialogElement, dialog)
        }

        rootDialogElement.btGoogleSignIn.setOnClickListener {
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {

        if(rootDialogElement.edSignEmail.text.isNotEmpty()){
            act.myAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    Toast.makeText(act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        } else {
            rootDialogElement.tvDialogMessage.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if(index == DialogConst.SIGN_UP_STATE){

            accHelper.signUpWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edPass.text.toString())

        } else {
            accHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edPass.text.toString())
        }
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if(index == DialogConst.SIGN_UP_STATE){
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.ac_sigh_up)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sigh_up_action)
        } else{
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.ac_sigh_in)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sigh_in_action)
            rootDialogElement.btForgetPass.visibility = View.VISIBLE
        }
    }
}