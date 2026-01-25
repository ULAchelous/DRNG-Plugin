package io.ula.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;

public class SayJsonCmd {
    public static final LiteralArgumentBuilder<CommandSourceStack> sayJsonCmdBuilder = Commands.literal("sayjson")
            .then(Commands.argument("json", StringArgumentType.greedyString())
                    .executes(commandContext -> {
                        CommandSender commandSender = commandContext.getSource().getSender();
                        String context = commandContext.getArgument("json",String.class);
                        JsonObject jsonObject = null;
                        try {
                            jsonObject = JsonParser.parseString(context).getAsJsonObject();
                            try {
                                Component component = Component.text("");
                                for (JsonElement text : jsonObject.getAsJsonArray("text")) {
                                    String content = text.getAsJsonObject().get("content").getAsString();
                                    String color = text.getAsJsonObject().get("color").getAsString();
                                    component=component.append(Component.text(content).color(getColor(color)));
                                }
                                commandSender.sendMessage(Component.text(String.format("<%s> ",commandSender.getName())).append(component));
                            } catch (ClassCastException e) {
                                commandSender.sendMessage(Component.text("错误的JSON格式").color(TextColor.color(Color.RED.asRGB())));
                                commandSender.sendMessage(Component.text("格式: {\"text\":[{\"content\":<文本>,\"color\":<颜色>}},...]"));
                                return 0;
                            }
                        }catch (Exception e){
                            commandSender.sendMessage(Component.text("不完整的JSON数据").color(TextColor.color(Color.RED.asRGB())));
                            commandSender.sendMessage(Component.text("格式: {\"text\":[{\"content\":<文本>,\"color\":<颜色>}},...]"));
                            return 0;
                        }
                        return 0;
                    }));
    private static TextColor getColor(String color){
        switch(color){
            case "red":
                return TextColor.color(Color.RED.asRGB());
            case "yellow":
                return TextColor.color(Color.YELLOW.asRGB());
            case "green":
                return TextColor.color(Color.GREEN.asRGB());
            case "blue":
                return TextColor.color(Color.GREEN.asRGB());
            case "aqua":
                return TextColor.color(Color.AQUA.asRGB());
            default:
                return TextColor.fromCSSHexString(color);
        }
    }
    public static final LiteralCommandNode<CommandSourceStack> buildSayJsonCmd = sayJsonCmdBuilder.build();
}
