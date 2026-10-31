plugins {
    alias(libs.plugins.android.library)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sona.core.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:common"))

    api(project(":core:data:alarms"))
    api(project(":core:data:apps"))
    api(project(":core:data:battery"))
    api(project(":core:data:bluetooth"))
    api(project(":core:data:browser"))
    api(project(":core:data:clipboard"))
    api(project(":core:data:contacts"))
    api(project(":core:data:device"))
    api(project(":core:data:display"))
    api(project(":core:data:documents"))
    api(project(":core:data:downloads"))
    api(project(":core:data:gallery"))
    api(project(":core:data:hardware"))
    api(project(":core:data:location"))
    api(project(":core:data:media"))
    api(project(":core:data:network"))
    api(project(":core:data:nfc"))
    api(project(":core:data:notifications"))
    api(project(":core:data:performance"))
    api(project(":core:data:permissions"))
    api(project(":core:data:sensors"))
    api(project(":core:data:sms"))
    api(project(":core:data:sound"))
    api(project(":core:data:storage"))
    api(project(":core:data:usage"))
    api(project(":core:data:usb"))
    api(project(":core:data:vpn"))
    api(project(":core:data:wifi"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
}
