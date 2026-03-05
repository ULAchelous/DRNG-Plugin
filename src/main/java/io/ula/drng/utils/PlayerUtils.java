package io.ula.drng.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.ula.drng.ScoreBoardHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;

import static io.ula.drng.config.Configs.*;
import static org.bukkit.Bukkit.getServer;

public class PlayerUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    public static Component getPlayerTitles(Player player) {
        PLAYER_TITLES.reload();
        Component component = Component.empty();
        if(PLAYER_TITLES.has(player.getName())) {
            try {
                for (JsonElement title : PLAYER_TITLES.getKey(player.getName()).getAsJsonArray()) {
                    try {
                        component = component
                                .append(Component.text("["))
                                .append(GsonComponentSerializer.gson().deserialize(title.toString()))//title component
                                .append(Component.text("]"))
                                .append(Component.space())//spacer
                        ;
                    } catch (Exception e) {
                        player.sendMessage(Component.text("错误: 玩家头衔加载中出现问题").color(TextColor.color(Color.RED.getRGB())));
                        LOGGER.error("Error in ./config/player_titles.json");
                        LOGGER.error("Not a valid Component object!");
                        return Component.text("");
                    }
                }
            } catch (ClassCastException e) {
                player.sendMessage(Component.text("错误: 玩家头衔加载中出现问题").color(TextColor.color(Color.RED.getRGB())));
                LOGGER.error("Error in ./config/player_titles.json");
                LOGGER.error(String.format("\"%s\" : ...<(HERE)", player.getName()));
                LOGGER.error("Wrong JsonElement,need JsonArray!");
                return Component.text("");
            }
        }
        return component;
    }

    public static String getPlayerChatMsg(String message,Player player){
        CHAT_REPLACEMENTS.reload();
        if(!CHAT_REPLACEMENTS.has(player.getName()))
            CHAT_REPLACEMENTS.addKey(player.getName(),new JsonArray());
        JsonArray array = CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray();
        for (int i=0;i<array.size();i++){
            JsonElement element = array.get(i);
            if (element.getAsJsonObject().has("removed")){
                CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray().remove(element);
                continue;
            }
            message+="☐";
            String key = element.getAsJsonObject().get("key").getAsString();
            String replace = element.getAsJsonObject().get("replace").getAsString();
            String[] temp = message.split(key);
            message="";
            int len = temp.length;
            if(temp[len -1].equals("☐")){
                for(int idx = 0; idx < len-1; idx++) message += temp[idx] + replace;
            }else {
                for (int idx = 0; idx < len; idx++) {
                    if(idx == len-1)
                        message += temp[idx].substring(0,temp[idx].length()-1);
                    else
                        message += temp[idx]+replace;
                }
            }
        }
        CHAT_REPLACEMENTS.write();
        return  message;
    }

    public static Component getPlayerLoginMsg(Player player){
        if(player.isOp())
            return  Component.empty();
        Component loginMsg = Component.text("");
        if(PLAYER_TITLES.has(player.getName()))
            loginMsg = loginMsg.append(PlayerUtils.getPlayerTitles(player));
        loginMsg = loginMsg.append(Component.text(player.getName()))
                .append(Component.text("，欢迎回来～").decorate(TextDecoration.BOLD));
        return loginMsg;
    }

    public static void playerUpdateOnlineTime(JavaPlugin plugin){
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.hasMetadata("onlineTime")) {
                int value = player.getMetadata("onlineTime").getFirst().asInt();
                player.setMetadata("onlineTime", new FixedMetadataValue(plugin, value + 1));
                ScoreBoardHelper.updateScores(player, 3);
            }
        }
    }

    public static void balanceFunction(){
        for(Player player : getServer().getOnlinePlayers()){
            if(player.isOp() && CONFIG.getKey("balancedOp").getAsBoolean())
                player.setGameMode(GameMode.SPECTATOR);
            if(player.getGameMode().equals(GameMode.CREATIVE) && !CONFIG.getKey("allowCreativeMode").getAsBoolean())
                player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public static void initPlayerStatus(Player player,JavaPlugin plugin){
        player.setMetadata("onlineTime",new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("deathCount"))
            player.setMetadata("deathCount", new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("digCount"))
            player.setMetadata("digCount", new FixedMetadataValue(plugin,0));
        //init Metadata

        if(player.isOp()&&CONFIG.getKey("balancedOp").getAsBoolean()) player.setGameMode(GameMode.SPECTATOR);
        if (player.getGameMode().equals(GameMode.SPECTATOR) && !player.isOp())
            player.setGameMode(GameMode.SURVIVAL);
    }
}
