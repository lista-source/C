package com.example.campusflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusflow.data.model.ChatMessage
import com.example.campusflow.data.repository.AuthRepository
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    authRepository: AuthRepository,
    receiverId: String,
    receiverName: String
) {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
    val currentUserId = authRepository.currentUser?.uid ?: ""
    val database = FirebaseDatabase.getInstance()
    val listState = rememberLazyListState()

    DisposableEffect(receiverId) {
        val ref = database.getReference("chats")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages = snapshot.children
                    .mapNotNull { it.getValue(ChatMessage::class.java) }
                    .filter {
                        (it.senderId == currentUserId && it.receiverId == receiverId) ||
                        (it.senderId == receiverId && it.receiverId == currentUserId)
                    }
                    .sortedBy { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receiverName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(contentPadding = PaddingValues(8.dp)) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val msg = ChatMessage(
                                senderId = currentUserId,
                                receiverId = receiverId,
                                message = messageText,
                                timestamp = System.currentTimeMillis()
                            )
                            database.getReference("chats").push().setValue(msg)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg, msg.senderId == currentUserId)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(12.dp),
                color = if (isCurrentUser) contentColorFor(MaterialTheme.colorScheme.primary)
                else contentColorFor(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}
