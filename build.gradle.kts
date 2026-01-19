// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Añado dependencias para para el servicio de google gradle plugin (firebase)
    id("com.google.gms.google-services") version "4.4.4" apply false

}