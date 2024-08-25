package org.mcraster.loader

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Used by Minecraft server, not by this project
class McRasterLoaderPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        val manager: LifecycleEventManager<Plugin> = this.getLifecycleManager()
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()
            commands.register("mc-raster-load", "Load an mc-raster map into the world.", LoadCommand())
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
       event.player.sendMessage(Component.text("Hello, " + event.player.name + "! The mc-raster loader plugin is active."))
    }

}

class LoadCommand : BasicCommand {

    override fun execute(stack: CommandSourceStack, args: Array<out String>?) {
        stack.sender.sendRichMessage("<rainbow>NOT IMPLEMENTED YET!");
    }

}
