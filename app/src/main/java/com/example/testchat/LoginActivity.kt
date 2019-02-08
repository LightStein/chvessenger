package com.example.testchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login_login.setOnClickListener {
            LoginUser()
        }

        textView_login_register.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun LoginUser() {
        val email = editText_login_email.text.toString()
        val password = editText_login_password.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this@LoginActivity, "გთხოვთ შეიყვანოთ ელ-ფოსტა", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this@LoginActivity, "შეიყვანეთ პაროლი", Toast.LENGTH_SHORT).show()
        } else {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "წარმატებით შეხვედით", Toast.LENGTH_SHORT).show()
                        SendUserToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "ავტორიზაცია ჩაიშალა", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Log.d("LOGIN", "Login::: User login on login button() failed! \n ${it.message}")
                }
        }
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainIntent)
        finish()
    }
}
