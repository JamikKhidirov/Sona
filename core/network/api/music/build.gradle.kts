plugins {
    alias(libs.plugins.android.library)

    id("com.google.devtools.ksp")


}

android {
    namespace = "com.example.music"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures{
        buildConfig = true
    }
}

dependencies {


    implementation("com.google.dagger:dagger:2.60.1")

    ksp("com.google.dagger:dagger-compiler:2.60.1")


    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)


    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.5.0")

    //Gson
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
}