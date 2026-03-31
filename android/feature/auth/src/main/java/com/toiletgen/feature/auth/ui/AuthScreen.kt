package com.toiletgen.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.feature.auth.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

private val Teal = Color(0xFF00897B)
private val TealDark = Color(0xFF004D40)
private val Amber = Color(0xFFFFB300)
private val TealLight = Color(0xFFB2DFDB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    // Bounce animation for the toilet emoji
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounceScale",
    )
    val bounceTranslation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounceTranslation",
    )

    // Card entrance animation
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 300),
        label = "cardAlpha",
    )
    val cardTranslation by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 80f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "cardTranslation",
    )

    // Gradient background
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Teal, TealDark, Color(0xFF00332C)),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            // Animated toilet emoji
            Text(
                text = "\uD83D\uDEBD",
                fontSize = 72.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = bounceScale
                    scaleY = bounceScale
                    translationY = bounceTranslation
                },
            )

            Spacer(Modifier.height(8.dp))

            // App name
            Text(
                text = "ToiletGen",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
            )

            // Subtitle
            Text(
                text = "Найди свой комфорт",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = TealLight.copy(alpha = 0.85f),
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(32.dp))

            // Animated form card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = cardAlpha
                        translationY = cardTranslation
                    }
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.3f),
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Card header
                    Text(
                        text = if (uiState.isLoginMode) "Вход в аккаунт" else "Создать аккаунт",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                    )
                    Text(
                        text = if (uiState.isLoginMode)
                            "Рады видеть вас снова!"
                        else
                            "Присоединяйтесь к нам!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(Modifier.height(24.dp))

                    // Username field (register only)
                    AnimatedVisibility(
                        visible = !uiState.isLoginMode,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ) + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(300),
                        ) + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                    ) {
                        Column {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Имя пользователя") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Teal,
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal,
                                    cursorColor = Teal,
                                    focusedLabelColor = Teal,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            )
                            Spacer(Modifier.height(14.dp))
                        }
                    }

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Электронная почта") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = Teal,
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            cursorColor = Teal,
                            focusedLabelColor = Teal,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Teal,
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            cursorColor = Teal,
                            focusedLabelColor = Teal,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    )

                    // Error display
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        uiState.error?.let { error ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp,
                                    ),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Submit button
                    Button(
                        onClick = {
                            if (uiState.isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.register(username, email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal,
                            disabledContainerColor = Teal.copy(alpha = 0.5f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                            )
                        } else {
                            Text(
                                text = if (uiState.isLoginMode) "Войти" else "Зарегистрироваться",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Toggle mode button
            TextButton(onClick = { viewModel.toggleMode() }) {
                Text(
                    text = if (uiState.isLoginMode)
                        "Нет аккаунта? Зарегистрироваться"
                    else
                        "Уже есть аккаунт? Войти",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
