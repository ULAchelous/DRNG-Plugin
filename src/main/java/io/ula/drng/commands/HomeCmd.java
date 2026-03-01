package io.ula.drng.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Duration;

import static io.ula.drng.config.Configs.PLAYER_HOMES;

public class HomeCmd {

    private static LiteralArgumentBuilder<CommandSourceStack> homeCmdBuilder = Commands.literal("home")
            .then(Commands.literal("locate")
                    .then(Commands.argument("id", StringArgumentType.string())
                            .executes(context -> {
                                Player sender = (Player)context.getSource().getSender();
                                String id = context.getArgument("id",String.class);
                                int num = 0;
                                if(!PLAYER_HOMES.has(sender.getName()))
                                    PLAYER_HOMES.addKey(sender.getName(), new JsonArray());
                                if(!PLAYER_HOMES.has("maxCount"))
                                    PLAYER_HOMES.addKey("maxCount", 3);
                                for(JsonElement element : PLAYER_HOMES.getKey(sender.getName()).getAsJsonArray())
                                    if(!element.getAsJsonObject().has("removed")) num++;
                                if(num > PLAYER_HOMES.getKey("maxCount").getAsInt()){
                                    sender.sendMessage(Component.text("标记点数量到达上限", TextColor.color(Color.RED.getRGB())));
                                    return 0;
                                }
                                JsonObject marker = new JsonObject();
                                marker.addProperty("id",id);
                                marker.addProperty("location",LocationSerializer(sender.getLocation()));
                                marker.addProperty("world",sender.getWorld().getName());
                                PLAYER_HOMES.getKey(sender.getName()).getAsJsonArray().add(marker);
                                sender.sendMessage(Component.text("添加成功",TextColor.color(Color.YELLOW.getRGB()), TextDecoration.BOLD));
                                PLAYER_HOMES.write();
                                return 0;
                            })
                    )
            )
            .then(Commands.literal("remove")
                    .executes(context -> {
                        Player sender = (Player) context.getSource().getSender();
                        sender.sendMessage(Component.text("点击标记点以删除它！"));
                        sender.sendMessage(getPlayerHomes(sender,true));
                        return 0;
                    })
            )
            .then(Commands.literal("tp")
                    .executes(context -> {
                        Player sender = (Player) context.getSource().getSender();
                        sender.sendMessage(Component.text("点击标记点以传送！"));
                        sender.sendMessage(getPlayerHomes(sender,false));
                        return 0;
                    })
            )
            ;
    public static LiteralCommandNode<CommandSourceStack> homeCmd = homeCmdBuilder.build();

    private static ClickEvent getRemoveClickEvent(JsonElement element,String id){
        return ClickEvent.callback(audience -> {
            audience.sendMessage(Component.text("删除了")
                    .append(Component.space())
                    .append(Component.text(id,TextColor.color(Color.YELLOW.getRGB())))
            );
            element.getAsJsonObject().addProperty("removed",true);
            PLAYER_HOMES.write();
        }, ClickCallback.Options.builder().lifetime(Duration.ofSeconds(15)).build());
    }
    private static ClickEvent getTeleportClickEvent(Player player,JsonElement element){
        return ClickEvent.callback(audience -> {
            World world = Bukkit.getWorld(element.getAsJsonObject().get("world").getAsString());
            audience.sendMessage(Component.text("传送...",TextColor.color(Color.GREEN.getRGB())));
            player.teleport(LocationDeserializer(element.getAsJsonObject().get("location").getAsString(),world));
        },ClickCallback.Options.builder().lifetime(Duration.ofSeconds(15)).build());
    }

    static Component getPlayerHomes(Player player,Boolean flag){
        Component component = Component.empty();
        for(int idx = 0;idx<PLAYER_HOMES.getKey(player.getName()).getAsJsonArray().size();idx++) {
            JsonElement element = PLAYER_HOMES.getKey(player.getName()).getAsJsonArray().get(idx);
            if(element.getAsJsonObject().has("removed")) {
                PLAYER_HOMES.getKey(player.getName()).getAsJsonArray().remove(element);
                PLAYER_HOMES.write();
                continue;
            }
            String id = element.getAsJsonObject().get("id").getAsString();
            if(flag) {
                component = component.append(Component.text('[')
                        .append(Component.text(id,TextColor.color(Color.YELLOW.getRGB()))
                                .clickEvent(getRemoveClickEvent(element,id))
                        )
                        .append(Component.text(']'))
                );
            }else{
                component = component.append(Component.text('[')
                        .append(Component.text(id,TextColor.color(Color.YELLOW.getRGB()))
                                .clickEvent(getTeleportClickEvent(player,element))
                        )
                        .append(Component.text(']'))
                );
            }
            component = component.append(Component.space());
        }
        return component;
    }
    static String LocationSerializer(Location location){
        int x,y,z;
        x=location.getBlockX();y=location.getBlockY();z=location.getBlockZ();
        return String.format("%d %d %d",x,y,z);
    }
    static Location LocationDeserializer(String str,World world){
        String[] location = str.split(" ");
        return new Location(world,Integer.parseInt(location[0]),Integer.parseInt(location[1]),Integer.parseInt(location[2]));
    }
}
