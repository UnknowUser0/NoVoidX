package com.skyxserver.my.id.noVoidX.ReadConfig

import com.skyxserver.my.id.noVoidX.NoVoidX
import org.bukkit.configuration.file.FileConfiguration

/**
 * Handles the configuration for the NoVoidX plugin
 */
class ConfigHandler(private val plugin: NoVoidX) {

    private lateinit var config: FileConfiguration

    private var pluginEnabled: Boolean = true
    private val worldConfigs = mutableMapOf<String, WorldConfig>()

    private var blindnessDuration: Int = 5
    private var teleportCooldown: Int = 3
    private var checkUpdate: Boolean = true
    private var loggingEnabled: Boolean = false // Tambahkan variabel untuk pengaturan logging

    /**
     * Loads the configuration from config.yml
     */
    fun loadConfig() {
        plugin.saveDefaultConfig()
        config = plugin.config

        worldConfigs.clear()

        // Load global settings
        pluginEnabled = config.getBoolean("enabled", true)
        checkUpdate = config.getBoolean("check-update", true)
        loggingEnabled = config.getBoolean("log", false)

        // Load world-specific settings
        val worldsSection = config.getConfigurationSection("worlds")
        worldsSection?.getKeys(false)?.forEach { worldName ->
            val enabled = worldsSection.getBoolean("$worldName.enabled", false)
            val voidThreshold = worldsSection.getInt("$worldName.void-threshold", -64)
            val targetWorld = worldsSection.getString("$worldName.target-world") ?: "world"
            val targetY = worldsSection.getInt("$worldName.target-y", 100)
            val ratio = worldsSection.getInt("$worldName.ration", 1)

            worldConfigs[worldName] = WorldConfig(enabled, voidThreshold, targetWorld, targetY, ratio)
        }

        // Load other settings
        blindnessDuration = config.getInt("blindness-duration", 5)
        teleportCooldown = config.getInt("teleport-cooldown", 3)

        // Log loaded configuration
        fun logConfiguration() {
            plugin.logger.info("NoVoidX configuration loaded:")
            plugin.logger.info("- Plugin enabled: $pluginEnabled")
            plugin.logger.info("- Check for updates: $checkUpdate")
            plugin.logger.info("- World configurations:")
            worldConfigs.entries.joinToString { "${it.key}: Enabled=${it.value.enabled}, VoidThreshold=${it.value.voidThreshold}, TargetWorld=${it.value.targetWorld}, TargetY=${it.value.targetY}, Ratio=${it.value.ratio}" }
                .also { plugin.logger.info(it) }
            plugin.logger.info("- Blindness duration: $blindnessDuration seconds")
            plugin.logger.info("- Teleport cooldown: $teleportCooldown seconds")
        }
        if (loggingEnabled) {
            logConfiguration()
        } else {
            plugin.logger.info("NoVoidX configuration loaded without logging.")
        }
    }

    /**
     * Checks if the plugin is globally enabled
     */
    fun isPluginEnabled(): Boolean {
        return pluginEnabled
    }

    /**
     * Checks if a world has void teleportation enabled
     */
    fun isWorldEnabled(worldName: String): Boolean {
        return worldConfigs[worldName]?.enabled ?: false
    }

    /**
     * Gets the teleport target for a world
     * @return The teleport target or null if not configured
     */
    fun getTeleportTarget(worldName: String): TeleportTarget? {
        val worldConfig = worldConfigs[worldName]
        return if (worldConfig != null) {
            TeleportTarget(worldConfig.targetWorld, worldConfig.targetY, worldConfig.ratio)
        } else {
            null
        }
    }

    /**
     * Gets the blindness effect duration in ticks
     */
    fun getBlindnessDuration(): Int {
        return blindnessDuration * 20 // Convert seconds to ticks
    }

    /**
     * Gets the Y-coordinate threshold for void detection for a specific world
     * @param worldName The name of the world
     * @return The void Y-coordinate threshold for the world, or -64 as a default
     */
    fun getVoidThreshold(worldName: String): Int {
        return worldConfigs[worldName]?.voidThreshold ?: -64
    }

    /**
     * Checks if update checking is enabled
     */
    fun isCheckUpdateEnabled(): Boolean {
        return checkUpdate
    }

    /**
     * Gets the teleport cooldown in milliseconds
     */
    fun getTeleportCooldownMs(): Long {
        return teleportCooldown * 1000L // Convert seconds to milliseconds
    }

    fun isLoggingEnabled(): Boolean {
        return loggingEnabled
    }

    /**
     * Represents a teleport target with world name, Y-coordinate, and ratio
     */
    // UBAH: Tambahkan 'ratio'
    data class TeleportTarget(val targetWorld: String, val targetY: Int, val ratio: Int)

    /**
     * Represents the full configuration for a specific world
     */
    data class WorldConfig(
        val enabled: Boolean,
        val voidThreshold: Int,
        val targetWorld: String,
        val targetY: Int,
        val ratio: Int
    )
}