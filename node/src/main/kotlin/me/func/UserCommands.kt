package me.func

import me.func.battlepass.quest.ArcadeType
import me.func.misc.PersonalizationMenu
import me.func.mod.Anime
import me.func.mod.selection.Confirmation
import me.func.mod.selection.MenuManager
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.transfer.ITransferService
import java.util.UUID
import java.util.function.Consumer
import kotlin.math.abs

object UserCommands {

    init {
        fun register(command: String, on: (Player, Array<String>) -> Unit) {
            app.getCommand(command).setExecutor(CommandExecutor { sender, _, _, args ->
                if (sender is Player) on(sender, args)
                return@CommandExecutor true
            })
        }

        val hub = RealmId.of("HUB")

        register("rp") { sender, _ ->
            val stat = app.userManager.getUser(sender.uniqueId).stat
            Confirmation("Рекомендуем установить", "аркадный ресурс-пак") {
                it.setResourcePack("https://storage.c7x.dev/func/arcade-latest.zip", "5")
                stat.enabledResourcePack = Tristate.TRUE
            }.run {
                onDeny = Consumer {
                    it.setResourcePack("", "")
                    stat.enabledResourcePack = Tristate.FALSE
                }

                open(sender)
            }
        }
        register("menu") { sender, _ -> PersonalizationMenu.open(sender) }
        register("leave") { sender, _ -> ITransferService.get().transfer(sender.uniqueId, hub) }
        register("play") { sender, _ -> Anime.sendEmptyBuffer("g5e:open", sender) }
        register("battlepass") { sender, _ -> BattlePassManager.show(sender) }
        register("bot") { sender, _ ->
            sender.spigot().sendMessage(
                *ComponentBuilder("\n§7Бесят баги? Пиши сюда - §bhttps://t.me/deadles_bot§7, помоги нам стать лучше!\n")
                    .event(ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/deadles_bot§7"))
                    .create()
            )
        }
        // Найти очередь, в которой разница СЛОТОВ и ЛЮДЕЙ В ОЧЕРЕДИ минимальная и не меньше единицы
        register("random") { sender, _ ->
            sender.performCommand(
                "queue ${
                    Games5e.client.queueOnline.minByOrNull { queue ->
                        abs(
                            (ArcadeType.values()
                                .firstOrNull { queue.key.toString() == it.queue }?.slots ?: 0) - queue.value + 1
                        )
                    }?.key ?: UUID.fromString(
                        ArcadeType.values().random().queue
                    )
                }"
            )
        }
    }
}
