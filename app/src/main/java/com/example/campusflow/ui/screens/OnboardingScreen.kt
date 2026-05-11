package com.example.campusflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusflow.ui.theme.*
import kotlinx.coroutines.launch

// ── Onboarding page data ─────────────────────────────────────────────────────
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val features: List<String>
)

private val pages = listOf(
    OnboardingPage(
        title       = "Welcome to\nCampusFlow",
        subtitle    = "Your Academic Companion",
        description = "All-in-one platform for students, lecturers, and admins. Manage your academic life from one place.",
        icon        = Icons.Default.School,
        accent      = TealAccent,
        gradientStart = Color(0xFF040B2E),
        gradientEnd   = Color(0xFF1A237E),
        features    = listOf("Real-time updates", "All roles supported", "Secure & private")
    ),
    OnboardingPage(
        title       = "Track Your\nAttendance",
        subtitle    = "Never Miss a Class",
        description = "Mark attendance instantly with GPS verification. View your attendance rate and stay above the 75% threshold.",
        icon        = Icons.Default.QrCodeScanner,
        accent      = Color(0xFF00BFA5),
        gradientStart = Color(0xFF003D33),
        gradientEnd   = Color(0xFF00695C),
        features    = listOf("GPS verification", "QR code scanning", "Live attendance rate")
    ),
    OnboardingPage(
        title       = "View Results\n& Analytics",
        subtitle    = "Know Where You Stand",
        description = "Access your CAT and exam scores, track your GPA, and get personalized performance insights each semester.",
        icon        = Icons.Default.TrendingUp,
        accent      = Color(0xFF82B1FF),
        gradientStart = Color(0xFF0D1B6E),
        gradientEnd   = Color(0xFF1565C0),
        features    = listOf("GPA tracking", "Grade appeals", "Performance insights")
    ),
    OnboardingPage(
        title       = "Stay Connected\n& Informed",
        subtitle    = "Everything in One App",
        description = "Get announcements, chat with lecturers, download course materials, and manage grade appeals — all in one place.",
        icon        = Icons.Default.Hub,
        accent      = Color(0xFFFFD54F),
        gradientStart = Color(0xFF1A0533),
        gradientEnd   = Color(0xFF4A148C),
        features    = listOf("Live chat", "Course materials", "Push notifications")
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    val infiniteTransition = rememberInfiniteTransition(label = "onboarding")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "ring"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Pager ────────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            OnboardingPage(
                page        = page,
                floatOffset = floatY,
                ringAngle   = ringRotation,
                pageIndex   = pageIndex
            )
        }

        // ── Top skip button ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = !isLastPage,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
        ) {
            TextButton(onClick = onFinish) {
                Text(
                    "Skip",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Bottom controls ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val accent     = pages[pagerState.currentPage].accent

                    val width by animateDpAsState(
                        targetValue    = if (isSelected) 28.dp else 8.dp,
                        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label          = "dotWidth"
                    )
                    val alpha by animateFloatAsState(
                        targetValue   = if (isSelected) 1f else 0.35f,
                        animationSpec = tween(300),
                        label         = "dotAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected)
                                    Brush.horizontalGradient(listOf(accent, accent.copy(0.7f)))
                                else
                                    Brush.horizontalGradient(listOf(Color.White.copy(alpha), Color.White.copy(alpha)))
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // CTA button
            val accent = pages[pagerState.currentPage].accent

            AnimatedContent(
                targetState  = isLastPage,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInVertically { it / 3 } togetherWith
                    fadeOut(tween(200)) + slideOutVertically { -it / 3 }
                },
                label = "ctaButton"
            ) { last ->
                if (last) {
                    // Full-width "Get Started" button
                    Button(
                        onClick   = onFinish,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape     = RoundedCornerShape(29.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(TealAccent, Color(0xFF0097A7))),
                                    RoundedCornerShape(29.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint               = Color.White,
                                    modifier           = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Get Started",
                                    color      = Color.White,
                                    fontSize   = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Next arrow button
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(accent.copy(0.25f), accent.copy(0.1f))
                                    )
                                )
                                .border(1.5.dp, accent.copy(0.5f), CircleShape)
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                tint               = accent,
                                modifier           = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Single onboarding page ────────────────────────────────────────────────────
@Composable
private fun OnboardingPage(
    page        : OnboardingPage,
    floatOffset : Float,
    ringAngle   : Float,
    pageIndex   : Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(pageIndex) { visible = false; kotlinx.coroutines.delay(80); visible = true }

    val contentAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = EaseOut),
        label         = "contentAlpha"
    )
    val contentOffset by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(600, delayMillis = 100, easing = EaseOut),
        label         = "contentOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(page.gradientStart, page.gradientEnd, Color(0xFF040B2E))
                )
            )
    ) {
        // Background ambient blobs
        Box(
            modifier = Modifier
                .size(340.dp)
                .offset(x = 120.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(page.accent.copy(0.12f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 340.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(page.accent.copy(0.09f), Color.Transparent)
                    )
                )
        )

        // Grid dots decoration
        repeat(5) { row ->
            repeat(3) { col ->
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .offset(x = (col * 120 + 30).dp, y = (row * 100 + 60).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.06f))
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(100.dp))

            // ── Animated hero illustration ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(y = floatOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .rotate(ringAngle)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    page.accent.copy(0.45f),
                                    Color.White.copy(0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle frosted ring
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .rotate(-ringAngle * 0.6f)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    page.accent.copy(0.2f),
                                    Color.Transparent,
                                    page.accent.copy(0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Inner dark circle
                Box(
                    modifier = Modifier
                        .size(154.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    page.gradientEnd.copy(0.9f),
                                    page.gradientStart
                                )
                            )
                        )
                        .border(
                            width  = 1.5.dp,
                            brush  = Brush.sweepGradient(
                                listOf(page.accent.copy(0.6f), Color.Transparent, page.accent.copy(0.3f))
                            ),
                            shape  = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector       = page.icon,
                        contentDescription = null,
                        tint              = Color.White,
                        modifier          = Modifier.size(68.dp)
                    )
                }

                // Small accent dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 20.dp)
                        .clip(CircleShape)
                        .background(page.accent)
                )
                // Smaller dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = 20.dp, y = (-18).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.5f))
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Text ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .offset(y = contentOffset.dp)
                    .alpha(contentAlpha)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Subtitle pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(page.accent.copy(0.18f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            page.subtitle,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = page.accent,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Main title
                    Text(
                        text       = page.title,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center,
                        lineHeight = 40.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Description
                    Text(
                        text      = page.description,
                        fontSize  = 15.sp,
                        color     = Color.White.copy(0.65f),
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    // Feature chips row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        page.features.forEach { feature ->
                            FeatureChip(label = feature, accent = page.accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(label: String, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White.copy(0.8f)
        )
    }
}
