package com.byshnev.recipebook.activities

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byshnev.recipebook.R
import com.byshnev.recipebook.adapters.IngredientsListEditAdapter
import com.byshnev.recipebook.database.DatabaseHelper

class RecipeCreateActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editPortionsAmount: EditText
    private lateinit var editCookingTime: EditText
    private lateinit var ingredientsList: RecyclerView
    private lateinit var newIngredientInput: EditText
    private lateinit var addIngredientButton: ImageView
    private lateinit var editInstructions: EditText
    private lateinit var saveButton: TextView
    private lateinit var exitButton: TextView

    private var ingredients: MutableList<String> = mutableListOf()
    private lateinit var adapter: IngredientsListEditAdapter

    private var userId: Long = -1
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_create)

        userId = intent.getLongExtra("USER_ID", -1).toLong()
        // Проверяем, что USER_ID получен корректно
        if (userId == (-1).toLong()) {
            Toast.makeText(this, "Ошибка: User ID не передан", Toast.LENGTH_SHORT).show()
            // TODO(сделать нормальный выход)
        }

        dbHelper = DatabaseHelper.getInstance(applicationContext)

        initializeInterface()
    }

    private fun initializeInterface() {
        editName = findViewById(R.id.dishNameInput)
        editCookingTime = findViewById(R.id.cookingTimeInput)
        editPortionsAmount = findViewById(R.id.portionsAmountInput)
        addIngredientButton = findViewById(R.id.addIngredientButton)
        editInstructions = findViewById(R.id.recipeInstructionsInput)
        newIngredientInput = findViewById(R.id.newIngredientInput)

        saveButton = findViewById(R.id.editDishButton)
        saveButton.setOnClickListener { saveRecipe(saveButton) }

        exitButton = findViewById(R.id.exitButton)
        exitButton.setOnClickListener { exit(exitButton) }

        initializeRecyclerView()

        // Добавление в список ингредиентами
        addIngredientButton.setOnClickListener {
            // Показать строку для ввода ингредиента, если она не видима
            if (!newIngredientInput.isVisible) {
                newIngredientInput.visibility = View.VISIBLE
            } else {
                val newIngredient = newIngredientInput.text.toString()
                if (newIngredient.isNotEmpty()) {
                    // Добавить новый ингредиент в список
                    ingredients.add(newIngredient)
                    adapter.notifyItemInserted(ingredients.size - 1)
                    newIngredientInput.text.clear()
                }
            }
            newIngredientInput.requestFocus()
        }
        // Обработать добавление ингредиента
        newIngredientInput.setOnFocusChangeListener { _, hasFocus ->
            if (newIngredientInput.isVisible && !hasFocus) {
                val newIngredient = newIngredientInput.text.toString()
                if (newIngredient.isNotEmpty()) {
                    // Добавить новый ингредиент в список
                    ingredients.add(newIngredient)
                    adapter.notifyItemInserted(ingredients.size - 1)
                    newIngredientInput.text.clear()
                }
                newIngredientInput.visibility = View.GONE
            }
        }

    }

    private fun initializeRecyclerView() {
        ingredientsList = findViewById(R.id.ingredientsList)
        // Добавление адаптера (ингредиенты)
        adapter = IngredientsListEditAdapter(this, ingredients)
        ingredientsList.adapter = adapter
        ingredientsList.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapterPosition = viewHolder.adapterPosition
                ingredients.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                var toast: Toast? = null
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && viewHolder != null) {
                    // Показать сообщение при начале перетаскивания
                    toast = Toast.makeText(viewHolder.itemView.context, "Удалить ингредиент", Toast.LENGTH_LONG)
                    toast?.show()
                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    toast?.cancel()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(ingredientsList)
    }

    fun exit(view: View) {
        finish()
    }

    fun saveRecipe(view: View) {
        db = dbHelper.writableDatabase

        val recipeName = editName.text.toString()
        val cookingTime = editCookingTime.text.toString()
        val portionsAmount = editPortionsAmount.text.toString().toIntOrNull() ?: 0
        val instructions = editInstructions.text.toString()

        if (recipeName.isNotEmpty() && cookingTime.isNotEmpty() && portionsAmount > 0 && instructions.isNotEmpty()) {
            // Сохраняем рецепт в таблицу "recipes"
            val recipeId = saveRecipeToDatabase(recipeName, cookingTime, portionsAmount, instructions)
            // Сохраняем каждый ингредиент в таблицу "ingredients"
            if (recipeId != (-1).toLong()) {
                saveIngredients(recipeId)
            } else {
                Toast.makeText(this, "Ошибка при сохранении рецепта", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show()
            return
        }
        db.close()

        // Вернуться к просмотру списка (завершить активити)
        finish()
    }

    private fun saveRecipeToDatabase(name: String, time: String, portions: Int, recipe: String): Long {
        // Для вставки данных в таблицу
        val values = ContentValues().apply {
            put(DatabaseHelper.RCP_NAME, name)
            put(DatabaseHelper.RCP_TIME, time)
            put(DatabaseHelper.RCP_PORTIONS_AMOUNT, portions)
            put(DatabaseHelper.RCP_RECIPE_INSTRUCTIONS, recipe)
            put(DatabaseHelper.RCP_USER_ID, userId) // Здесь можно добавить идентификатор пользователя (например, текущего пользователя)
        }

        // Вставляем запись в таблицу рецептов и получаем ID нового рецепта
        return db.insert(DatabaseHelper.RCP_TABLE_NAME, null, values)
    }

    private fun saveIngredients(recipeId: Long) {
        for (ingredient in ingredients) {
            val values = ContentValues().apply {
                put(DatabaseHelper.INGR_NAME, ingredient)
                put(DatabaseHelper.INGR_RECIPE_ID, recipeId)
            }
            // Вставляем каждый ингредиент в таблицу ингредиентов
            db.insert(DatabaseHelper.INGR_TABLE_NAME, null, values)
        }

        // После сохранения всех ингредиентов можно уведомить пользователя
        Toast.makeText(this, "Рецепт сохранен!", Toast.LENGTH_SHORT).show()
    }
}