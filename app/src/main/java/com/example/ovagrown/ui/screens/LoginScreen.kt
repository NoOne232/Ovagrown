package com.example.ovagrown.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ovagrown.R
import com.example.ovagrown.database.Authentication
import com.example.ovagrown.database.ProfileFunctions
import com.example.ovagrown.ui.theme.OVAgrownTheme
import kotlinx.coroutines.launch

val TropiLandFont = FontFamily(
    Font(R.font.tropi_land)
)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var showLoginPopup by remember { mutableStateOf(false) }

    val authentication = remember { Authentication() }
    val profileFunctions = remember { ProfileFunctions() }

    val infiniteTransition = rememberInfiniteTransition(label = "fade")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonTextFade"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Button(
            onClick = {
                showLoginPopup = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF17721D)
            ),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .width(260.dp)
                .height(64.dp)
        ) {
            Text(
                text = "Press here to login",
                fontFamily = TropiLandFont,
                fontSize = 22.sp,
                color = Color.White.copy(alpha = alpha)
            )
        }

        if (showLoginPopup) {
            LoginSignupPopup(
                onDismiss = {
                    showLoginPopup = false
                },
                onLoginClick = { email, password ->
                    val userId = authentication.login(email, password)

                    println("Logged in user ID: $userId")

                    onLoginSuccess()
                },
                onSignUpClick = { email, password ->
                    authentication.signUp(
                        email = email,
                        password = password
                    )

                    var userId = authentication.getCurrentUserId()

                    if (userId == null) {
                        userId = authentication.login(
                            email = email,
                            password = password
                        )
                    }

                    if (userId == null) {
                        throw IllegalStateException(
                            "Account was created, but the user ID could not be found."
                        )
                    }

                    profileFunctions.createProfile(
                        userId = userId,
                        username = email
                    )

                    println("Account and profile created for: $email")
                }
            )
        }
    }
}

@Composable
fun LoginSignupPopup(
    onDismiss: () -> Unit,
    onLoginClick: suspend (String, String) -> Unit,
    onSignUpClick: suspend (String, String) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val titleText = if (isSignUpMode) {
        "Create your account"
    } else {
        "Welcome back"
    }

    val mainButtonText = if (isSignUpMode) {
        if (isLoading) "Creating account..." else "Sign up"
    } else {
        if (isLoading) "Logging in..." else "Login"
    }

    fun submitForm() {
        errorMessage = null
        successMessage = null

        when {
            email.isBlank() -> {
                errorMessage = "Please enter your email."
            }

            !email.contains("@") -> {
                errorMessage = "Please enter a valid email."
            }

            password.isBlank() -> {
                errorMessage = "Please enter your password."
            }

            password.length < 6 -> {
                errorMessage = "Password must be at least 6 characters."
            }

            isSignUpMode && confirmPassword.isBlank() -> {
                errorMessage = "Please confirm your password."
            }

            isSignUpMode && password != confirmPassword -> {
                errorMessage = "Passwords do not match."
            }

            else -> {
                isLoading = true

                coroutineScope.launch {
                    try {
                        if (isSignUpMode) {
                            onSignUpClick(email, password)

                            successMessage = "Account created. You can now log in."
                            isSignUpMode = false
                            password = ""
                            confirmPassword = ""
                        } else {
                            onLoginClick(email, password)
                            onDismiss()
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Something went wrong. Please try again."
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(
                text = titleText,
                fontFamily = TropiLandFont,
                fontSize = 26.sp,
                color = Color(0xFF17721D)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    label = {
                        Text("Email")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = {
                        Text("Password")
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                passwordVisible = !passwordVisible
                            }
                        ) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSignUpMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        label = {
                            Text("Confirm password")
                        },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    confirmPasswordVisible = !confirmPasswordVisible
                                }
                            ) {
                                Text(
                                    text = if (confirmPasswordVisible) "Hide" else "Show"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage ?: "",
                        color = Color(0xFF17721D),
                        fontSize = 14.sp
                    )
                }

                TextButton(
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        errorMessage = null
                        successMessage = null
                        password = ""
                        confirmPassword = ""
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isSignUpMode) {
                            "Already have an account? Login"
                        } else {
                            "Don't have an account? Sign up"
                        },
                        color = Color(0xFF17721D)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    submitForm()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF17721D)
                )
            ) {
                Text(mainButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    OVAgrownTheme {
        LoginScreen()
    }
}
