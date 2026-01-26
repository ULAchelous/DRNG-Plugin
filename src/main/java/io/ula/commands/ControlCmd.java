package io.ula.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class ControlCmd {
    public static JavaPlugin plugin;
    public static final LiteralArgumentBuilder<CommandSourceStack> cCmd = Commands.literal("control")
            .then(Commands.argument("player", ArgumentTypes.player())
                    .executes(commandContext -> {
                        Player player = (Player)commandContext.getSource().getSender();
                        PlayerSelectorArgumentResolver playerSelector = commandContext.getArgument("player", PlayerSelectorArgumentResolver.class);
                        if(player.hasMetadata("controlling_player")){
                            player.sendMessage(Component.text("无法控制(未退出正在进行的控制)").color(TextColor.color(Color.RED.asRGB())));
                            return 0;
                        }
                        Player playerobj = playerSelector.resolve(commandContext.getSource()).getFirst();
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
                            playerobj.setMetadata("been_controlled", new FixedMetadataValue(plugin, player.getUniqueId()));//使用元数据标记被控制玩家，存储控制者
                            player.setMetadata("controlling_player", new FixedMetadataValue(plugin, playerobj.getUniqueId()));//使用元数据标记控制玩家，存储被控制者
                            player.setMetadata("location_before_control", new FixedMetadataValue(plugin, player.getLocation()));//存储控制者控制前的坐标
                            player.teleport(playerobj.getLocation());
                        }else {
                            player.sendMessage(Component.text("未知的玩家").color(TextColor.color(java.awt.Color.RED.getRGB())));
                        }
                        return 0;
                    })
            )
            .then(Commands.literal("stop").executes(commandContext -> {
                Player player = (Player)commandContext.getSource().getSender();
                if(player.hasMetadata("controlling_player")){
                    Player target = Bukkit.getPlayer((UUID) player.getMetadata("controlling_player").getFirst().value());
                    player.teleport((Location)player.getMetadata("location_before_control").getFirst().value());
                    player.removeMetadata("controlling_player",plugin);
                    player.removeMetadata("location_before_control",plugin);
                    target.removeMetadata("been_controlled", plugin);

                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                }else{
                    player.sendMessage(Component.text("没有正在进行的控制").color(TextColor.color(Color.RED.asRGB())));
                }
                return 0;
            }))
            .requires(commandSourceStack -> (commandSourceStack.getSender() instanceof Player && commandSourceStack.getSender().isOp()));
    public static final LiteralCommandNode<CommandSourceStack> buildCCmd = cCmd.build();
}
