package com.toiletgen.app

import android.app.Application
import com.toiletgen.app.di.appModule
import com.yandex.mapkit.MapKitFactory
import com.toiletgen.core.database.databaseModule
import com.toiletgen.core.network.networkModule
import com.toiletgen.feature.achievements.di.achievementsFeatureModule
import com.toiletgen.feature.auth.di.authFeatureModule
import com.toiletgen.feature.chat.di.chatFeatureModule
import com.toiletgen.feature.entertainment.di.entertainmentFeatureModule
import com.toiletgen.feature.map.di.mapFeatureModule
import com.toiletgen.feature.profile.di.profileFeatureModule
import com.toiletgen.feature.sos.di.sosFeatureModule
import com.toiletgen.feature.toilet_details.di.toiletDetailsFeatureModule
import com.toiletgen.feature.yearly_report.di.yearlyReportFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ToiletGenApp : Application() {

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        startKoin {
            androidLogger()
            androidContext(this@ToiletGenApp)
            modules(
                // Core
                appModule,
                networkModule(BuildConfig.API_BASE_URL),
                databaseModule,

                // Features
                authFeatureModule,
                mapFeatureModule,
                toiletDetailsFeatureModule,
                sosFeatureModule,
                profileFeatureModule,
                achievementsFeatureModule,
                yearlyReportFeatureModule,
                entertainmentFeatureModule,
                chatFeatureModule,
            )
        }
    }
}
