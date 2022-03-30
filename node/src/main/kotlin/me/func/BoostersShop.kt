package me.func

import me.func.boost.ArcadeBoosters
import me.func.boosterapi.protocol.BoosterInfo
import me.func.protocol.ArcadeNewBoostersPackage
import ru.cristalix.core.network.ISocketClient
import java.util.*

object BoostersShop {

    fun addNewBooster(owner: UUID, key: String, factor: Double, cost: Long) {
        val currentBoosters = ArcadeBoosters.boosterValues
        currentBoosters[key] ?: currentBoosters.put(key, arrayListOf())
        currentBoosters[key]?.add(BoosterInfo(owner, factor, System.currentTimeMillis(), cost))
        ISocketClient.get().write(ArcadeNewBoostersPackage(currentBoosters))
    }
}