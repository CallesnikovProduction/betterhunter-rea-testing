plugins { alias(libs.plugins.kotlin.jvm) }
kotlin {
    jvmToolchain(21)
}
dependencies {
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.launcher)
}
tasks.test { useJUnitPlatform() }
