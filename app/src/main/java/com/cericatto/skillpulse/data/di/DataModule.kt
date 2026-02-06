package com.cericatto.skillpulse.data.di

import android.app.Application
import android.content.Context
import com.cericatto.skillpulse.data.auth.FirebaseUserAuthentication
import com.cericatto.skillpulse.data.remote.FirebaseRemoteDatabase
import com.cericatto.skillpulse.domain.auth.UserAuthentication
import com.cericatto.skillpulse.domain.remote.RemoteDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

	@Provides
	@Singleton
	fun provideContext(
		app: Application
	): Context {
		return app.applicationContext
	}

	@Provides
	@Singleton
	fun provideFirebaseAuth(): FirebaseAuth {
		return FirebaseAuth.getInstance()
	}

	@Provides
	@Singleton
	fun provideFirebaseFirestore(): FirebaseFirestore {
		return FirebaseFirestore.getInstance()
	}

	@Provides
	@Singleton
	fun provideUserAuthentication(auth: FirebaseAuth): UserAuthentication {
		return FirebaseUserAuthentication(auth)
	}

	@Provides
	@Singleton
	fun provideRemoteDatabase(
		db: FirebaseFirestore,
		userAuth: UserAuthentication
	): RemoteDatabase {
		return FirebaseRemoteDatabase(db, userAuth)
	}
}