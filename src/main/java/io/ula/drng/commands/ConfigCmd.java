package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.ula.drng.Main;
import io.ula.drng.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigCmd {
    public static Main ownerPlugin;
    private static LiteralArgumentBuilder<CommandSourceStack> configCmdBuilder = Commands.literal("configfile")
            .then(Commands.literal("save").executes(
               commandContext -> {
                   ConfigManager configManager = ownerPlugin.getConfigManager();
                   new Thread(configManager::saveAll).start();
                   return 0;
               }
            ))
            .then(Commands.literal("reload").executes(
                    commandContext -> {
                        ConfigManager configManager = ownerPlugin.getConfigManager();
                        new Thread(configManager::reloadAll).start();
                        return 0;
                    }
            ));
}
