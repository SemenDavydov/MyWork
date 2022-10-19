package com.simba_studio.mywork.accounthelper


import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.simba_studio.mywork.Constans.FirebaseAuthConstans
import com.simba_studio.mywork.MainActivity
import com.simba_studio.mywork.R

class AccountHelper(act: MainActivity) {
    private val act = act
    private lateinit var googleSignInClient:GoogleSignInClient

    /////////////////////////Функция регистрации////////////////////////////////////////////////////
    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {task ->
                if(task.isSuccessful){
                    act.myAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signUpWithEmailSuccessful(task.result.user!!)
                            } else {
                                signUpWithEmailErrors(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser){
        sendEmailVerification(user)
        act.userInterfaceUpdate(user)
    }

    private fun signUpWithEmailErrors(ex: java.lang.Exception, email: String, password: String){
        if (ex is FirebaseAuthUserCollisionException) {
            if (ex.errorCode == FirebaseAuthConstans.ERROR_EMAIL_ALREADY_IN_USE) {
                linkEmailToG(email, password)
            }
        } else if (ex is FirebaseAuthInvalidCredentialsException) {
            if (ex.errorCode == FirebaseAuthConstans.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstans.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
            }
        }
        if (ex is FirebaseAuthWeakPasswordException) {
            val exeption = ex as FirebaseAuthWeakPasswordException
            if (exeption.errorCode == FirebaseAuthConstans.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstans.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
            }
        }
    }

    ///////////////////////Функция Входа в аккаунт////////////////////////////////////////////////
    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.myAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.userInterfaceUpdate(task.result?.user)
                            } else {
                                signInWithEmailErrors(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailErrors(ex: java.lang.Exception, email: String, password: String){
        if(ex is FirebaseAuthInvalidCredentialsException){
            if(ex.errorCode == FirebaseAuthConstans.ERROR_INVALID_EMAIL){
                Toast.makeText(act, FirebaseAuthConstans.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
            } else if(ex.errorCode == FirebaseAuthConstans.ERROR_WRONG_PASSWORD){
                Toast.makeText(act, FirebaseAuthConstans.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG).show()
            }
        }else if(ex is FirebaseAuthInvalidUserException){
            if(ex.errorCode == FirebaseAuthConstans.ERROR_USER_NOT_FOUND){
                Toast.makeText(act, FirebaseAuthConstans.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun linkEmailToG(email:String, password: String){
        val credential = EmailAuthProvider.getCredential(email, password)
        if(act.myAuth.currentUser != null) {
            act.myAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, act.resources.getString(R.string.link_done), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(act,act.resources.getString(R.string.enter_to_g), Toast.LENGTH_LONG).show()
        }
    }

    //////////////////////Функция получения данных с помощью Гугл сервисов///////////////////////
    private fun getSignInClient():GoogleSignInClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)
    }


    ////////////////////////////////////////Функция для входа с помощью Гугл///////////////////
    fun signInWithGoogle(){
        googleSignInClient = getSignInClient()
        val intent = googleSignInClient.signInIntent
        act.googleSignInLauncher.launch(intent)
    }

    fun signOutG(){
        getSignInClient().signOut()
    }

    ///////////////////////////////////////Функция для регистрации на Firebase//////////////////
    fun signInFirebaseWithGoogle(token:String){
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.myAuth.currentUser?.delete()?.addOnCompleteListener {task ->
            if(task.isSuccessful){
                Toast.makeText(act, "Получение данных",Toast.LENGTH_LONG).show()
                act.myAuth.signInWithCredential(credential).addOnCompleteListener { task2 ->
                    if(task.isSuccessful){
                        Toast.makeText(act, "Вы вошли успешно", Toast.LENGTH_LONG).show()
                        act.userInterfaceUpdate(task2.result?.user)
                    } else {
                        Toast.makeText(act, "Google Sign In Exception : ${task2.exception}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(act, "Ошибка получения данных",Toast.LENGTH_LONG).show()
            }
        }
    }


    ////////////////////Функция для верификации аккаунта с помощью подтверждения по ссылке////
    private fun sendEmailVerification(user:FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(act, act.resources.getString(R.string.send_verification_done), Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(act, act.resources.getString(R.string.send_verification_email_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInAnonymously(listener: Listener){
        act.myAuth.signInAnonymously().addOnCompleteListener { task ->
            if(task.isSuccessful){
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как Гость", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(act, "Не удалось войти как Гость", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface Listener{
        fun onComplete()
    }
}
