package com.byshnev.recipebook.activities

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byshnev.recipebook.R
import com.byshnev.recipebook.adapters.IngredientsListShowAdapter
import com.byshnev.recipebook.database.DatabaseHelper
import com.byshnev.recipebook.database.DatabaseHelper.Companion.INGR_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.INGR_RECIPE_ID
import com.byshnev.recipebook.database.DatabaseHelper.Companion.INGR_TABLE_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_ID
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_PORTIONS_AMOUNT
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_RECIPE_INSTRUCTIONS
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_TABLE_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_TIME

class RecipeViewActivity : AppCompatActivity() {

    private lateinit var dishName: TextView
    private lateinit var timeOfCooking: TextView
    private lateinit var portionsNumber: TextView
    private lateinit var ingredientsListView: RecyclerView
    private lateinit var recipeInstructions: TextView

    private lateinit var ingredientsAdapter: IngredientsListShowAdapter
    private var ingredientsList: MutableList<String> = mutableListOf()

    private var userId: Long = -1
    private var recipeId: Long = -1
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_view)
        userId = intent.getLongExtra("USER_ID", -1)
        recipeId = intent.getLongExtra("RECIPE_ID", -1)
        // Проверяем, что USER_ID получен корректно
        if (userId == (-1).toLong()) {
            Toast.makeText(this, "Ошибка: ID пользователя не передан", Toast.LENGTH_SHORT).show()
            // TODO(сделать нормальный выход)
        }
        if (recipeId == (-1).toLong()) {
            Toast.makeText(this, "Ошибка: ID рецепта не передан", Toast.LENGTH_SHORT).show()
            // TODO(сделать нормальный выход)
        }
        dbHelper = DatabaseHelper.getInstance(applicationContext)

        initializeInterface()
        showRecipe()
    }

    private fun initializeInterface() {
        dishName = findViewById(R.id.dishName)
        timeOfCooking = findViewById(R.id.cookingTime)
        portionsNumber = findViewById(R.id.portionsAmount)
        recipeInstructions = findViewById(R.id.cookingInstructions)

        ingredientsListView = findViewById(R.id.ingredientsList)
        ingredientsAdapter = IngredientsListShowAdapter(this, ingredientsList)
        ingredientsListView.layoutManager = LinearLayoutManager(this)
        ingredientsListView.adapter = ingredientsAdapter
    }

    private fun showRecipe() {
        db = dbHelper.readableDatabase
        var cursor = db.rawQuery("SELECT * FROM $RCP_TABLE_NAME WHERE $RCP_ID=?", arrayOf(recipeId.toString()))
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Ошибка: не получилось извлечь рецепт из базы данных", Toast.LENGTH_SHORT).show()
            finish()
        }
        dishName.text = cursor.getString(cursor.getColumnIndexOrThrow(RCP_NAME))
        timeOfCooking.append(cursor.getString(cursor.getColumnIndexOrThrow(RCP_TIME)))
        portionsNumber.append(cursor.getLong(cursor.getColumnIndexOrThrow(RCP_PORTIONS_AMOUNT)).toString())
        recipeInstructions.text = cursor.getString(cursor.getColumnIndexOrThrow(RCP_RECIPE_INSTRUCTIONS))
        cursor.close()
        cursor = db.rawQuery("SELECT * FROM $INGR_TABLE_NAME WHERE $INGR_RECIPE_ID=?", arrayOf(recipeId.toString()))
        if (cursor.moveToFirst()) {
            do {
                ingredientsList.add(cursor.getString(cursor.getColumnIndexOrThrow(INGR_NAME)))
            } while(cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    fun goToRecipeEditing(view: View) {
        val intent = Intent(this, RecipeEditActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivity(intent)
    }

    fun exit(view: View) {
        finish()
    }
}