package io.ula.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class controllCmd {
    LiteralArgumentBuilder<CommandSourceStack> cCmd = Commands.literal("controll")
            .then(Commands.argument("player", StringArgumentType.string())
                    .suggests((commandContext, suggestionsBuilder) -> {
                        for(Player player : Bukkit.getOnlinePlayers()){
                            suggestionsBuilder.suggest(player.getName());
                        }
                        return suggestionsBuilder.buildFuture();
                    })
                    .executes(commandContext -> {
                        Player player = (Player)commandContext.getSource().getSender();
                        String context = commandContext.getArgument("player",String.class);

                        return 0;
                    })
            )
            .requires(commandSourceStack -> (commandSourceStack.getSender() instanceof Player && commandSourceStack.getSender().isOp()));
    LiteralCommandNode<CommandSourceStack> buildCCmd = cCmd.build();
}
