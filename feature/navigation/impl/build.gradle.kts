plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sona.feature.navigation.impl"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:audio:api"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))

    implementation(project(":feature:navigation:api"))
    implementation(project(":feature:splashscreen:impl"))
    implementation(project(":feature:home:impl"))
    implementation(project(":feature:searchmusic:impl"))
    implementation(project(":feature:favorites:impl"))
    implementation(project(":feature:profile:impl"))
    implementation(project(":feature:settings:impl"))
    implementation(project(":feature:detailmusic:impl"))
    implementation(project(":feature:musiclist:impl"))
    implementation(project(":feature:player:impl"))
    implementation(project(":feature:artist:impl"))
    implementation(project(":feature:comments:impl"))
    implementation(project(":feature:explore:impl"))
    implementation(project(":feature:charts:impl"))
    implementation(project(":feature:playlistdetail:impl"))
    implementation(project(":feature:artistcommunity:impl"))
    implementation(project(":feature:artistfavorites:impl"))
    implementation(project(":feature:tracktools:impl"))

    implementation(project(":feature:splashscreen:api"))
    implementation(project(":feature:home:api"))
    implementation(project(":feature:searchmusic:api"))
    implementation(project(":feature:favorites:api"))
    implementation(project(":feature:profile:api"))
    implementation(project(":feature:settings:api"))
    implementation(project(":feature:detailmusic:api"))
    implementation(project(":feature:musiclist:api"))
    implementation(project(":feature:player:api"))
    implementation(project(":feature:artist:api"))
    implementation(project(":feature:comments:api"))
    implementation(project(":feature:explore:api"))
    implementation(project(":feature:charts:api"))
    implementation(project(":feature:playlistdetail:api"))
    implementation(project(":feature:artistcommunity:api"))
    implementation(project(":feature:artistfavorites:api"))
    implementation(project(":feature:tracktools:api"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.ui)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.javax.inject)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
