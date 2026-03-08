package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.ula.drng.Main;
import io.ula.drng.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigCmd {
    public static Main ownerPlugin;
    private static LiteralArgumentBuilder<CommandSourceStack> configCmdBuilder = Commands.literal("configfile")
            .then(Commands.literal("save").executes(
               commandContext -> {
                   ConfigManager configManager = ownerPlugin.getConfigManager();
                   commandContext.getSource().getSender().sendMessage(Component.text("已保存配置文件中的更改！"));
                   new Thread(configManager::saveAll).start();
                   return 0;
               }
            ))
            .then(Commands.literal("reload").executes(
                    commandContext -> {
                        ConfigManager configManager = ownerPlugin.getConfigManager();
                        commandContext.getSource().getSender().sendMessage(Component.text("已重新加载配置文件！"));
                        new Thread(configManager::reloadAll).start();
                        return 0;
                    }
            ))
            .requires(commandSourceStack -> commandSourceStack.getSender().isOp());
    public static LiteralCommandNode<CommandSourceStack> configCmd = configCmdBuilder.build();
}
