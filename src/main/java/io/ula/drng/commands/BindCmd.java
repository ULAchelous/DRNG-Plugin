package io.ula.drng.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.ula.drng.config.Configs.CHAT_REPLACEMENTS;

public class BindCmd {
    static LiteralArgumentBuilder<CommandSourceStack> bindCmdBuilder = Commands.literal("bind")
            .then(Commands.argument("targets", ArgumentTypes.players())
                    .then(Commands.literal("add")
                            .then(Commands.argument("key",StringArgumentType.string())
                                .then(Commands.argument("_replace",StringArgumentType.string())
                                    .executes(context -> {
                                        List<Player> targets = context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                        String key = context.getArgument("key",String.class);
                                        String _replace = context.getArgument("_replace",String.class);
                                        for(Player target : targets){
                                            if(target instanceof Player) {
                                                String targetName = target.getName();
                                                JsonObject bind = new JsonObject();
                                                bind.addProperty("key", key);
                                                bind.addProperty("replace", _replace);
                                                if (!CHAT_REPLACEMENTS.has(targetName))
                                                    CHAT_REPLACEMENTS.addKey(targetName, new JsonArray());
                                                CHAT_REPLACEMENTS.getKey(targetName).getAsJsonArray().add(bind);
                                            }
                                        }
                                        CHAT_REPLACEMENTS.write();
                                        context.getSource().getSender().sendMessage(Component.text("添加成功"));
                                        return 0;
                                    }))))
                    .then(Commands.literal("remove")
                            .executes(context -> {
                                List<Player> targets = context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                Player sender = (Player) context.getSource().getSender();
                                for(Player target : targets) {
                                    sender.sendMessage(Component.text("点击")
                                            .append(Component.object(ObjectContents.sprite(Key.key("gui"), Key.key("pending_invite/reject"))))
                                            .append(Component.text("以删除"))
                                            .append(Component.text(String.format("(%s)",target.getName())))
                                    );
                                    sender.sendMessage(getPlayerBinds(target));
                                }
                                return  0;
                            }))
                    .requires(commandSourceStack -> commandSourceStack.getSender().isOp())
            )
            .then(Commands.literal("add")
                    .then(Commands.argument("key",StringArgumentType.string())
                            .then(Commands.argument("_replace",StringArgumentType.string())
                                    .executes(context -> {
                                        Player target = (Player)context.getSource().getSender();
                                        String key = context.getArgument("key",String.class);
                                        String _replace = context.getArgument("_replace",String.class);
                                        String targetName = target.getName();
                                        JsonObject bind = new JsonObject();
                                        bind.addProperty("key",key);
                                        bind.addProperty("replace",_replace);
                                        if(!CHAT_REPLACEMENTS.has(targetName))
                                            CHAT_REPLACEMENTS.addKey(targetName,new JsonArray());
                                        CompletableFuture.runAsync(() -> CHAT_REPLACEMENTS.getKey(targetName).getAsJsonArray().add(bind))
                                                        .thenRun(() -> CHAT_REPLACEMENTS.write());
                                        target.sendMessage(Component.text("添加成功"));
                                        return 0;
                                    })
                            )
                    ).requires(commandSourceStack -> !commandSourceStack.getSender().isOp()))
            .then(Commands.literal("remove")
                    .executes(context -> {
                        Player sender = (Player) context.getSource().getSender();
                        sender.sendMessage(Component.text("点击")
                                .append(Component.object(ObjectContents.sprite(Key.key("gui"), Key.key("pending_invite/reject"))))
                                .append(Component.text("以删除"))
                        );
                        sender.sendMessage(getPlayerBinds(sender));
                        return 0;
                    }).requires(commandSourceStack -> !commandSourceStack.getSender().isOp()))
            .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player);
    public static LiteralCommandNode<CommandSourceStack> bindCmd = bindCmdBuilder.build();
    private static Component getPlayerBinds(Player player){
        CHAT_REPLACEMENTS.reload();
        Component component = Component.empty();
        int idx = 1;
        for(JsonElement element : CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray()){
            if(element.getAsJsonObject().has("removed")) continue;
            String key = element.getAsJsonObject().get("key").getAsString();
            String replace = element.getAsJsonObject().get("replace").getAsString();
            final int ii = idx;
            component = component
                    .append(Component.text(idx,TextColor.color(Color.RED.getRGB())).append(Component.space()))
                    .append(Component.text(String.format("\"%s\"",key), TextColor.color(Color.cyan.getRGB())).append(Component.space()))
                    .append(Component.text("->",TextColor.color(Color.YELLOW.getRGB())).append(Component.space()))
                    .append(Component.text(String.format("\"%s\"",replace), TextColor.color(Color.cyan.getRGB())).append(Component.space()))
                    .append(Component.object(ObjectContents.sprite(Key.key("gui"),Key.key("pending_invite/reject")))
                            .clickEvent(ClickEvent.callback(audience -> {
                                element.getAsJsonObject().addProperty("removed",true);
                                audience.sendMessage(Component.text("删除了")
                                        .append(Component.space())
                                        .append(Component.text(ii,TextColor.color(Color.RED.getRGB())))
                                        .append(Component.space())
                                        .append(Component.text("号")));
                                CHAT_REPLACEMENTS.write();
                            }, ClickCallback.Options.builder().lifetime(Duration.ofSeconds(30)).build())))
                    .append(Component.newline());
            idx++;
        }
        return component;
    }
}
