package com.toiletgen.feature.profile.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.core.ui.components.LoadingScreen
import com.toiletgen.core.ui.theme.Primary
import com.toiletgen.core.ui.theme.PrimaryContainer
import com.toiletgen.core.ui.theme.OnPrimaryContainer
import com.toiletgen.feature.profile.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

private val TealDark = Color(0xFF00695C)
private val TealLight = Color(0xFF26A69A)
private val TealSoft = Color(0xFF80CBC4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onAchievementsClick: () -> Unit,
    onYearlyReportClick: () -> Unit,
    onVisitHistoryClick: () -> Unit,
    onStampsClick: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen(Modifier.fillMaxSize())
        return
    }

    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // --- Gradient header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val gradient = Brush.linearGradient(
                        colors = listOf(TealDark, Primary, TealLight),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    drawRect(brush = gradient)
                }
                .padding(top = 56.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Animated avatar
                AnimatedAvatar(
                    initials = user?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = user?.username ?: "\u0413\u043E\u0441\u0442\u044C",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )

                if (!user?.email.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user!!.email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                        ),
                    )
                }
            }
        }

        // --- Stats row (overlapping the header bottom) ---
        StatsRow(
            reviewsCount = uiState.reviewsCount,
            visitsCount = uiState.visitsCount,
            toiletsCreated = uiState.toiletsCreated,
            onVisitsClick = onVisitHistoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(4.dp))

        // --- Menu section ---
        Text(
            text = "\u041C\u0435\u043D\u044E",
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        ProfileMenuCard(
            icon = Icons.Default.EmojiEvents,
            iconTint = Color(0xFFFFB300),
            title = "\u0414\u043E\u0441\u0442\u0438\u0436\u0435\u043D\u0438\u044F",
            subtitle = "\u0412\u0430\u0448\u0438 \u0430\u0447\u0438\u0432\u043A\u0438 \u0438 \u043D\u0430\u0433\u0440\u0430\u0434\u044B",
            onClick = onAchievementsClick,
        )

        ProfileMenuCard(
            icon = Icons.Default.Star,
            iconTint = Color(0xFF7B1FA2),
            title = "\u041A\u043E\u043B\u043B\u0435\u043A\u0446\u0438\u044F \u043C\u0430\u0440\u043E\u043A",
            subtitle = "\u0412\u0430\u0448\u0438 \u0442\u0443\u0430\u043B\u0435\u0442\u043D\u044B\u0435 \u043C\u0430\u0440\u043A\u0438 \u0438 \u043E\u0431\u043C\u0435\u043D",
            onClick = onStampsClick,
        )

        ProfileMenuCard(
            icon = Icons.Default.Assessment,
            iconTint = Color(0xFF1976D2),
            title = "\u0413\u043E\u0434\u043E\u0432\u043E\u0439 \u043E\u0442\u0447\u0451\u0442",
            subtitle = "\u0412\u0430\u0448\u0430 \u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043A\u0430 \u0437\u0430 \u0433\u043E\u0434",
            onClick = onYearlyReportClick,
        )

        Spacer(Modifier.height(16.dp))

        // --- Logout ---
        ProfileMenuCard(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            iconTint = MaterialTheme.colorScheme.error,
            title = "\u0412\u044B\u0439\u0442\u0438 \u0438\u0437 \u0430\u043A\u043A\u0430\u0443\u043D\u0442\u0430",
            subtitle = "\u0412\u044B \u0432\u0441\u0435\u0433\u0434\u0430 \u0441\u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0439\u0442\u0438 \u0441\u043D\u043E\u0432\u0430",
            onClick = {
                viewModel.logout()
                onLogout()
            },
            isDestructive = true,
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ---------------------------------------------------------------------------
// Animated avatar with pulsing ring
// ---------------------------------------------------------------------------

@Composable
private fun AnimatedAvatar(initials: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")

    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring_scale",
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring_alpha",
    )

    Box(contentAlignment = Alignment.Center) {
        // Pulsing outer ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(ringScale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = ringAlpha)),
        )

        // Avatar circle
        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = PrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (initials != "?") {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryContainer,
                        ),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = OnPrimaryContainer,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Stats row
// ---------------------------------------------------------------------------

@Composable
private fun StatsRow(
    reviewsCount: Int,
    visitsCount: Int,
    toiletsCreated: Int,
    onVisitsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(value = "$reviewsCount", label = "\u041E\u0442\u0437\u044B\u0432\u044B")
            StatItem(
                value = "$visitsCount",
                label = "\u0412\u0438\u0437\u0438\u0442\u044B",
                onClick = onVisitsClick,
            )
            StatItem(value = "$toiletsCreated", label = "\u0422\u043E\u0447\u043A\u0438")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        } else {
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        },
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Primary,
            ),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

// ---------------------------------------------------------------------------
// Menu card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDestructive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
