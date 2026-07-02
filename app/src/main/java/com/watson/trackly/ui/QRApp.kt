package com.watson.trackly.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.withResumed
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.watson.trackly.R
import com.watson.trackly.ui.history.HistoryScreenLayout
import com.watson.trackly.ui.main.MainScreenLayout
import com.watson.trackly.ui.main.MainViewModel
import com.watson.trackly.ui.main.QRCodeAction
import com.watson.trackly.ui.map.AisleMapScreen
import com.watson.trackly.ui.map.AisleMapViewModel
import com.watson.trackly.ui.premium.PremiumScreenLayout
import com.watson.trackly.ui.result.QRCodeResultLayout
import com.watson.trackly.ui.setting.SettingScreenLayout
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.watson.trackly.ui.login.LoginScreen

@Composable
fun QRApp(
    vm: MainViewModel = hiltViewModel(),
    mapVm: AisleMapViewModel = hiltViewModel(),
    appNavHost: NavHostController = rememberNavController()
) {

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(key1 = Unit) {
        vm.qrCodeActionState.collect {
            when (it) {
                is QRCodeAction.ToastAction -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is QRCodeAction.LocationScanned -> {
                    // Handle location scan - navigate back to map and update
                    mapVm.onBarcodeScanned(it.locationId)
                    appNavHost.navigate(AppScreen.AISLE_MAP.value) {
                        popUpTo(AppScreen.MAIN.value) { inclusive = true }
                    }
                }
                is QRCodeAction.OpenUrl -> {
                    val url = it.url
                    lifecycle.withResumed {
                        if (url.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                }

                is QRCodeAction.OpenQRCodeResult -> {
                    if (it.id != MainViewModel.INVALID_DB_ROW_ID) {
                        appNavHost.navigate(
                            AppScreen.RESULT.value.replace(
                                "{id}",
                                it.id.toString()
                            )
                        )
                    }
                }

                is QRCodeAction.CopyText -> {
                    val copyText = it.text
                    if (copyText.isNotEmpty()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Text", copyText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.resources.getString(R.string.copied_to_clipboard, copyText), Toast.LENGTH_SHORT).show()
                    }
                }

                is QRCodeAction.ContactInfo -> {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE
                        putExtra(ContactsContract.Intents.Insert.NAME, it.contact.name)
                        putExtra(ContactsContract.Intents.Insert.EMAIL, it.contact.email?.firstOrNull())
                        putExtra(ContactsContract.Intents.Insert.PHONE, it.contact.phone?.firstOrNull())
                        putExtra(ContactsContract.Intents.Insert.COMPANY, it.contact.organization)
                        putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it.contact.title)
                        putExtra(ContactsContract.Intents.Insert.NOTES, it.contact.urls?.firstOrNull())
                    }
                    context.startActivity(intent)
                }

                is QRCodeAction.TextSearchGoogle -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${it.text}"))
                    context.startActivity(intent)
                }

                is QRCodeAction.TextShareAction -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, it.text)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
                }

                is QRCodeAction.CallPhoneAction -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                    context.startActivity(intent)
                }

                is QRCodeAction.SendSMSAction -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${it.sms.number}"))
                    intent.putExtra("sms_body", it.sms.message)
                    context.startActivity(intent)
                }

                is QRCodeAction.PickGalleryImage -> {
                    val uri = it.uri
                    vm.showLoading()
                    BarcodeScanning.getClient().process(InputImage.fromFilePath(context, uri))
                        .addOnSuccessListener { barcodes ->
                            vm.hideLoading()
                            if (barcodes.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.no_qr_code_detected), Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            barcodes.forEach { barcode ->
                                if (barcode.rawValue != null) {
                                    vm.scanQRSuccess(barcode)
                                }
                            }
                        }
                        .addOnFailureListener {
                            vm.hideLoading()
                            Toast.makeText(context, context.getString(R.string.failed_to_scan_qr_code), Toast.LENGTH_SHORT).show()
                        }
                }

                else -> {}
            }
        }
    }

    var isLoggedIn by remember { mutableStateOf(false) }

    NavHost(
        navController = appNavHost,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppScreen.LOGIN.value
    ) {
        composable(route = AppScreen.LOGIN.value) {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                    appNavHost.navigate(AppScreen.AISLE_MAP.value) {
                        popUpTo(AppScreen.LOGIN.value) { inclusive = true }
                    }
                }
            )
        }
        composable(route = AppScreen.AISLE_MAP.value) {
            AisleMapScreen(
                vm = mapVm,
                appNavHost = appNavHost,
                onScanClick = {
                    appNavHost.navigate(AppScreen.MAIN.value)
                },
                onLogout = {
                    isLoggedIn = false
                    appNavHost.navigate(AppScreen.LOGIN.value) {
                        popUpTo(AppScreen.AISLE_MAP.value) { inclusive = true }
                    }
                }
            )
        }
        composable(route = AppScreen.MAIN.value) {
            MainScreenLayout(
                vm = vm,
                appNavHost = appNavHost,
                isLoggedIn = isLoggedIn,
                onLogout = {
                    isLoggedIn = false
                    appNavHost.navigate(AppScreen.LOGIN.value) {
                        popUpTo(AppScreen.MAIN.value) { inclusive = true }
                    }
                }
            )
        }
        composable(route = AppScreen.RESULT.value) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id")?.toInt() ?: 0
            QRCodeResultLayout(
                id, appNavHost, hiltViewModel(),
                {
                    vm.resetScanQR()
                }, { barCode ->
                    barCode?.let { vm.handleBarcodeResult(it) }
                }, {
                    // handle copy
                    vm.handleCopyText(it)
                }, {
                    // handle share
                    vm.handleShareText(it)
                })
        }
        composable(route = AppScreen.SETTING.value) {
            SettingScreenLayout(appNav = appNavHost)
        }
        composable(route = AppScreen.HISTORY.value) {
            HistoryScreenLayout(appNav = appNavHost)
        }
        composable(route = AppScreen.PREMIUM.value) {
            PremiumScreenLayout(appNav = appNavHost)
        }
    }
}

enum class AppScreen(val value: String) {
    LOGIN("login"),
    AISLE_MAP("aisle_map"),
    MAIN("main"),
    SETTING("setting"),
    PREMIUM("premium"),
    HISTORY("history"),
    RESULT("result/{id}")
}