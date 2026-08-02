package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.R
import com.moviles.triaje.model.ChatMessage
import com.moviles.triaje.view.adapter.ChatAdapter
import com.moviles.triaje.viewmodel.AuraIAViewModel

class AuraIAFragment : Fragment() {

    private lateinit var adapter: ChatAdapter
    private lateinit var viewModel: AuraIAViewModel
    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aura_ia, container, false)
        
        viewModel = ViewModelProvider(requireActivity())[AuraIAViewModel::class.java]
        
        rvChat = view.findViewById(R.id.rvChat)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        if (viewModel.messages.value.isNullOrEmpty()) {
            addInitialHeaders()
            showInitialMessage()
        }
        
        return view
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                rvChat.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.lastResponse.observe(viewLifecycleOwner) { responseText ->
            if (responseText != null) {
                typeText(responseText, 0)
                viewModel.consumeLastResponse()
            }
        }
    }

    private fun setupClickListeners() {
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessageToGemini(text)
                etMessage.setText("")
            }
        }
    }

    private fun addInitialHeaders() {
        val sdf = java.text.SimpleDateFormat("EEE, dd MMM", java.util.Locale("es", "ES"))
        val currentDate = sdf.format(java.util.Date()).replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() 
        }
        
        viewModel.addMessage(ChatMessage(currentDate, isUser = false, type = ChatMessage.TYPE_DATE))
        viewModel.addMessage(ChatMessage("", isUser = false, type = ChatMessage.TYPE_DISCLAIMER))
    }

    private fun showInitialMessage() {
        val welcomeText = "Hola, soy Aura IA. Estoy aquí para ayudarte con tus consultas médicas. ¿Cómo te sientes hoy?"
        val initialMsg = ChatMessage("", isUser = false, isTyping = true)
        viewModel.addMessage(initialMsg)
        
        typeText(welcomeText, 0)
    }

    private fun typeText(fullText: String, currentIndex: Int) {
        if (currentIndex <= fullText.length) {
            val partialText = fullText.substring(0, currentIndex)
            viewModel.updateLastIAMessage(partialText, isTyping = true)
            
            handler.postDelayed({
                typeText(fullText, currentIndex + 1)
            }, 20) // mas rapido para respuestas largas
        } else {
            viewModel.updateLastIAMessage(fullText, isTyping = false)
        }
    }
}