package com.byshnev.recipebook.activities

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byshnev.recipebook.R
import com.byshnev.recipebook.adapters.RecipesListAdapter
import com.byshnev.recipebook.database.DatabaseHelper
import com.byshnev.recipebook.database.DatabaseHelper.Companion.INGR_ID
import com.byshnev.recipebook.database.DatabaseHelper.Companion.INGR_RECIPE_ID
import com.byshnev.recipebook.database.DatabaseHelper.Companion.INGR_TABLE_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_ID
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_TABLE_NAME
import com.byshnev.recipebook.database.DatabaseHelper.Companion.RCP_USER_ID


class RecipesListActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var searchBar: EditText
    private lateinit var recipesList: RecyclerView
    private lateinit var addRecipeBtn: TextView
    private lateinit var exitBtn: TextView
    private lateinit var showAllRecipesBtn: TextView
    private lateinit var searchButton: ImageButton
    private var filteredRecipesIds: MutableList<Long> = mutableListOf()
    private var filteredRecipes: MutableList<String> = mutableListOf()
    private lateinit var adapter: RecipesListAdapter

    private var userId: Long = -1

    private var previosRequestWasAllRecipes: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)
        userId = intent.getLongExtra("USER_ID", -1)

        //TODO("Убрать после отладки")
        if (userId == (-1).toLong()) {
            Toast.makeText(this, "Ошибка: User ID не передан", Toast.LENGTH_SHORT).show()
        }

        searchBar = findViewById(R.id.editTextSearch)
        recipesList = findViewById(R.id.recipes_list)
        addRecipeBtn = findViewById(R.id.addRecipeBtn)
        exitBtn = findViewById(R.id.exitBtn)
        showAllRecipesBtn = findViewById(R.id.allRecipesBtn)
        searchButton = findViewById(R.id.searchImgBtn)

        adapter = RecipesListAdapter(this, filteredRecipes, { position: Int ->
            val intent = Intent(this, RecipeViewActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("RECIPE_ID", filteredRecipesIds[position])
            startActivity(intent)
        }, { position, v ->
            val popupMenu = PopupMenu(this, v)
            popupMenu.inflate(R.menu.recipe_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> goToRecipeEditing(filteredRecipesIds[position])
                    R.id.delete -> deleteRecipe(filteredRecipesIds[position])
                }
                true
            }
            popupMenu.show()
        })

        recipesList.adapter = adapter
        recipesList.layoutManager = LinearLayoutManager(this)
        initializeSwipes()

        dbHelper = DatabaseHelper.getInstance(applicationContext)

        searchButton.setOnClickListener {
            filterItems(searchBar.text.toString())
            previosRequestWasAllRecipes = false
        }

        showAllRecipesBtn.setOnClickListener {
            showAllRecipes()
            previosRequestWasAllRecipes = true
        }
    }

    override fun onStart() {
        super.onStart()
        db = dbHelper.writableDatabase
        filterItems(searchBar.text.toString())
    }

    override fun onResume() {
        super.onResume()
        if (previosRequestWasAllRecipes) {
            showAllRecipes()
        } else {
            filterItems(searchBar.text.toString())
        }
    }


    override fun onStop() {
        super.onStop()
        db.close()
    }

    // Добавить перетаскивания к элементам списка
    private fun initializeSwipes() {
        // Перетаскивание вправо (удаление)
        var itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteRecipe(filteredRecipesIds[viewHolder.adapterPosition])
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                var toast: Toast? = null
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && viewHolder != null) {
                    // Показать сообщение при начале перетаскивания
                    toast = Toast.makeText(viewHolder.itemView.context, "Удалить рецепт", Toast.LENGTH_SHORT)
                    toast?.show()
                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    toast?.cancel()
                }
            }
        })
        // Присоединение ItemTouchHelper к RecyclerView
        itemTouchHelper.attachToRecyclerView(recipesList)

        // Перетаскивание влево (редактирование)
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                goToRecipeEditing(filteredRecipesIds[viewHolder.adapterPosition])
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                var toast: Toast? = null
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && viewHolder != null) {
                    toast = Toast.makeText(viewHolder.itemView.context, "Редактировать рецепт", Toast.LENGTH_SHORT)
                    toast?.show()
                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    toast?.cancel()
                }
            }
        })
        // Присоединение ItemTouchHelper к RecyclerView
        itemTouchHelper.attachToRecyclerView(recipesList)
    }

    private fun filterItems(searchValue : String) {
        if (searchValue.isEmpty()) {
            return
        }
        filteredRecipes.clear()

        // Используем параметрический запрос для предотвращения SQL-инъекций
        val cursor = db.rawQuery("SELECT ${RCP_ID}, ${RCP_NAME} FROM ${RCP_TABLE_NAME} WHERE ${RCP_USER_ID}=? AND ${RCP_NAME} LIKE ?", arrayOf(userId.toString(), "%$searchValue%"))
        if (cursor.moveToFirst()) {
            var counter = 0
            do {
                filteredRecipes.add(counter, cursor.getString(cursor.getColumnIndexOrThrow(RCP_NAME)))
                filteredRecipesIds.add(counter, cursor.getLong(cursor.getColumnIndexOrThrow(RCP_ID)))
                ++counter
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Обновление адаптера с отфильтрованными данными
        adapter.notifyDataSetChanged()  // Для обновления интерфейса
    }

    private fun goToRecipeEditing(recipeId: Long) {
        val intent = Intent(this, RecipeEditActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivity(intent)
    }

    private fun deleteRecipe(recipeId: Long) {
        val ingrDeleted = db.delete(INGR_TABLE_NAME, "$INGR_RECIPE_ID = ?", arrayOf(recipeId.toString()))
        val recipesDeleted = db.delete(RCP_TABLE_NAME, "$RCP_ID=?", arrayOf(recipeId.toString()))
        if (ingrDeleted > 0 && recipesDeleted > 0) {
            Toast.makeText(this, "Рецепт был успешно удалён", Toast.LENGTH_SHORT).show()
            filterItems(searchBar.text.toString())
        } else {
            Toast.makeText(this, "Не получилось удалить рецепт", Toast.LENGTH_SHORT).show()
        }
    }

    fun goToRecipeCreation(view: View) {
        val intent = Intent(this, RecipeCreateActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }

    fun showAllRecipes() {
        filteredRecipes.clear()

        val cursor = db.rawQuery("SELECT ${RCP_ID}, ${RCP_NAME} FROM ${RCP_TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            var counter = 0
            do {
                filteredRecipes.add(counter, cursor.getString(cursor.getColumnIndexOrThrow(RCP_NAME)))
                filteredRecipesIds.add(counter, cursor.getLong(cursor.getColumnIndexOrThrow(RCP_ID)))
                ++counter
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Обновление адаптера
        adapter.notifyDataSetChanged()  // Для обновления интерфейса
    }

    fun exit(view: View) {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }
}
