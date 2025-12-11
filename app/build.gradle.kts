plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
	id("com.google.dagger.hilt.android")
	id("com.google.devtools.ksp")
	id("com.google.gms.google-services")
}

android {
	namespace = "com.cericatto.skillpulse"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.cericatto.skillpulse"
		minSdk = 26
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
}

dependencies {

	// Compose
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)

	// Firebase
	implementation(libs.firebase.auth)
	implementation(libs.firebase.firestore)

	// Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)
	implementation(libs.hilt.navigation.compose)

	// Navigation
	implementation(libs.navigation.compose)
	implementation(libs.kotlinx.serialization.json)

	// Timber
	implementation(libs.timber)

	// Material Icons
	implementation(libs.material.icons.extended)

	// Tests
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}