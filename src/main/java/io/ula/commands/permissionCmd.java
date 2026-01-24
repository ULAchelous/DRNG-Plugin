package io.ula.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;
import java.awt.print.Paper;
import java.util.Timer;
import java.util.TimerTask;

import static io.ula.drng.*;

public class permissionCmd {
    static Timer sync = new Timer();
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
                                player.sendMessage(Component.text(String.format("未知的服务器权限\"%s\"",context)).color(TextColor.color(Color.RED.getRGB())));
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
                                                player.addScoreboardTag(element.getAsString());
                                                player.sendMessage(String.format("%s权限成功鉴权",pms));
                                                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                                                if(player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)
                                                    player.setAllowFlight(false);
                                                return 1;
                                            }
                                        }
                                        player.sendMessage(Component.text("授权码错误").color(TextColor.color(Color.RED.getRGB())));
                                        return 0;
                                    }))
                    )
            )
            .then(Commands.literal("remove")//用于移除玩家的服务器权限
                    .then(Commands.argument("player",StringArgumentType.string())
                            .then(Commands.argument("permission",StringArgumentType.string())
                                    .suggests((commandContext, suggestionsBuilder) -> {
                                        for(JsonElement pms : DRNG_PERMISSIONS.getKey("permissions_list").getAsJsonArray())
                                            suggestionsBuilder.suggest(pms.getAsString());//添加json中的权限到建议列表
                                        return suggestionsBuilder.buildFuture();
                                    }).executes(commandContext -> {
                                        Player player = (Player) commandContext.getSource().getSender();
                                        String playerName = commandContext.getArgument("player",String.class);
                                        String context = commandContext.getArgument("permission",String.class);
                                        LOGGER.info(context);
                                        for(JsonElement pms : DRNG_PERMISSIONS.getKey("permissions_list").getAsJsonArray()){
                                            if(context.equals(pms.getAsString())){
                                                if(player.getScoreboardTags().contains(pms.toString())) {
                                                    player.removeScoreboardTag(pms.getAsString());
                                                    player.sendMessage(String.format("已移除玩家%s的%s权限",player.getName(),pms.getAsString()));
                                                    return 0;
                                                }
                                            }
                                        }
                                        player.sendMessage(Component.text(String.format("未知的服务器权限\"%s\"",context)).color(TextColor.color(Color.RED.getRGB())));
                                        return 0;
                                    })

                            )
                            .suggests((commandContext, suggestionsBuilder) -> {
                                for(Player player : Bukkit.getOnlinePlayers())
                                    suggestionsBuilder.suggest(player.getName());
                                return suggestionsBuilder.buildFuture();
                            })
                            .executes(commandContext -> {
                                String content = commandContext.getArgument("player",String.class);
                                CommandSender sender = commandContext.getSource().getSender();
                                for(Player player : Bukkit.getOnlinePlayers()){
                                    if(content.equals(player.getName()))
                                        return 0;
                                }
                                sender.sendMessage(Component.text(String.format("未知的玩家\"%s\"",content)).color(TextColor.color(Color.RED.getRGB())));
                                return 0;
                            })
                            .requires(commandSourceStack -> commandSourceStack.getSender().isOp())
                    )

            )

            .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player);
    public static final LiteralCommandNode<CommandSourceStack> buildpms = permission.build();
}
