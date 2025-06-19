package com.skyxserver.my.id.noVoidX

import com.skyxserver.my.id.noVoidX.Listener.VoidListener
import com.skyxserver.my.id.noVoidX.Commands.CommandHandler
import com.skyxserver.my.id.noVoidX.ReadConfig.ConfigHandler
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NoVoidX : JavaPlugin() {

    lateinit var configHandler: ConfigHandler
    private val modrinthProjectId = "T9JAwLIJ"
    private val currentVersion = "1.0.0"

    override fun onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig()

        // Initialize bStats
        val metrics = Metrics(this, 26209)

        // Initialize config handler
        configHandler = ConfigHandler(this)
        configHandler.loadConfig()

        // Register event listener
        val voidListener = VoidListener(this, configHandler)
        server.pluginManager.registerEvents(voidListener, this)

        // Register commands and set TabCompleter
        val reloadCommandExecutor = CommandHandler(this)
        getCommand("novoidx")?.apply {
            setExecutor(reloadCommandExecutor)
            setTabCompleter(reloadCommandExecutor) // Set the CommandHandler as the TabCompleter
        } ?: run { logger.warning("Command 'novoidx' not found in plugin.yml! Make sure it's defined correctly.") }

        // Check for updates if enabled
        if (configHandler.isCheckUpdateEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                checkForUpdates()
            })
        }

        logger.info("NoVoidX has been enabled!")
    }

    override fun onDisable() {
        logger.info("NoVoidX has been disabled!")
    }

    /**
     * Checks for updates using the Modrinth API
     */
    private fun checkForUpdates() {
        try {
            // Create URL for Modrinth API request
            val url = URL("https://api.modrinth.com/v2/project/$modrinthProjectId/version")

            // Open connection
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            // Check response code
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                logger.warning("Failed to check for updates: HTTP error code $responseCode")
                return
            }

            // Read response
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Parse JSON response
            val parser = JSONParser()
            val versionsArray = parser.parse(response.toString()) as JSONArray

            if (versionsArray.isEmpty()) {
                logger.warning("No versions found on Modrinth for project $modrinthProjectId")
                return
            }

            // Get the latest version
            val latestVersion = (versionsArray[0] as JSONObject).get("version_number") as String

            // Compare versions
            if (latestVersion != currentVersion) {
                logger.info("A new version of NoVoidX is available: $latestVersion (current: $currentVersion)")
                logger.info("Download it from: https://modrinth.com/plugin/$modrinthProjectId/versions")

                // Notify console and ops
                val updateMessage = "[NoVoidX] A new version is available: $latestVersion (current: $currentVersion)"
                Bukkit.getConsoleSender().sendMessage(updateMessage)

                // Schedule a delayed task to notify ops when they join
                Bukkit.getScheduler().runTask(this, Runnable {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        if (player.isOp) {
                            player.sendMessage(updateMessage)
                        }
                    }
                })
            } else {
                logger.info("NoVoidX is up to date (version $currentVersion)")
            }

        } catch (e: Exception) {
            logger.warning("Failed to check for updates: ${e.message}")
        }
    }
}
