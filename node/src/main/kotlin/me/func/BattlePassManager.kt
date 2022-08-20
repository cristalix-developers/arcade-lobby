package me.func

import dev.implario.bukkit.item.item
import me.func.battlepass.BattlePassUtil
import me.func.donate.DonatePosition
import me.func.donate.impl.*
import me.func.misc.PersonalizationMenu
import me.func.mod.Anime
import me.func.mod.battlepass.BattlePass
import me.func.mod.battlepass.BattlePass.onBuyAdvanced
import me.func.mod.battlepass.BattlePass.onBuyPage
import me.func.mod.battlepass.BattlePass.sale
import me.func.mod.battlepass.BattlePassPageAdvanced
import me.func.mod.conversation.ModTransfer
import me.func.protocol.DropRare
import me.func.protocol.battlepass.BattlePassUserData
import me.func.util.Music
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import pw.lach.p13n.network.tower.GiveModelToUserPackage
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

const val BATTLEPASS_PRICE = 399
const val BATTLEPASS_SALE_PERCENT = 35

object BattlePassManager {

    private val premium = item {
        type = Material.CLAY_BALL
        text("§6Персонализация премиальный джойстик")
        nbt("p13nModelId", "e28387d9-c465-41a5-871b-7f27fd26076d")
        nbt("rare", DropRare.LEGENDARY.ordinal)
    }.build()

    private val classic = item {
        type = Material.CLAY_BALL
        text("§bПерсонализация джойстик")
        nbt("p13nModelId", "1a4caaf5-77bc-4d7f-9302-6b2fcb510a6a")
        nbt("rare", DropRare.LEGENDARY.ordinal)
    }.build()

    val rewards = listOf(
        listOf(
            MoneyKit.SMALL,
            NameTag.TAG1,
            Mask.COMEDY_MASK,
            MoneyKit.SMALL,
            NameTag.TAG3,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.HORROR
        ) to listOf(
            NameTag.TAG2,
            MoneyKit.SMALL,
            NameTag.TAG4,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            Mask.TRADEGY,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            ArrowParticle.SLIME
        ), listOf(
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.GAS_MASK,
            MoneyKit.SMALL,
            GraffitiUnit,
            MoneyKit.SMALL,
            NameTag.TAG4,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Corpse.G1
        ) to listOf(
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            NameTag.TAG5,
            MoneyKit.SMALL,
            ArrowParticle.SLIME,
            MoneyKit.SMALL,
            NameTag.TAG6,
            MoneyKit.SMALL,
            LootboxUnit
        ), listOf(
            MoneyKit.SMALL,
            KillMessage.GLOBAL,
            MoneyKit.SMALL,
            ArrowParticle.WATER_DROP,
            MoneyKit.SMALL,
            NameTag.TAG7,
            MoneyKit.SMALL,
            GraffitiUnit,
            MoneyKit.SMALL,
            MoneyKit.SMALL
        ) to listOf(
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            StepParticle.SLIME,
            MoneyKit.SMALL,
            NameTag.TAG8,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            KillMessage.AHEAD
        ), listOf(
            MoneyKit.SMALL,
            NameTag.TAG9,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            KillMessage.END,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            GraffitiUnit,
            MoneyKit.SMALL
        ) to listOf(
            MoneyKit.SMALL,
            NameTag.TAG10,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            NameTag.TAG11,
            MoneyKit.SMALL,
            Mask.DALLAS,
            MoneyKit.SMALL,
            StepParticle.FALLING_DUST,
            MoneyKit.SMALL
        ), listOf(
            MoneyKit.SMALL,
            NameTag.TAG12,
            MoneyKit.SMALL,
            KillMessage.SLEEP,
            MoneyKit.SMALL,
            ArrowParticle.SPELL_INSTANT,
            MoneyKit.SMALL,
            Mask.HOUSTON,
            MoneyKit.SMALL,
            NameTag.TAG13
        ) to listOf(
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.CHAINS,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            KillMessage.HORNY,
            MoneyKit.SMALL,
            NameTag.TAG14,
            MoneyKit.NORMAL
        ), listOf(
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            NameTag.TAG15,
            MoneyKit.SMALL,
            ArrowParticle.REDSTONE,
            LootboxUnit,
            MoneyKit.SMALL,
            NameTag.TAG16,
            MoneyKit.SMALL,
            Mask.FOXI
        ) to listOf(
            MoneyKit.SMALL,
            Mask.CHICA,
            MoneyKit.SMALL,
            StepParticle.REDSTONE,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.BONNIE,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            NameTag.TAG17
        ), listOf(
            MoneyKit.SMALL,
            NameTag.TAG18,
            MoneyKit.SMALL,
            ArrowParticle.VILLAGER_ANGRY,
            MoneyKit.NORMAL,
            StepParticle.VILLAGER_ANGRY,
            MoneyKit.SMALL,
            Mask.DIVER_HELMET,
            MoneyKit.SMALL,
            Corpse.G2
        ) to listOf(
            MoneyKit.SMALL,
            Mask.RAPHAEL,
            MoneyKit.SMALL,
            StepParticle.VILLAGER_ANGRY,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            Mask.MICHELANGELO
        ), listOf(
            MoneyKit.SMALL,
            NameTag.TAG19,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.LEONARDO,
            KillMessage.ROOM,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            NameTag.TAG21,
            ArrowParticle.SPELL_WITCH
        ) to listOf(
            MoneyKit.SMALL,
            NameTag.TAG20,
            MoneyKit.NORMAL,
            ArrowParticle.VILLAGER_HAPPY,
            MoneyKit.SMALL,
            StepParticle.FLAME,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            KillMessage.BLACK,
            MoneyKit.SMALL
        ), listOf(
            MoneyKit.NORMAL,
            NameTag.TAG22,
            MoneyKit.SMALL,
            Mask.DONATELLO,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.SCREAM,
            MoneyKit.SMALL,
            NameTag.TAG23,
            MoneyKit.SMALL
        ) to listOf(
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            Mask.CREWMATE_ORANGE,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            Mask.CREWMATE_WHITE,
            MoneyKit.SMALL,
            NameTag.TAG24
        ), listOf(
            MoneyKit.SMALL,
            GraffitiUnit,
            MoneyKit.SMALL,
            NameTag.TAG25,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            Mask.CREWMATE_YELLOW,
            MoneyKit.NORMAL,
            NameTag.TAG26,
            MoneyKit.SMALL
        ) to listOf(
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            ArrowParticle.LAVA,
            MoneyKit.SMALL,
            NameTag.TAG27,
            MoneyKit.SMALL,
            KillMessage.X,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            MoneyKit.SMALL
        ), listOf(
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            NameTag.TAG28,
            MoneyKit.SMALL,
            GraffitiUnit,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            NameTag.TAG29,
            MoneyKit.SMALL,
            Mask.JASON
        ) to listOf(
            MoneyKit.SMALL,
            ArrowParticle.NOTE,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            NameTag.TAG30,
            MoneyKit.SMALL,
            Mask.EMOJI,
            MoneyKit.BIG,
            MoneyKit.SMALL,
            NameTag.TAG31
        ), listOf(
            MoneyKit.SMALL,
            Mask.FREDDY,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            ArrowParticle.HEAR,
            MoneyKit.NORMAL,
            NameTag.TAG32,
            Corpse.G3
        ) to listOf(
            MoneyKit.SMALL,
            Mask.ONI,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            KillMessage.KIRA,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.NORMAL,
            LootboxUnit,
            MoneyKit.BIG
        ), listOf(
            MoneyKit.BIG,
            Mask.CREWMATE_RED,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            NameTag.TAG33,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            GraffitiUnit,
            MoneyKit.SMALL
        ) to listOf(
            MoneyKit.SMALL,
            MoneyKit.BIG,
            Mask.CREWMATE_PURPLE,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            Mask.CREWMATE_PINK,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            NameTag.TAG34,
            LootboxUnit
        ), listOf(
            MoneyKit.SMALL,
            NameTag.TAG35,
            MoneyKit.NORMAL,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            NameTag.TAG36,
            MoneyKit.SMALL,
            MoneyKit.BIG,
            Mask.CREWMATE_LIME
        ) to listOf(
            MoneyKit.SMALL,
            Mask.SAW,
            MoneyKit.SMALL,
            MoneyKit.BIG,
            LootboxUnit,
            Corpse.G4,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            NameTag.TAG37,
            MoneyKit.BIG
        ), listOf(
            MoneyKit.SMALL,
            MoneyKit.BIG,
            NameTag.TAG38,
            MoneyKit.SMALL,
            Mask.AHRI,
            MoneyKit.SMALL,
            MoneyKit.NORMAL,
            LootboxUnit,
            MoneyKit.SMALL,
            object : DonatePosition {
                override fun getIcon() = classic
                override fun getName() = classic.itemMeta.displayName
                override fun getPrice() = 999
                override fun getRare() = DropRare.LEGENDARY
                override fun getTitle() = classic.itemMeta.displayName
                override fun isActive(player: ArcadeUserData) = true
                override fun give(arcadeUserData: ArcadeUserData) {
                    CoreApi.get().socketClient.write(
                        GiveModelToUserPackage(
                            arcadeUserData.uuid, UUID.fromString("1a4caaf5-77bc-4d7f-9302-6b2fcb510a6a")
                        )
                    )
                }
            }
        ) to listOf(
            MoneyKit.NORMAL,
            NameTag.TAG39,
            MoneyKit.SMALL,
            Mask.TIK_TOK,
            MoneyKit.SMALL,
            Mask.STAR_PLATINUM,
            MoneyKit.SMALL,
            StepParticle.NOTE,
            MoneyKit.BIG,
            object : DonatePosition {
                override fun getIcon() = premium
                override fun getName() = premium.itemMeta.displayName
                override fun getPrice() = 999
                override fun getRare() = DropRare.LEGENDARY
                override fun getTitle() = premium.itemMeta.displayName
                override fun isActive(player: ArcadeUserData) = true
                override fun give(arcadeUserData: ArcadeUserData) {
                    CoreApi.get().socketClient.write(
                        GiveModelToUserPackage(
                            arcadeUserData.uuid, UUID.fromString("e28387d9-c465-41a5-871b-7f27fd26076d")
                        )
                    )
                }
            }
        )
    )

    private val battlepass = BattlePass.new(BATTLEPASS_PRICE) {
        pages = rewards.mapIndexed { index, drop ->
            BattlePassPageAdvanced(
                100 + 25 * index,
                10 + index,
                drop.first.map { it.getIcon() },
                drop.second.map { it.getIcon() }
            )
        }.toMutableList()
        sale(BATTLEPASS_SALE_PERCENT.toDouble())
        onBuyAdvanced { player ->
            Arcade.getArcadeData(player).progress?.let { data ->
                player.closeInventory()

                if (data.advanced) {
                    return@onBuyAdvanced
                }

                buy(
                    player,
                    BATTLEPASS_PRICE - (BATTLEPASS_PRICE * BATTLEPASS_SALE_PERCENT / 100.0).toInt(),
                    "Покупка премиум адркадного BattlePass'а."
                ) {
                    data.advanced = true
                    Anime.itemTitle(player, premium, "§bУспешно", "Собирайте награды!", 3.5)
                    Bukkit.getOnlinePlayers().forEach {
                        Anime.topMessage(
                            it,
                            Formatting.fine("§e${player.name} §fкупил §bПремиум §6BattlePass§f!")
                        )
                        Music.BONUS.sound(it)
                        it.sendMessage("")
                        it.sendMessage(Formatting.fine("§7Игрок §e${player.name} §7купил §bпремиум §6BattlePass§7!"))
                        it.sendMessage("")
                    }
                }
            }
        }
        onBuyPage { player, cost ->
            player.closeInventory()

            Arcade.getArcadeData(player).progress?.let { data ->
                pages.firstOrNull { (it.skipPrice - it.skipPrice * BATTLEPASS_SALE_PERCENT / 100.0).toInt() == cost }
                    ?.let { page ->
                        buy(player, cost, "Пропуск уровня аркадного BattlePass.") {
                            data.exp += page.requiredExp
                            Anime.itemTitle(player, classic, "§bУспешно", "Новый уровень", 2.6)
                            Music.BONUS2.sound(player)
                            Bukkit.getOnlinePlayers().forEach {
                                Anime.topMessage(
                                    it,
                                    Formatting.fine("§e${player.name} §fпропустил страницу §6BattlePass§f!")
                                )
                            }
                        }
                    }
            }
        }

        facade.tags.add("Выполняйте квесты - получайте призы!")
        facade.tags.add("BattlePass завершится 01.12.2022")
        questStatusUpdater = Function<Player, List<String>> { player ->
            BattlePassUtil.getQuestLore(player)
        }
    }

    init {
        Anime.createReader("bp:reward") { player, buffer ->
            val advanced = buffer.readBoolean()
            val page = buffer.readInt()
            val index = buffer.readInt()
            val data = Arcade.getArcadeData(player)

            data.progress?.let {
                if (advanced && !it.advanced)
                    return@createReader
                var exp = it.exp
                var level = 1

                for (current in battlepass.pages) {
                    for (item in current.items) {
                        if (exp >= current.requiredExp) {
                            exp -= current.requiredExp
                            level++
                        } else break
                    }
                }

                val position =
                    (if (advanced) battlepass.pages.size * battlepass.pages.first().items.size else 0) + page * 10 + index

                data.claimedRewards?.let { claimed ->
                    if (claimed.contains(position))
                        return@createReader

                    if (level > page * 10 + index) {
                        val reward = (if (advanced) rewards[page].second else rewards[page].first)[index]

                        if (reward is GraffitiUnit) {
                            Anime.killboardMessage(player, Formatting.fine("Награду можно забрать потом."))
                            return@createReader
                        }

                        reward.give(data)
                        claimed.add(position)
                        Music.RARE_ITEM.sound(player)
                        Anime.killboardMessage(player, Formatting.fine("Награда: " + reward.getTitle()))
                    }
                }
            }
        }
    }

    fun buy(player: Player, price: Int, desc: String, successfully: Consumer<Player>) {
        ISocketClient.get().writeAndAwaitResponse<MoneyTransactionResponsePackage>(
            MoneyTransactionRequestPackage(
                player.uniqueId,
                price,
                true,
                desc
            )
        ).thenAccept {
            if (it.errorMessage.isNullOrEmpty()) {
                successfully.accept(player)
                Arcade.save(player.uniqueId)
            } else {
                Anime.killboardMessage(player, "Ошибка! " + it.errorMessage)
            }
        }
    }

    fun show(player: Player) {
        BattlePass.send(player, battlepass)
        val data = Arcade.getArcadeData(player)
        var progress = data.progress

        if (progress == null)
            progress = BattlePassUserData(5, false)

        // Back Door
        ModTransfer(battlepass.uuid.toString()).apply {
            integer(data.claimedRewards?.size ?: 0)
            data.claimedRewards?.forEach { integer(it) }
        }.send("bp:claimed", player)

        BattlePass.show(player, battlepass, progress)
    }
}