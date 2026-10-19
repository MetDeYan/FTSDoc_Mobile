package com.ftsdoc.mobile.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.ftsdoc.mobile.di.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("Контейнер приложения не найден")
}
