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
	fun provideRemoteDatabase(db: FirebaseFirestore): RemoteDatabase {
		return FirebaseRemoteDatabase(db)
	}

	/*
	@Provides
	@Singleton
	fun provideOkHttpClient(): OkHttpClient {
		val loggingInterceptor = HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}

		return OkHttpClient.Builder()
			.addInterceptor(
				HttpLoggingInterceptor().apply {
					level = HttpLoggingInterceptor.Level.BODY
				}
			)
			.addInterceptor(loggingInterceptor)
			.build()
	}

	@Provides
	@Singleton
	fun provideRickMortyApi(
		client: OkHttpClient,
		moshi: Moshi
	): RickMortyApi {
		return Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(MoshiConverterFactory.create(moshi))
			.client(client)
			.build()
			.create()
	}

	@Provides
	@Singleton
	fun provideRepository(
		db: RedditDatabase,
		api: RickMortyApi
	): RickMortyRepository {
		return RickMortyRepositoryImpl(
			dao = db.dao,
			api = api
		)
	}

	@Provides
	@Singleton
	fun provideRickMortyDatabase(app: Application): RedditDatabase {
		return Room.databaseBuilder(
			app,
			RedditDatabase::class.java,
			"reddit_db"
		).build()
	}

	@Provides
	@Singleton
	fun provideMoshi(): Moshi {
		return Moshi.Builder()
			.add(KotlinJsonAdapterFactory())
			.build()
	}
	 */
}