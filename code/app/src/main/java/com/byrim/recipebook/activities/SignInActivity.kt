package com.byshnev.recipebook.activities

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.byshnev.recipebook.R
import com.byshnev.recipebook.database.DatabaseHelper

class SignInActivity : AppCompatActivity() {

    private lateinit var login: EditText
    private lateinit var password: EditText
    private lateinit var createAccountRef: TextView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        login = findViewById(R.id.editTextLogin)
        password = findViewById(R.id.editTextPassword)
        createAccountRef = findViewById(R.id.signUpRef)
        dbHelper = DatabaseHelper.getInstance(applicationContext)

        createAccountRef.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        init {
            System.loadLibrary("recipebook")
        }
    }

    external fun stringsAreEqual(str1: String, str2: String): Boolean

    fun trySignIn(view: View) {
        db = dbHelper.writableDatabase
        val currentLogin = login.text.toString()
        val currentPassword = password.text.toString()

        val userCursor: Cursor? = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.CRED_TABLE_NAME} "
                    + "WHERE ${DatabaseHelper.CRED_LOGIN} = ?", arrayOf(currentLogin)
        )

        if (userCursor != null && userCursor.moveToFirst()) {
            val realPassword = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseHelper.CRED_PASSWORD))
            if (stringsAreEqual(realPassword, currentPassword)) {
                Toast.makeText(this, "Успешный вход", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RecipesListActivity::class.java)
                intent.putExtra("USER_ID", userCursor.getLong(userCursor.getColumnIndexOrThrow(
                    DatabaseHelper.CRED_ID)))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
        }
        userCursor?.close()
        db.close()
    }
}