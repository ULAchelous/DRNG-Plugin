package io.ula.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ControlCmd {
    public static JavaPlugin plugin;
    public static final LiteralArgumentBuilder<CommandSourceStack> cCmd = Commands.literal("control")
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
                        if(player.hasMetadata("controlling_player")){
                            player.sendMessage(Component.text("无法控制(未退出正在进行的控制)").color(TextColor.color(Color.RED.asRGB())));
                            return 0;
                        }
                        Player playerobj = Bukkit.getPlayer(context);
                        if(playerobj != null) {
                            if (playerobj.equals(player)) {
                                player.sendMessage(Component.text("无法控制(对象为自身)").color(TextColor.color(Color.RED.asRGB())));
                                return 0;
                            }
                            if (playerobj.isOp()) {
                                player.sendMessage(Component.text("无法控制(对象为Operator)").color(TextColor.color(Color.RED.asRGB())));
                                return 0;
                            }
                            player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.INVISIBILITY,
                                    Integer.MAX_VALUE,
                                    0,
                                    false,
                                    false
                            ));
                            playerobj.setAllowFlight(true);
                            playerobj.setFlying(true);
                            playerobj.setMetadata("been_controlled", new FixedMetadataValue(plugin, player.getUniqueId()));//使用元数据标记被控制玩家，存储控制者
                            player.setMetadata("controlling_player", new FixedMetadataValue(plugin, playerobj.getUniqueId()));//使用元数据标记控制玩家，存储被控制者
                            player.teleport(playerobj.getLocation());
                        }else {
                            player.sendMessage(Component.text(String.format("未知的玩家\"%s\"", context)).color(TextColor.color(java.awt.Color.RED.getRGB())));
                        }
                        return 0;
                    })
            )
            .then(Commands.literal("stop").executes(commandContext -> {
                Player player = (Player)commandContext.getSource().getSender();
                if(player.hasMetadata("controlling_player")){
                    Player target = Bukkit.getPlayer((java.util.UUID) player.getMetadata("controlling_player").get(0).value());
                    player.removeMetadata("controlling_player",plugin);
                    if(target != null) {
                        target.setAllowFlight(true);
                        target.setFlying(true);
                        target.removeMetadata("been_controlled", plugin);
                    }else{
                        player.sendMessage(Component.text("警告:控制对象离线").color(TextColor.color(Color.YELLOW.asRGB())));
                    }
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                }else{
                    player.sendMessage(Component.text("没有正在进行的控制").color(TextColor.color(Color.RED.asRGB())));
                }
                return 0;
            }))
            .requires(commandSourceStack -> (commandSourceStack.getSender() instanceof Player && commandSourceStack.getSender().isOp()));
    public static final LiteralCommandNode<CommandSourceStack> buildCCmd = cCmd.build();
}
