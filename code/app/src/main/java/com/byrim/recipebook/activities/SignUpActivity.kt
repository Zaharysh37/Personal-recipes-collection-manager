package com.byshnev.recipebook.activities

import android.content.ContentValues
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
import com.byshnev.recipebook.database.DatabaseHelper.Companion.CRED_LOGIN
import com.byshnev.recipebook.database.DatabaseHelper.Companion.CRED_PASSWORD
import com.byshnev.recipebook.database.DatabaseHelper.Companion.CRED_TABLE_NAME

class SignUpActivity : AppCompatActivity() {
    private lateinit var login: EditText
    private lateinit var password: EditText
    private lateinit var signInRef: TextView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up)

        login = findViewById(R.id.editTextLogin)
        password = findViewById(R.id.editTextPassword)
        signInRef = findViewById(R.id.signInRef)
        dbHelper = DatabaseHelper.getInstance(applicationContext)

        signInRef.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

        fun trySignUp(view: View) {
        val currentLogin = login.text.toString()
        val currentPassword = password.text.toString()

        db = dbHelper.writableDatabase
        var userCursor: Cursor? = db.rawQuery(
            "SELECT * FROM $CRED_TABLE_NAME "
                    + "WHERE $CRED_LOGIN = ?", arrayOf(currentLogin)
        )

        if (userCursor != null && userCursor.moveToFirst()) {
            Toast.makeText(this, "Аккаунт с таким логином уже существует", Toast.LENGTH_SHORT).show()
        } else {
            val values = ContentValues()
            values.put(CRED_LOGIN, currentLogin)
            values.put(CRED_PASSWORD, currentPassword)
            db.insert(CRED_TABLE_NAME, null, values)
            Toast.makeText(this, "Аккаунт успешно создан", Toast.LENGTH_SHORT).show()

            userCursor?.close() // Закрываем предыдущий курсор, если был открыт

            userCursor = db.rawQuery("SELECT ${DatabaseHelper.CRED_ID} "
                    + "FROM $CRED_TABLE_NAME "
                    + "WHERE $CRED_LOGIN = ?", arrayOf(currentLogin))

            if (userCursor.moveToFirst()) {
                val userId = userCursor.getLong(userCursor.getColumnIndexOrThrow(DatabaseHelper.CRED_ID))
                val intent = Intent(this, RecipesListActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }
            userCursor?.close()
        }
        db.close()
    }
}
