package io.ula.drng;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

import java.awt.*;

import static io.ula.drng.Main.LOGGER;
import static io.ula.drng.config.Configs.CHAT_REPLACEMENTS;
import static io.ula.drng.config.Configs.PLAYER_TITLES;

public class PlayerUtils {
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
}
