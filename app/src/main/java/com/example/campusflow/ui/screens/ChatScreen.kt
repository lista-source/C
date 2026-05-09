package com.example.campusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campusflow.data.model.ChatMessage
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.ui.theme.*
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

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
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TealAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(receiverName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(receiverName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Online", fontSize = 11.sp, color = SuccessGreen)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = TextHint) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = DividerGrey,
                            unfocusedContainerColor = BackgroundGrey,
                            focusedContainerColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (messageText.isNotBlank()) NavyPrimary else DividerGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    val msg = ChatMessage(
                                        senderId = currentUserId,
                                        receiverId = receiverId,
                                        message = messageText.trim(),
                                        timestamp = System.currentTimeMillis()
                                    )
                                    database.getReference("chats").push().setValue(msg)
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (messageText.isNotBlank()) Color.White else TextHint
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundGrey
    ) { padding ->
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Start a conversation", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)
                    Text("Send a message to $receiverName", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg, msg.senderId == currentUserId, timeFormat)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isCurrentUser: Boolean, timeFormat: SimpleDateFormat) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp, topEnd = 18.dp,
                        bottomStart = if (isCurrentUser) 18.dp else 4.dp,
                        bottomEnd = if (isCurrentUser) 4.dp else 18.dp
                    )
                )
                .background(if (isCurrentUser) NavyPrimary else Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                color = if (isCurrentUser) Color.White else TextPrimary,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            timeFormat.format(Date(message.timestamp)),
            fontSize = 10.sp,
            color = TextHint,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}



