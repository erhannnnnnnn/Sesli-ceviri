package com.example.ytsubtitlespeaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ytsubtitlespeaker.ui.AppViewModel
import com.example.ytsubtitlespeaker.ui.screens.CaptionListScreen
import com.example.ytsubtitlespeaker.ui.screens.LinkInputScreen
import com.example.ytsubtitlespeaker.ui.screens.LoginScreen
import com.example.ytsubtitlespeaker.ui.screens.ResultScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val uiState by viewModel.uiState.collectAsState()

            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val signInClient = GoogleSignIn.getClient(this, signInOptions)

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = runCatching { task.getResult(com.google.android.gms.common.api.ApiException::class.java) }.getOrNull()
                if (account != null) {
                    viewModel.onLoginSuccess(account.email.orEmpty())
                    navController.navigate("link")
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                statusMessage = uiState.statusMessage,
                                onGoogleSignIn = { launcher.launch(signInClient.signInIntent) }
                            )
                        }
                        composable("link") {
                            LinkInputScreen(
                                uiState = uiState,
                                onUrlChange = viewModel::onYoutubeUrlChanged,
                                onValidateAndNext = {
                                    if (viewModel.extractVideoIdAndValidate()) {
                                        viewModel.fetchCaptionTracks()
                                        navController.navigate("captions")
                                    }
                                }
                            )
                        }
                        composable("captions") {
                            CaptionListScreen(
                                uiState = uiState,
                                onSelectTrack = viewModel::selectTrack,
                                onSelectLanguage = viewModel::selectLanguage,
                                onProcess = {
                                    viewModel.processTrack()
                                    navController.navigate("result")
                                }
                            )
                        }
                        composable("result") {
                            ResultScreen(uiState = uiState)
                        }
                    }
                }
            }
        }
    }
}
