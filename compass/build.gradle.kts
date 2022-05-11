plugins {
    id("anime.mod-bundler")
}

dependencies {	
    compileOnly("ru.cristalix:client-api:4.0-SNAPSHOT")
    implementation("ru.cristalix:uiengine:4.0-SNAPSHOT")
    implementation("ru.cristalix:client-sdk:4.0-SNAPSHOT")

    implementation("implario:humanize:1.1.3")
    implementation("dev.implario.games5e:commons:2.1.4")
}

mod {
    name = "Compass"
    main = "Games5eMod"
    version = "1.0"
    author = "func"
}
