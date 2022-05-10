import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.21" apply false
}

allprojects {
    group = "me.func"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }

    tasks {
        withType<Jar> { duplicatesStrategy = DuplicatesStrategy.FAIL }
        withType<JavaCompile> { options.encoding = "UTF-8" }
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf(
                    "-Xlambdas=indy",
                    "-Xno-param-assertions",
                    "-Xno-receiver-assertions",
                    "-Xno-call-assertions",
                    "-Xbackend-threads=0",
                    "-Xuse-ir",
                    "-Xassertions=always-disable",
                    "-Xuse-fast-jar-file-system",
                    "-Xsam-conversions=indy",
                    "-Xcontext-receivers",
                    "-Xskip-prerelease-check"
                )
            }
        }
    }
}
