package ru.finnipet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.finnipet.app.ui.FinniApp
import ru.finnipet.app.ui.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(applicationContext))
            FinniApp(viewModel)
        }
    }
}
