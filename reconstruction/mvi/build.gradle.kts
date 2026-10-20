plugins { alias(libs.plugins.kotlin.jvm); `java-library` }
kotlin {
    jvmToolchain(21)
}
dependencies {
    api(project(":search"))
    api(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.launcher)
}
tasks.test { useJUnitPlatform() }
