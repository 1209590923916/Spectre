plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "dev.thomasbuilds.spectre"
  compileSdk {
    version = release(37)
  }

  defaultConfig {
    applicationId = "zero.spectre"
    minSdk = 31
    targetSdk = 37
    versionCode = 103999
    versionName = "0.1.3"
    vectorDrawables.useSupportLibrary = true
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      vcsInfo { include = false }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
  }
  buildFeatures {
    compose = true
  }

  lint {
    checkAllWarnings = true
    disable += setOf("SyntheticAccessor", "LogConditional", "MemberExtensionConflict")
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = false
  }

  bundle {
    language { enableSplit = false }
    density { enableSplit = false }
    abi { enableSplit = false }
  }

  packaging {
    jniLibs {
      keepDebugSymbols += "**/*.so"
    }
  }
}

base {
  archivesName = "spectre"
}

tasks.configureEach {
  if (name.contains("ArtProfile")) enabled = false
}

kotlin {
  jvmToolchain(25)
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.process)
}
