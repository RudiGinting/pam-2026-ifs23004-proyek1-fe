package org.delcom.pam_proyek1_ifs23004.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.delcom.pam_proyek1_ifs23004.prefs.AuthTokenPref

@Module
@InstallIn(SingletonComponent::class)
object PrefModule {
    @Provides
    fun provideAuthTokenPref(@ApplicationContext context: Context): AuthTokenPref {
        return AuthTokenPref(context)
    }
}