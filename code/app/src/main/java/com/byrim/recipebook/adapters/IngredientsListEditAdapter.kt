package com.byshnev.recipebook.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.byshnev.recipebook.R

class IngredientsListEditAdapter(
    private val context: Context,
    private val items: List<String>): RecyclerView.Adapter<IngredientsListEditAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var lastClickTime: Long = 0
        val ingredientContainerView: ConstraintLayout = view.findViewById(R.id.ingredient_item_container)
        val ingredientNameView: TextView = view.findViewById(R.id.ingredient_item_name)
    }

    // Вызывается при создании представления (элемента пользовательсокго интерфейса)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_item_edit_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = items.size    // Вернуть размер списка

    // Вызывается при заполнении представления данными
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val ingredientItem = items[position]
        holder.ingredientNameView.text = ingredientItem
        if (position % 2 == 0) {    // Если элемент в списке чётный
            holder.ingredientContainerView.setBackgroundColor(context.applicationContext.getColor(R.color.light_green))
        } else {
            holder.ingredientContainerView.setBackgroundColor(context.applicationContext.getColor(R.color.bold_green))
        }
        holder.ingredientContainerView.isClickable = true
        holder.ingredientContainerView.isFocusable = true
    }
}