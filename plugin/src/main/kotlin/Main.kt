package org.mcraster.loader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.mcraster.model.Block
import org.mcraster.model.DiskBoundModel
import java.io.File
import org.mcraster.model.BlockType
import org.mcraster.util.OptionalUtils.orThrow
import java.util.*
import java.util.concurrent.CancellationException

@Suppress("unused") // Used by Minecraft server, not by this project
class McRasterLoaderPlugin : JavaPlugin(), Listener {

    private val asyncScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    override fun onDisable() {
        asyncScope.cancel("Plugin disabled")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage(Component.text("Hello, " + event.player.name + "! The mc-raster loader plugin is active."))
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name != "mc-raster-load") {
            sender.sendRichMessage("<red>Unrecognized command: ${command.name}")
            return false
        }
        if (args.size != 1) {
            sender.sendRichMessage("<red>Expected 1 argument, given ${args.size}")
            return false
        }
        try {
            loadWorldAsync(args[0])
        } catch (t: Throwable) {
            sender.sendRichMessage("<red>Generation failed due to error: ${t.message}")
            t.printStackTrace()
            return false
        }
        sender.sendRichMessage("<rainbow>Generation has been started async, check console for progress!")
        return true
    }

    private fun loadWorldAsync(directory: String) {
        println("Starting to load the mc-raster world...")
        val world = Bukkit.getWorlds().firstOrNull().orThrow("Couldn't get world to modify!")
        // Make sure the processing under asyncScope strictly doesn't interact with Bukkit world! It's not thread-safe!
        asyncScope.launch {
            // TODO Will open any directory given, creating it if it doesn't exist, and report that loading succeeded. This is not desired, "DiskBoundModel" should have a read-only mode which should fail if the world doesn't exist.
            val model = DiskBoundModel(File(directory), false)
            var running = true
            // TODO Extract the magic number 1024*1024, also make it slightly smaller like 1024*256 so the server wouldn't jump back so much while processing
            for ((index, blocks) in model.asSequence().chunked(1024 * 1024).withIndex()) {
                if (!running) break
                println("Schedule processing of chunk $index...")
                val id = Bukkit.getScheduler().scheduleSyncDelayedTask(this@McRasterLoaderPlugin, blocksSetterJob(blocks, world, index))
                while (Bukkit.getScheduler().isQueued(id)) {
                    if (!isActive) {
                        println("Job externally terminated, gracefully skipping unprocessed blocks")
                        running = false
                        break
                    }
                    try {
                        delay(1L)
                    } catch (e: CancellationException) {
                        println("Job cancelled while sleeping (${e.message}), gracefully skipping unprocessed blocks")
                        running = false
                        break
                    }
                }
            }
            println("Done loading the mc-raster world.")
            // TODO Print xyz bounds and stats like how many blocks/chunks were written
        }
    }

    companion object {

        private fun blocksSetterJob(
            blocks: List<Block>,
            world: World,
            index: Int
        ) = Runnable {
            println("Start processing chunk $index.")
            blocks.forEach { block ->
                // TODO Ignores "NONE" blocks, meaning those would remain as are in the world. Probably not desired behavior, should clear the entire stack of blocks first.
                block.type.toMaterial().ifPresent { material ->
                    world.getBlockAt(block.pos.x, block.pos.y, block.pos.z).type = material
                }
            }
            println("Done processing chunk $index.")
        }

        private fun BlockType.toMaterial(): Optional<Material> {
            return when (this) {
                BlockType.NONE -> Optional.empty()
                BlockType.STONE -> Optional.of(Material.STONE)
                BlockType.SOIL -> Optional.of(Material.DIRT)
                BlockType.WATER -> Optional.of(Material.WATER)
                BlockType.SOIL_WITH_GRASS -> Optional.of(Material.GRASS_BLOCK)
                BlockType.SAND -> Optional.of(Material.SAND)
                BlockType.GRAVEL -> Optional.of(Material.GRAVEL)
                BlockType.GLASS -> Optional.of(Material.GLASS)
                BlockType.AIR -> Optional.of(Material.AIR)
                BlockType.UNBREAKABLE_STONE -> Optional.of(Material.BEDROCK)
                else -> Optional.empty()
            }
        }

    }

}
