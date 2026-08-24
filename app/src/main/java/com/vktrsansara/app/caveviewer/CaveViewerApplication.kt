package com.vktrsansara.app.caveviewer

import android.app.Application
import com.vktrsansara.app.caveviewer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CaveViewerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CaveViewerApplication)
            modules(appModule)
        }
    }
}
