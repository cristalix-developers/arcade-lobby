package me.func

import me.func.donate.DonatePosition
import me.func.donate.impl.*
import me.func.mod.battlepass.BattlePass
import me.func.mod.battlepass.BattlePass.sale
import me.func.mod.battlepass.BattlePassPageAdvanced
import me.func.protocol.battlepass.BattlePassUserData
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BattlePassManager {

    private val battlepass = BattlePass.new(399) {
        pages = mutableListOf(
            BattlePassPageAdvanced(
                300,
                10,
                listOf(
                    NameTag.TAG1.getIcon(),
                    ArrowParticle.SLIME.getIcon(),
                    ArrowParticle.WATER_DROP.getIcon(),
                    StepParticle.SLIME.getIcon(),
                    Mask.HOUSTON.getIcon(),
                    KillMessage.GLOBAL.getIcon(),
                    MoneyKit.SMALL.getIcon(),
                    ArrowParticle.FALLING_DUST.getIcon(),
                    ArrowParticle.SPELL_INSTANT.getIcon(),
                    Mask.JASON.getIcon(),
                ),
                listOf(
                    NameTag.TAG3.getIcon(),
                    Mask.TRADEGY.getIcon(),
                    Mask.HORROR.getIcon(),
                    MoneyKit.SMALL.getIcon(),
                    NameTag.TAG28.getIcon(),
                    Mask.COMEDY_MASK.getIcon(),
                    KillMessage.END.getIcon(),
                    ArrowParticle.REDSTONE.getIcon(),
                    StepParticle.REDSTONE.getIcon(),
                    Corpse.G1.getIcon(),
                ),
            ), BattlePassPageAdvanced(
                600,
                20,
                listOf(
                    KillMessage.DEAD.getIcon(),
                    MoneyKit.SMALL.getIcon(),
                    Mask.SCREAM.getIcon(),
                    NameTag.TAG10.getIcon(),
                    StepParticle.REDSTONE.getIcon(),
                    KillMessage.ROOM.getIcon(),
                    Mask.DALLAS.getIcon(),
                    NameTag.TAG16.getIcon(),
                    Mask.CREWMATE_LIME.getIcon(),
                    MoneyKit.NORMAL.getIcon()
                ),
                listOf(
                    MoneyKit.SMALL.getIcon(),
                    KillMessage.SLEEP.getIcon(),
                    Mask.CREWMATE_WHITE.getIcon(),
                    NameTag.TAG17.getIcon(),
                    Mask.JASON.getIcon(),
                    ArrowParticle.VILLAGER_ANGRY.getIcon(),
                    NameTag.TAG20.getIcon(),
                    MoneyKit.NORMAL.getIcon(),
                    Mask.CREWMATE_PURPLE.getIcon(),
                    StepParticle.VILLAGER_ANGRY.getIcon()
                ),
            )
        )
        sale(50.0)
        facade.tags.add("Выполняйте квесты - получайте призы!")
        facade.tags.add("BattlePass завершится в 01.04.2022")
    }

    fun show(player: Player) {
        BattlePass.send(player, battlepass)
        BattlePass.show(player, battlepass, BattlePassUserData(100, false))
    }
}