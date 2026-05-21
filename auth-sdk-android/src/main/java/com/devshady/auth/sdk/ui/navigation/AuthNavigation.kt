package com.devshady.auth.sdk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.devshady.auth.sdk.di.ServiceLocator
import com.devshady.auth.sdk.ui.screens.OtpVerificationScreen
import com.devshady.auth.sdk.ui.screens.OtpVerificationViewModel
import com.devshady.auth.sdk.ui.screens.PhoneEntryScreen
import com.devshady.auth.sdk.ui.screens.PhoneEntryViewModel
import com.devshady.auth.sdk.ui.screens.SuccessScreen

@Composable
fun AuthNavigation(
    backStack: NavBackStack<AuthNavKey>,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ServiceLocator.provideAuthRepository(context) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                is PhoneEntryKey -> NavEntry(key) {
                    val viewModel: PhoneEntryViewModel = viewModel {
                        PhoneEntryViewModel(repository)
                    }
                    PhoneEntryScreen(
                        viewModel = viewModel,
                        onNext = { phoneNumber ->
                            backStack.add(OtpVerificationKey(phoneNumber))
                        }
                    )
                }
                is OtpVerificationKey -> NavEntry(key) {
                    val viewModel: OtpVerificationViewModel = viewModel {
                        OtpVerificationViewModel(
                            repository,
                            ServiceLocator.provideSmsRetrieverHelper(context)
                        )
                    }
                    val onSuccess: () -> Unit = {
                        backStack.add(AuthSuccessKey)
                    }
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        viewModel.startSmsRetriever(key.phoneNumber, onSuccess)
                    }
                    OtpVerificationScreen(
                        phoneNumber = key.phoneNumber,
                        viewModel = viewModel,
                        onSuccess = onSuccess
                    )
                }
                is AuthSuccessKey -> NavEntry(key) {
                    SuccessScreen(
                        onContinue = onFinish
                    )
                }
            }
        }
    )
}
