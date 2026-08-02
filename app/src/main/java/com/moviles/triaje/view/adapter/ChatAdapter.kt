package com.moviles.triaje.view.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.R
import com.moviles.triaje.model.ChatMessage

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = mutableListOf<ChatMessage>()

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_IA = 2
        private const val TYPE_DATE = 3
        private const val TYPE_DISCLAIMER = 4
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when (message.type) {
            ChatMessage.TYPE_DATE -> TYPE_DATE
            ChatMessage.TYPE_DISCLAIMER -> TYPE_DISCLAIMER
            else -> if (message.isUser) TYPE_USER else TYPE_IA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false))
            TYPE_IA -> IAViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_ia, parent, false))
            TYPE_DATE -> DateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_date, parent, false))
            TYPE_DISCLAIMER -> DisclaimerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_disclaimer, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> {
                holder.tvMessage.text = message.text
                if (message.isTyping) {
                    holder.tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_user_processing)
                    holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                } else {
                    holder.tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_user)
                    holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
                }
            }
            is IAViewHolder -> {
                holder.tvMessage.text = formatMarkdown(message.text)
                holder.tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_ia)
                holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            }
            is DateViewHolder -> {
                holder.tvDate.text = message.text
            }
            is DisclaimerViewHolder -> {

            }
        }
    }

    private fun formatMarkdown(text: String): CharSequence {
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
        val finalBuilder = android.text.SpannableStringBuilder()
        var lastIdx = 0

        var matchResult = boldRegex.find(text)
        while (matchResult != null) {
            finalBuilder.append(text.substring(lastIdx, matchResult.range.first))
            val start = finalBuilder.length
            finalBuilder.append(matchResult.groupValues[1])
            finalBuilder.setSpan(StyleSpan(Typeface.BOLD), start, finalBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            lastIdx = matchResult.range.last + 1
            matchResult = boldRegex.find(text, lastIdx)
        }
        finalBuilder.append(text.substring(lastIdx))

        return if (finalBuilder.isEmpty() && text.isNotEmpty()) text else finalBuilder
    }

    override fun getItemCount(): Int = messages.size

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newList: List<ChatMessage>) {
        this.messages = newList.toMutableList()
        notifyDataSetChanged()
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessageUser)
    }

    class IAViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessageIA)
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvChatDateItem)
    }

    class DisclaimerViewHolder(view: View) : RecyclerView.ViewHolder(view)
}