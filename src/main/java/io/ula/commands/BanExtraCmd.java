package io.ula.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import static io.ula.drng.BANNED_PLAYERS;

public class BanExtraCmd {
    public static JavaPlugin plugin;
    public static LiteralArgumentBuilder<CommandSourceStack> banextraBuilder  = Commands.literal("banextra")
            .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("time",ArgumentTypes.time())
                            .executes(commandContext -> {
                                Player player = (Player) commandContext.getSource().getSender();
                                Player target = commandContext.getArgument("player", PlayerSelectorArgumentResolver.class)
                                        .resolve(commandContext.getSource()).getFirst();//获取封禁对象
                                int time = commandContext.getArgument("time",Integer.class)/20;//获取封禁时间
                                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");//时间格式化格式
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.SECOND,time);
                                JsonElement banReason = GsonComponentSerializer.gson().serializeToTree(Component.text("你已被此服务器封禁")
                                        .append(Component.newline())
                                        .append(Component.text("解封时间: ")
                                                .append(Component.text(formatter.format(calendar.getTime())).color(TextColor.color(Color.AQUA.asRGB()))))
                                );
                                BANNED_PLAYERS.addKey(target.getName(),banReason);

                                BanList banList = plugin.getServer().getBanList(BanListType.PROFILE);
                                banList.addBan(target.getPlayerProfile(),"",calendar.getTime(),player.getName());
                                target.kick(GsonComponentSerializer.gson().deserializeFromTree(banReason));
                                return 0;
                            })
                    )
            )
            .requires(commandSourceStack -> (commandSourceStack.getSender() instanceof Player && commandSourceStack.getSender().isOp()));
    public static LiteralCommandNode<CommandSourceStack> buildBanExtraCmd = banextraBuilder.build();
}
