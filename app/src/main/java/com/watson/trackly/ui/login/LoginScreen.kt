package com.watson.trackly.ui.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aholdusa.cleansweep.ui.common.AppMain
import com.aholdusa.cleansweep.ui.common.TransparentAppMain
import com.aholdusa.cleansweep.ui.common.White
import com.watson.trackly.R
import com.watson.trackly.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    vm: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val loginState by vm.loginUiState.collectAsStateWithLifecycle()

    // Handle login success
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Show error toast
    LaunchedEffect(loginState.errorMessage) {
        loginState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo at the top area
        Image(
            painter = painterResource(id = R.drawable.main_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 100.dp)
                .fillMaxWidth(0.7f),
            contentScale = ContentScale.Fit
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Trackly",
            color = AppMain,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Login container at the bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = TransparentAppMain,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomLoginTextField(
                        value = loginState.userId,
                        onValueChange = vm::onUserIdChange,
                        label = "User ID",
                        icon = Icons.Default.Person,
                        enabled = !loginState.isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    CustomLoginTextField(
                        value = loginState.password,
                        onValueChange = vm::onPasswordChange,
                        label = "Password",
                        icon = Icons.Default.Lock,
                        enabled = !loginState.isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (loginState.userId.isNotEmpty() && loginState.password.isNotEmpty()) {
                                    vm.login()
                                }
                            }
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        text = if (loginState.isLoading) "Logging in..." else "Login",
                        onClick = vm::login,
                        enabled = loginState.userId.isNotEmpty() &&
                                loginState.password.isNotEmpty()
                    )

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun CustomLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background area (pill start) with stylized slant
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp)
                    .background(
                        color = AppMain, // Brighter pink/red like the reference image
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 12.dp, bottomEnd = 40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Input area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        color = Color(0xFFF25C84).copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = Color(0xFF333333),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    enabled = enabled,
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFFF25C84))
                )
            }
        }
    }
}

// Made with Bob
