package me.func

enum class Arcades(val title: String, val address: String, val slots: Int, val queue: String) {
    DBD(
        "DeadByDaylight",
        "noop",
        6,
        ""
    ),
    MUR(
        "MurderMystery",
        "murder",
        12,
        ""
    ),
    SQD(
        "Игра в кальмара",
        "squid-game1",
        30,
        "7188b5b2-43b3-40fc-bcd0-abeea3883490"
    ),
    PILL("Столбы", "pillars-classic", 7, "95a1b3e4-5bae-4957-8df2-006e233f3004"),
    SHW(
        "SheepWars",
        "sheepwars",
        16,
        "3089411e-2c69-11e8-b5ea-1cb72caa35fd"
    ),
    AMG("Among Us", "amongus", 10, "845e92f3-7006-11ea-acca-1cb72caa35fd"),
    TJL("Том и Джери", "noop", 10, ""),
    TTG("TntTag", "tnttag", 16, ""),
    LZT("LazerTag", "lazertag", 10, ""),
    ARC("Бесконечные аркады", "arcade", 24, ""),
    SKY("SkyControl", "skycontrol", 12, "2bd88cc8-603c-11ec-acca-1cb72caa35fd"),
}
