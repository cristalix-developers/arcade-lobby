plugins {
    id("dev.implario.bundler")
}

dependencies {
    compileOnly("ru.cristalix:client-api:4.0-SNAPSHOT")
    implementation("ru.cristalix:uiengine:4.0-SNAPSHOT")
    implementation("ru.cristalix:client-sdk:4.0-SNAPSHOT")

    implementation("implario:humanize:1.1.3")
    implementation("dev.implario.games5e:commons:2.1.4")
}

tasks {
    jar {
        from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })

        include("**/*.class", "*.class", "mod.properties", "*.png", "**/*.png")
    }
    bundle {
        optimizationpasses(10)
    }
}

bundler {
    name = "Compass"
    mainClass = "me.func.compass.Games5eMod"
    version = "1.2"
    author = "func"
}
