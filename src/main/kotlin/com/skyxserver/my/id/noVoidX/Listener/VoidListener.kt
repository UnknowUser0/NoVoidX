package com.skyxserver.my.id.noVoidX.Listener

import com.skyxserver.my.id.noVoidX.NoVoidX
import com.skyxserver.my.id.noVoidX.ReadConfig.ConfigHandler
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Listener for void fall detection and teleportation
 */
class VoidListener(private val plugin: NoVoidX, private val configHandler: ConfigHandler) : Listener {

    // Map to track player teleport cooldowns (player UUID -> last teleport time)
    private val teleportCooldowns = ConcurrentHashMap<UUID, Long>()

    /**
     * Handles player movement to detect void falls
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val world = player.world
        val worldName = world.name

        // Check if void teleportation is enabled for this world
        if (!configHandler.isWorldEnabled(worldName)) {
            return
        }

        // Check if player is below void threshold for this world
        if (player.location.y > configHandler.getVoidThreshold(worldName)) {
            return
        }

        // Get teleport target for this world
        val target = configHandler.getTeleportTarget(worldName) ?: return

        // Check if player is on cooldown
        if (isOnCooldown(player)) {
            return
        }

        // Teleport player to target world
        teleportToTarget(player, target)
    }

    /**
     * Teleports a player to the target world and applies effects
     */
    private fun teleportToTarget(player: Player, target: ConfigHandler.TeleportTarget) {
        // Get target world
        val targetWorld = Bukkit.getWorld(target.targetWorld) ?: run {
            if (configHandler.isLoggingEnabled()) {
                logger.warn { "Target world '${target.targetWorld}' not found for teleportation" }
            }
            return
        }

        // Calculate coordinates based on ratio
        val playerLoc = player.location
        val initialTargetX = playerLoc.x / target.ratio.toDouble()
        val initialTargetZ = playerLoc.z / target.ratio.toDouble()
        val initialTargetY = target.targetY.toDouble()

        val initialLocation = Location(targetWorld, initialTargetX, initialTargetY, initialTargetZ)

        // Adjust findSafeLocation logic
        val safeLocation = findSafeLocation(initialLocation)
        if (safeLocation == null) {
            if (configHandler.isLoggingEnabled()) {
                logger.warn { "Could not find a safe location around X=$initialTargetX, Z=$initialTargetZ in ${targetWorld.name} at Y=${target.targetY}. Teleporting to initial location (potentially unsafe)." }
            }
            player.teleport(initialLocation)
        } else {
            // Set cooldown
            setTeleportCooldown(player)

            // Teleport player to safe location
            player.teleport(safeLocation)
        }

        // Apply blindness effect
        player.addPotionEffect(PotionEffect(
            PotionEffectType.BLINDNESS,
            configHandler.getBlindnessDuration(),
            0,
            false,
            false,
            true
        ))
    }

    /**
     * Finds a safe location to teleport to in the target world.
     * Starts from the given location and searches a small radius.
     */
    private fun findSafeLocation(startLocation: Location): Location? {
        val world = startLocation.world ?: return null

        // Check if the starting location is safe
        if (isSafeLocation(startLocation)) {
            return startLocation
        }

        // Search within a small radius around the calculated point.
        val searchRadius = 3
        for (yOffset in -1..1) {
            for (xOffset in -searchRadius..searchRadius) {
                for (zOffset in -searchRadius..searchRadius) {
                    val checkLocation = startLocation.clone().add(xOffset.toDouble(), yOffset.toDouble(), zOffset.toDouble())
                    if (isSafeLocation(checkLocation)) {
                        return checkLocation
                    }
                }
            }
        }

        // Return null if no safe location is found
        return null
    }

    /**
     * Checks if a location is safe for teleportation
     */
    private fun isSafeLocation(location: Location): Boolean {
        val world = location.world

        // Check if the blocks at and above the location are safe (air)
        val blockAt = world.getBlockAt(location)
        val blockAbove = world.getBlockAt(location.clone().add(0.0, 1.0, 0.0))

        // Check if the block below is solid
        val blockBelow = world.getBlockAt(location.clone().add(0.0, -1.0, 0.0))

        return blockAt.type == Material.AIR &&
                blockAbove.type == Material.AIR &&
                blockBelow.type.isSolid
    }

    /**
     * Sets the teleport cooldown for a player
     */
    private fun setTeleportCooldown(player: Player) {
        teleportCooldowns[player.uniqueId] = System.currentTimeMillis()
    }

    /**
     * Checks if a player is on teleport cooldown
     */
    private fun isOnCooldown(player: Player): Boolean {
        val lastTeleport = teleportCooldowns[player.uniqueId] ?: return false
        val cooldownTime = configHandler.getTeleportCooldownMs()
        val currentTime = System.currentTimeMillis()

        return currentTime - lastTeleport < cooldownTime
    }
}