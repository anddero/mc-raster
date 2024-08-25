package org.mcraster.loader

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Used by Minecraft server, not by this project
class McRasterLoaderPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
       event.player.sendMessage(Component.text("Hello, " + event.player.name + "! The mc-raster loader plugin is active."))
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        sender.sendRichMessage("<rainbow>COMMAND NOT IMPLEMENTED YET! Called '${command.name}' with label '${label}' and args '${args.joinToString(" ")}'.")
        return true
    }

}
