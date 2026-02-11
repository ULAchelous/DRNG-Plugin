package io.ula.drng.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.entity.Player;

import java.awt.*;

public class TpaCmd {
    private static LiteralArgumentBuilder<CommandSourceStack> tpaCmdBuilder = Commands.literal("tpa")
            .then(Commands.argument("target",ArgumentTypes.player()).executes(context -> {
                Player sendr = (Player) context.getSource().getSender();
                Player target = context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                sendr.sendMessage(Component.text("请耐心等待对方接受传送哦~"));
                target.sendMessage(Component.text("玩家")
                        .append(Component.space())
                        .append(Component.text(sendr.getName(), TextColor.color(Color.YELLOW.getRGB()), TextDecoration.BOLD))
                        .append(Component.space())
                        .append(Component.text("想要传送到你这里！"))
                );
                target.sendMessage(Component.empty()
                        .append(Component.text("[同意]")
                                .append(Component.object(ObjectContents.sprite(Key.key("gui"),Key.key("pending_invite/accept"))))
                                .hoverEvent(HoverEvent.showText(Component.text("点击确认传送",TextColor.color(Color.green.getRGB()))))
                                .clickEvent(ClickEvent.callback(audience -> sendr.teleport(target))))
                        .append(Component.space())
                        .append(Component.text("[拒绝]")
                                .append(Component.object(ObjectContents.sprite(Key.key("gui"),Key.key("pending_invite/reject"))))
                                .hoverEvent(HoverEvent.showText(Component.text("点击拒绝传送",TextColor.color(Color.RED.getRGB()))))
                                .clickEvent(ClickEvent.callback(audience -> sendr.sendMessage(Component.text("你被拒绝了！" ,TextColor.color(Color.RED.getRGB()))))))
                );
                return 0;
            }))
            .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player);
    public static LiteralCommandNode<CommandSourceStack> tpaCmd = tpaCmdBuilder.build();
}
