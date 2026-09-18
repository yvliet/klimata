package com.example.klimata

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.klimata.data.storage.KlimataPreferences
import com.example.klimata.ui.navigation.KlimataNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (KlimataPreferences.isStatusBarDisabled(this)) {
            applyStatusBarVisibility(this, isStatusBarDisabled = true)
        }
        setContent {
            KlimataNavGraph()
        }
    }

    override fun onResume() {
        super.onResume()
        if (KlimataPreferences.isStatusBarDisabled(this)) {
            applyStatusBarVisibility(this, isStatusBarDisabled = true)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && KlimataPreferences.isStatusBarDisabled(this)) {
            applyStatusBarVisibility(this, isStatusBarDisabled = true)
        }
    }
}

fun applyStatusBarVisibility(activity: Activity?, isStatusBarDisabled: Boolean) {
    applyStatusBarVisibility(activity?.window, isStatusBarDisabled)
}

@Suppress("DEPRECATION")
fun applyStatusBarVisibility(window: Window?, isStatusBarDisabled: Boolean) {
    val win = window ?: return
    val insetsController = WindowCompat.getInsetsController(win, win.decorView)
    if (isStatusBarDisabled) {
        win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        insetsController.show(WindowInsetsCompat.Type.statusBars())
    }
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun SyncDialogStatusBar() {
    val context = LocalContext.current
    val isStatusBarDisabled = remember { KlimataPreferences.isStatusBarDisabled(context) }
    if (!isStatusBarDisabled) return

    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    val activity = remember(context) { context.findActivity() }

    SideEffect {
        applyStatusBarVisibility(dialogWindow, isStatusBarDisabled = true)
    }

    DisposableEffect(dialogWindow) {
        applyStatusBarVisibility(dialogWindow, isStatusBarDisabled = true)
        onDispose {
            applyStatusBarVisibility(activity, isStatusBarDisabled = true)
        }
    }
}