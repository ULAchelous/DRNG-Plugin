package io.ula.drng.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.List;

import static io.ula.drng.Main.*;

public class PermissionCmd {
    public static final LiteralArgumentBuilder<CommandSourceStack> permission = Commands.literal("permission")

            .then(Commands.literal("request")
                    .then(Commands.argument("permissions",StringArgumentType.string())
                            .suggests((commandContext, suggestionsBuilder) -> {
                                for(JsonElement pms : DRNG_PERMISSIONS.getKey("permissions_list").getAsJsonArray())
                                    suggestionsBuilder.suggest(pms.getAsString());
                                return suggestionsBuilder.buildFuture();
                            })
                            .executes(commandContext -> {
                                Player player = (Player)commandContext.getSource().getSender();
                                String context = commandContext.getArgument("permissions",String.class);
                                LOGGER.info(context);
                                for(JsonElement pms : DRNG_PERMISSIONS.getKey("permissions_list").getAsJsonArray()){
                                    if(context.equals(pms.getAsString())){
                                        if(player.getScoreboardTags().contains(pms.getAsString())) {
                                            player.sendMessage(String.format("拥有%s权限: true", pms.getAsString()));
                                            return 0;
                                        }
                                        player.sendMessage(String.format("拥有%s权限: false", pms.getAsString()));
                                        return 0;
                                    }
                                }
                                player.sendMessage(Component.text(String.format("未知的服务器权限\"%s\"",context)).color(TextColor.color(Color.RED.asRGB())));
                                return 0;
                            })
                            .then(Commands.argument("pms_code",StringArgumentType.string())
                                    .executes(commandContext -> {
                                        Player player = (Player) commandContext.getSource().getSender();
                                        String pms = commandContext.getArgument("permissions",String.class);
                                        String context = commandContext.getArgument("pms_code",String.class);
                                        for(JsonElement element : PMS_CODES.getKey(pms).getAsJsonArray()){
                                            if(context.equals(element.getAsString())){
                                                if(player.getScoreboardTags().contains(pms)){
                                                    player.sendMessage("已经拥有了该权限");
                                                    return 1;
                                                }
                                                player.addScoreboardTag(pms);
                                                player.sendMessage(String.format("%s权限成功鉴权",pms));
                                                return 1;
                                            }
                                        }
                                        player.sendMessage(Component.text("授权码错误").color(TextColor.color(Color.RED.asRGB())));
                                        return 0;
                                    }))
                    )
            )
            .then(Commands.literal("remove")//用于移除玩家的服务器权限
                    .then(Commands.argument("player", ArgumentTypes.players())
                            .then(Commands.argument("permission",StringArgumentType.string())
                                    .suggests((commandContext, suggestionsBuilder) -> {
                                        for(JsonElement pms : DRNG_PERMISSIONS.getKey("permissions_list").getAsJsonArray())
                                            suggestionsBuilder.suggest(pms.getAsString());//添加json中的权限到建议列表
                                        return suggestionsBuilder.buildFuture();
                                    }).executes(commandContext -> {
                                        Player player = (Player) commandContext.getSource().getSender();
                                        String pmsName = commandContext.getArgument("permission",String.class);
                                        List<Player> targets = commandContext.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(commandContext.getSource());
                                        for(JsonElement pms : DRNG_PERMISSIONS.getKey("permissions_list").getAsJsonArray()){
                                            if(pmsName.equals(pms.getAsString())){
                                                for(Player target : targets) {
                                                    if (target.getScoreboardTags().contains(pmsName)) {
                                                        target.removeScoreboardTag(pms.getAsString());
                                                        player.sendMessage(String.format("已移除玩家%s的%s权限", target.getName(), pms.getAsString()));
                                                    }
                                                }
                                                return 0;
                                            }
                                        }
                                        player.sendMessage(Component.text(String.format("未知的服务器权限\"%s\"",pmsName)).color(TextColor.color(Color.RED.asRGB())));
                                        return 0;
                                    })

                            )
                    )
                    .requires(commandSourceStack -> commandSourceStack.getSender().isOp())
            )

            .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player);
    public static final LiteralCommandNode<CommandSourceStack> buildpms = permission.build();
}
