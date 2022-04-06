package me.func

import dev.implario.bukkit.entity.StandHelper
import dev.implario.bukkit.item.item
import implario.humanize.Humanize
import me.func.donate.DonatePosition
import me.func.donate.impl.*
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.conversation.ModTransfer
import me.func.protocol.DropRare
import me.func.protocol.element.Banner
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.TabCompleteEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider

object LootBoxManager : Listener {

    data class LootBox(
        val origin: Location,
        val stand: ArmorStand = StandHelper(origin)
            .gravity(false)
            .invisible(false)
            .name("§e§lКЛИК")
            .build()
            .apply {
                (this as CraftArmorStand).handle
                    .setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(chest))
            },
        val banner: Banner = Banner.Builder()
            .x(origin.x)
            .y(origin.y + 3.6)
            .z(origin.z + 2.0)
            .yaw(-90f)
            .pitch(10f)
            .weight(65)
            .height(14)
            .resizeLine(0, 0.5)
            .resizeLine(1, 0.5)
            .build()
    )

    private val chest = item {
        type = Material.ENDER_CHEST
    }.build()
    private val lootbox = app.map.getLabels("lootbox").map { LootBox(it.clone().add(1.0, -0.6, 1.0)) }
    private const val lootboxPrice = 192
    private val lootboxItem = item {
        type = Material.CLAY_BALL
        nbt("other", "enderchest1")
        text(
            "§bЛутбокс\n\n§7Откройте и получите\n§7псевдоним, частицы ходьбы\n§7следы от стрелы, маски\n§7или скин могилы!\n\n§e > §f㜰 §aОткрыть сейчас за\n${
                me.func.donate.MoneyFormatter.texted(lootboxPrice)
            }"
        )
    }.build()
    private val dropList = Corpse.values().map { it }
        .plus(NameTag.values())
        .plus(StepParticle.values())
        .plus(KillMessage.values())
        .plus(ArrowParticle.values())
        .plus(Mask.values())
        .filter { it != KillMessage.NONE && it != Corpse.NONE && it != NameTag.NONE && it != StepParticle.NONE && it != ArrowParticle.NONE && it != Mask.NONE }

    private val lootboxMenu = ControlledInventory.builder()
        .title("Ваши лутбоксы")
        .rows(5)
        .columns(9)
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                Arcade.get(player)?.let {
                    val donate = Arcade.getArcadeData(player)

                    contents.setLayout(
                        "XOOOOOOOX",
                        "XOOOOOOOX",
                        "XOOOPOOOX",
                        "XOOOOOOOX",
                        "XOOOOOOOX",
                    )

                    repeat(minOf(it.crates, contents.size('O'))) {
                        contents.add('O', ClickableItem.of(lootboxItem) {
                            player.closeInventory()
                            val balance = Arcade.getMoney(player)
                            if (balance < lootboxPrice) {
                                Anime.killboardMessage(player, Formatting.error("Не хватает жетонов :("))
                                return@of
                            }
                            Arcade.setMoney(player, balance - lootboxPrice)
                            Arcade.openLootbox(player.uniqueId)

                            var drop = dropList.random() as DonatePosition
                            var counter = 0

                            while (counter < 3 && drop.getRare() == DropRare.LEGENDARY) {
                                drop = dropList.random() as DonatePosition
                                counter++
                            }

                            val moneyDrop = (Math.random() * 50 + 20).toInt() * (drop.getRare().ordinal + 1)

                            ModTransfer()
                                .integer(2)
                                .item(CraftItemStack.asNMSCopy(drop.getIcon()))
                                .string(drop.getTitle())
                                .string(drop.getRare().name)
                                .item(MoneyKit.BIG.getIcon())
                                .string("§d$moneyDrop ${Humanize.plurals("жетон", "жетона", "жетонов", moneyDrop)}")
                                .string(DropRare.EPIC.name)
                                .send("lootbox", player)

                            if (donate.donate.contains(drop)) {
                                val giveBack = (drop.getRare().ordinal + 1) * 48
                                player.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §d$giveBack жетонов§f."))
                                Arcade.deposit(player, giveBack)
                            } else {
                                donate.donate.add(drop)
                            }
                            Arcade.deposit(player, moneyDrop)

                            Bukkit.getOnlinePlayers().forEach {
                                Anime.topMessage(
                                    it,
                                    Formatting.fine(
                                        "§e${player.name} §fполучил §b${
                                            drop.getRare().with(drop.getTitle())
                                        }."
                                    )
                                )
                            }
                        })
                    }
                    contents.add('P', ClickableItem.empty(item {
                        type = Material.CLAY_BALL
                        nbt("other", "anvil")
                        text("§bКак их получить?\n\n§7Побеждайте в игре,\n§7и с шансом §a10%\n§7вы получите §bлутбокс§7.")
                    }.build()))
                    contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
                }
            }
        }).build()

    init {
        var counter = 0

        Bukkit.getScheduler().runTaskTimer(app, {
            counter++

            lootbox.forEach { lootbox ->
                Bukkit.getOnlinePlayers().minByOrNull { it.location.distanceSquared(lootbox.origin) }?.let {
                    val pose = lootbox.stand.headPose

                    pose.x = Math.toRadians(it.location.pitch.toDouble() - 15)
                    pose.y = Math.toRadians(it.location.yaw.toDouble() + 180)

                    lootbox.stand.headPose = pose
                }

                if (counter % 30 == 0) {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        Arcade.get(player)?.let { data ->
                            lootbox.banner.content = "§bЛутбокс\n§fДоступно ${data.crates} ${
                                Humanize.plurals(
                                    "штука",
                                    "штуки",
                                    "штук",
                                    data.crates
                                )
                            }\n"
                            Banners.content(player, lootbox.banner.uuid, lootbox.banner.content)

                            ModTransfer().integer(data.money.toInt()).send("arcade:money", player)
                        }
                    }
                }
            }
        }, 10, 3)
    }

    @EventHandler
    fun TabCompleteEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.handle() {
        if (clickedEntity.type == EntityType.ARMOR_STAND && (clickedEntity as ArmorStand).helmet.getType() == Material.ENDER_CHEST)
            lootboxMenu.open(player)
    }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        Bukkit.getScheduler().runTaskLater(app, {
            Arcade.get(player)?.let { data ->
                lootbox.forEach {
                    it.banner.content = "§bЛутбокс\n§fДоступно ${data.crates} ${
                        Humanize.plurals(
                            "штука",
                            "штуки",
                            "штук",
                            data.crates
                        )
                    }\n"
                    Banners.show(player, it.banner)
                }
            }
        }, 5)
    }
}