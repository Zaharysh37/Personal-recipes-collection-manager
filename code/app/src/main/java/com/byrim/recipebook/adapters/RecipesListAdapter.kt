package com.byshnev.recipebook.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.byshnev.recipebook.R
import com.byshnev.recipebook.activities.RecipeCreateActivity
import com.byshnev.recipebook.database.DatabaseHelper

class RecipesListAdapter(
    private val context: Context,
    private val items: List<String>,
    private val onClickDo: (position: Int) -> Unit,
    private val onLongClickDo: (position: Int, view: View) -> Unit): RecyclerView.Adapter<RecipesListAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recipeNameView: TextView = view.findViewById(R.id.recipes_list_recipe_name)
        val recipeItemContainerView: ConstraintLayout = view.findViewById(R.id.recipes_list_recipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val recipeItem = items[position]
        holder.recipeNameView.text = recipeItem
        holder.recipeItemContainerView.setOnClickListener {
            onClickDo(position)
        }
        holder.recipeItemContainerView.setOnLongClickListener { view ->
            onLongClickDo(position, view)
            true
        }
    }
}