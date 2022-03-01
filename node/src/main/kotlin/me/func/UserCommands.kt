package me.func

import me.func.misc.PersonalizationMenu
import me.func.mod.Anime
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.transfer.ITransferService

object UserCommands {

    init {
        fun register(command: String, on: (Player, Array<String>) -> Unit) {
            app.getCommand(command).setExecutor(CommandExecutor { sender, _, _, args ->
                if (sender is Player) on(sender, args)
                return@CommandExecutor true
            })
        }

        val hub = RealmId.of("HUB")

        register("menu") { sender, _ -> PersonalizationMenu.open(sender) }
        register("leave") { sender, _ -> ITransferService.get().transfer(sender.uniqueId, hub) }
        register("play") { sender, _ -> Anime.sendEmptyBuffer("g5e:open", sender) }
        register("battlepass") { sender, _ -> BattlePassManager.show(sender) }
        register("discord") { sender, _ ->
            sender.spigot().sendMessage(
                *ComponentBuilder("\n§7Бесят баги? Пиши сюда - §bhttps://discord.gg/ra2uwWv9QV§7, помоги нам стать лучше!\n")
                    .event(ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/ra2uwWv9QV§7"))
                    .create()
            )
        }
    }
}