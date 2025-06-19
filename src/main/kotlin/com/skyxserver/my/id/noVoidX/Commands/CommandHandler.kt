package com.skyxserver.my.id.noVoidX.Commands

import com.skyxserver.my.id.noVoidX.NoVoidX
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter // Import TabCompleter
import org.bukkit.util.StringUtil
import java.util.ArrayList

class CommandHandler(private val plugin: NoVoidX) : CommandExecutor, TabCompleter { // Implement TabCompleter

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("novoidx", ignoreCase = true) || command.aliases.any { it.equals(label, ignoreCase = true) }) {
            if (args.isNotEmpty()) {
                if (args[0].equals("reload", ignoreCase = true)) {
                    if (!sender.hasPermission("novoidx.reload") && !sender.hasPermission("novoidx.admin")) {
                        sender.sendMessage("${ChatColor.RED}You do not have permission to reload NoVoidX.")
                        return true
                    }
                    plugin.configHandler.loadConfig()
                    sender.sendMessage("${ChatColor.GREEN}NoVoidX configuration successfully reloaded.")
                    plugin.logger.info("NoVoidX configuration successfully reloaded by ${sender.name}.")
                    return true
                } else {
                    displayPluginInfo(sender)
                    return true
                }
            } else {
                displayPluginInfo(sender)
                return true
            }
        }
        return false
    }

    /**
     * Helper function to display plugin information and Modrinth link.
     */
    private fun displayPluginInfo(sender: CommandSender) {
        val pluginName = plugin.description.name
        val pluginVersion = plugin.description.version
        val pluginAuthors = plugin.description.authors.joinToString()

        sender.sendMessage("${ChatColor.GOLD}--- $pluginName ---")
        sender.sendMessage("${ChatColor.YELLOW}Authors: ${pluginAuthors}")
        sender.sendMessage("${ChatColor.YELLOW}Versi: ${pluginVersion}")
        sender.sendMessage("${ChatColor.YELLOW}Modrinth: ${ChatColor.AQUA}https://modrinth.com/plugin/novoidx")
        sender.sendMessage("${ChatColor.GOLD}----------------------")
    }

    // Implement onTabComplete method
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (command.name.equals("novoidx", ignoreCase = true) || command.aliases.any { it.equals(alias, ignoreCase = true) }) {
            val completions = ArrayList<String>()
            val commands = ArrayList<String>()

            if (args.size == 1) { // When the user types the first argument
                if (sender.hasPermission("novoidx.reload") || sender.hasPermission("novoidx.admin")) {
                    commands.add("reload")
                }
                StringUtil.copyPartialMatches(args[0], commands, completions)
            }
            return completions.sorted() // Sort the suggestions alphabetically
        }
        return null
    }
}