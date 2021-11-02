package com.example.android_coroutine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.android_coroutine.R
import com.example.android_coroutine.databinding.ArticleBinding
import com.example.android_coroutine.model.Article

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ViewHolder>(){
    private val articles : MutableList<Article>  = mutableListOf()

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val binding = DataBindingUtil.bind<ArticleBinding>(view)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.article,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            binding.article = articles[position]
        }
    }

    fun add(articles: List<Article>){
        this.articles.addAll(articles)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = articles.size
}