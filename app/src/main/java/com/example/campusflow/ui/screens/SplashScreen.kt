package com.example.campusflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusflow.data.model.UserRole
import com.example.campusflow.data.repository.AuthRepository
import com.example.campusflow.ui.navigation.Screen
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    authRepository: AuthRepository
) {
    // ── Navigation logic (unchanged) ─────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(2800L)
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            onNavigate(Screen.Login.route)
        } else {
            try {
                val user = authRepository.getUserData(currentUser.uid)
                val destination = when (user.role) {
                    UserRole.STUDENT  -> Screen.StudentDashboard.route
                    UserRole.LECTURER -> Screen.LecturerDashboard.route
                    UserRole.ADMIN    -> Screen.AdminDashboard.route
                }
                onNavigate(destination)
            } catch (e: Exception) {
                onNavigate(Screen.Login.route)
            }
        }
    }

    // ── Animation drivers ────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")

    // Orb 1: slow pulsing scale
    val orb1Scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(3200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orb1"
    )
    // Orb 2: offset pulsing
    val orb2Scale by infiniteTransition.animateFloat(
        initialValue = 1.1f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orb2"
    )
    // Orbit ring rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "ring"
    )
    // Counter-rotating ring
    val ringRotation2 by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(5500, easing = LinearEasing)),
        label = "ring2"
    )
    // Shimmer on progress bar
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "shimmer"
    )
    // Dot pulse for tagline
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "dot"
    )

    // ── Enter animations ─────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOut),
        label = "logoAlpha"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 60f,
        animationSpec = tween(700, delayMillis = 300, easing = EaseOutCubic),
        label = "titleOffset"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 300, easing = EaseOut),
        label = "titleAlpha"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 600, easing = EaseOut),
        label = "taglineAlpha"
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, delayMillis = 900, easing = EaseOut),
        label = "pillAlpha"
    )
    val progressAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 1200, easing = EaseOut),
        label = "progressAlpha"
    )

    // ── UI ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF040B2E),
                        0.45f to Color(0xFF0D1452),
                        0.75f to Color(0xFF1A237E),
                        1.0f to Color(0xFF0A1545)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Background decorative orbs ──────────────────────────────────
        // Top-right large soft orb
        Box(
            modifier = Modifier
                .size(380.dp)
                .offset(x = 100.dp, y = (-160).dp)
                .align(Alignment.TopEnd)
                .scale(orb1Scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TealAccent.copy(alpha = 0.18f),
                            Color(0xFF00BFA5).copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
                .blur(40.dp)
        )

        // Bottom-left soft orb
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = 120.dp)
                .align(Alignment.BottomStart)
                .scale(orb2Scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CornflowerBlue.copy(alpha = 0.22f),
                            Color(0xFF3F51B5).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )

        // Small accent orb top-left
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-30).dp, y = (-220).dp)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TealAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .blur(30.dp)
        )

        // ── Geometric grid dots (subtle) ────────────────────────────────
        repeat(6) { row ->
            repeat(4) { col ->
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .offset(
                            x = (col * 96 - 144).dp,
                            y = (row * 120 - 280).dp
                        )
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                )
            }
        }

        // ── Main content column ─────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Animated logo ring + icon ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating dashed-style ring (outer glow)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(ringRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    TealAccent.copy(alpha = 0.0f),
                                    TealAccent.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.3f),
                                    TealAccent.copy(alpha = 0.0f)
                                )
                            )
                        )
                )

                // Inner counter-rotating ring
                Box(
                    modifier = Modifier
                        .size(134.dp)
                        .rotate(ringRotation2)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CornflowerBlue.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    CornflowerBlue.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Logo center circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E3A9F),
                                    Color(0xFF0D1B6E),
                                    Color(0xFF071040)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle inner glow ring
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        TealAccent.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "CampusFlow",
                        modifier = Modifier.size(52.dp),
                        tint = Color.White
                    )
                }

                // Small accent dot — top-right of circle
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .offset(x = 52.dp, y = (-48).dp)
                        .clip(CircleShape)
                        .background(TealAccent)
                )

                // Small accent dot — bottom-left
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .offset(x = (-50).dp, y = 44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── App name ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .offset(y = titleOffset.dp)
                    .alpha(titleAlpha)
            ) {
                // Glow behind text
                Text(
                    text = "CampusFlow",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealAccent.copy(alpha = 0.25f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.blur(12.dp)
                )
                Text(
                    text = "CampusFlow",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFB8CFFF),
                                TealAccent,
                                Color.White
                            )
                        )
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Tagline ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.alpha(taglineAlpha),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(TealAccent.copy(alpha = dotAlpha))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Your Academic Companion",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(TealAccent.copy(alpha = dotAlpha))
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Role pills / feature badges ──────────────────────────────
            Row(
                modifier = Modifier.alpha(pillAlpha),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Student" to Color(0xFF1565C0),
                    "Lecturer" to Color(0xFF00695C),
                    "Admin" to Color(0xFF4A148C)
                ).forEach { (label, color) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(color.copy(alpha = 0.35f))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(56.dp))

            // ── Shimmer progress bar ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .alpha(progressAlpha)
            ) {
                // Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                )
                // Shimmer fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    (shimmerOffset - 0.4f).coerceIn(0f, 1f) to Color.Transparent,
                                    shimmerOffset.coerceIn(0f, 1f) to TealAccent,
                                    (shimmerOffset + 0.4f).coerceIn(0f, 1f) to Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Loading label ─────────────────────────────────────────────
            Text(
                text = "Loading your experience...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(progressAlpha)
            )
        }

        // ── Version tag bottom-center ───────────────────────────────────
        Text(
            text = "v2.0 · Powered by CampusFlow",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(pillAlpha),
            letterSpacing = 0.4.sp
        )
    }
}

