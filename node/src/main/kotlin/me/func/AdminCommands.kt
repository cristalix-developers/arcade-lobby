package me.func

import me.func.battlepass.quest.QuestGenerator
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import pw.lach.p13n.network.tower.GiveModelToUserPackage
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import java.util.*

object AdminCommands {

    init {
        fun register(command: String, on: (Player, Array<String>) -> Unit) {
            app.getCommand(command).setExecutor(CommandExecutor { sender, _, _, args ->
                if (sender is Player && sender.isOp) {
                    on(sender, args)
                    sender.sendMessage(Formatting.fine("Что-то произошло (Сообщение по умолчанию)!"))
                }
                return@CommandExecutor true
            })
        }

        register("bpgive") { _, args ->
            Arcade.getArcadeData(Bukkit.getPlayer(args[1])).let {
                if (args[0] == "advanced")
                    it.progress?.advanced = true
                if (args[1] == "exp")
                    it.progress?.exp = args[2].toInt()
            }
        }
        register("reroll") { sender, _ -> Arcade.getArcadeData(sender).data = QuestGenerator.generate() }
        register("lootbox") { sender, _ -> Arcade.giveLootbox(sender.uniqueId) }
        register("exp") { _, args -> Arcade.getArcadeData(Bukkit.getPlayer(args[0])).progress!!.exp += args[1].toInt() }
        register("money") { _, args -> Arcade.deposit(Bukkit.getPlayer(args[0]), args[1].toInt()) }
        register("pers") { _, args ->
            CoreApi.get().socketClient.write(
                GiveModelToUserPackage(
                    UUID.fromString(args[0]),
                    UUID.fromString(args[1])
                )
            )
        }
    }
}