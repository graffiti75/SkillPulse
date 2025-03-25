package com.cericatto.skillpulse.di

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
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