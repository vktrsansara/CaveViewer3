package com.vktrsansara.app.caveviewer.di

import com.vktrsansara.app.caveviewer.data.repository.SettingsRepositoryImpl
import com.vktrsansara.app.caveviewer.domain.repository.SettingsRepository
import com.vktrsansara.app.caveviewer.presentation.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }
    viewModel { MainViewModel(get()) }
}
