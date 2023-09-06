package com.raywenderlich.listmaker.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.listmaker.databinding.ListSelectionViewHolderBinding

class ListSelectionRecyclerViewAdapter:RecyclerView.Adapter<ListSelectionViewHolder>() {
    val listTitles = arrayOf("Shopping list", "Chores", "Android Tutorials")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSelectionViewHolder {
      val binding = ListSelectionViewHolderBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ListSelectionViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return listTitles.size
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ListSelectionViewHolder, position: Int) {
holder.binding.itemNumber.text = (position +1).toString()
        holder.binding.itemString.text = listTitles[position]
        TODO("Not yet implemented")
    }
}