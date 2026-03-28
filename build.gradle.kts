// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    plugins {
        id("org.sonarqube") version "7.2.3.7755"
    }

    sonar {
        properties {
            property("sonar.projectKey", "weltreise-dora-the-explorer_Weltreise_App")
            property("sonar.organization", "weltreise-dora-the-explorer")
        }
    }
}