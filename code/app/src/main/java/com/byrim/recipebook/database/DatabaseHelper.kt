package com.byshnev.recipebook.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, SCHEMA)  {
    companion object {
        const val DB_NAME: String = "recipe_book.db"
        const val SCHEMA: Int = 1

        const val CRED_TABLE_NAME = "credentials"
        const val CRED_ID = "id"
        const val CRED_LOGIN = "login"
        const val CRED_PASSWORD = "password"

        const val RCP_TABLE_NAME = "recipes"    // Название таблицы рецептов блюд
        const val RCP_ID = "id"
        const val RCP_NAME = "name"
        const val RCP_TIME = "cooking_time"
        const val RCP_PORTIONS_AMOUNT = "portions_amount"
        const val RCP_RECIPE_INSTRUCTIONS = "recipe"
        const val RCP_USER_ID = "user_id"

        private var instance: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context.applicationContext)
            }
            return instance!!
        }

        const val INGR_TABLE_NAME = "ingredients"
        const val INGR_ID = "id"
        const val INGR_NAME = "name"
        const val INGR_RECIPE_ID = "recipe_id"

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $CRED_TABLE_NAME (" +
                "$CRED_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$CRED_LOGIN TEXT, " +
                "$CRED_PASSWORD TEXT);")

        db.execSQL("CREATE TABLE $RCP_TABLE_NAME (" +
                "$RCP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$RCP_NAME TEXT, " +
                "$RCP_PORTIONS_AMOUNT INTEGER, " +
                "$RCP_TIME TEXT, " +
                "$RCP_RECIPE_INSTRUCTIONS TEXT, " +
                "$RCP_USER_ID INTEGER, " +
                "FOREIGN KEY($RCP_USER_ID) REFERENCES $CRED_TABLE_NAME($CRED_ID));")

        db.execSQL("CREATE TABLE $INGR_TABLE_NAME (" +
                "$INGR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$INGR_NAME TEXT, " +
                "$INGR_RECIPE_ID INTEGER, " +
                "FOREIGN KEY($INGR_RECIPE_ID) REFERENCES $RCP_TABLE_NAME($RCP_ID));")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
}