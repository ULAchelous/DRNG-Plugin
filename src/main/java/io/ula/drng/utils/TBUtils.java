package io.ula.drng.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ula.drng.Main;
import io.ula.drng.commands.NoticeCmd;
import io.ula.drng.config.ConfigFile;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class TBUtils {
    public  static Main ownerPlugin;
    public static void aliceBehaviour(){
        int owCnt = 0, netherCnt = 0, teCnt = 0;
        World overworld = Bukkit.getWorld(Key.key("overworld"));
        World nether = Bukkit.getWorld(Key.key("the_nether"));
        World the_end = Bukkit.getWorld(Key.key("the_end"));
        if(overworld.getEntities().size() >= 2700) {
            for (Entity entity : overworld.getEntities()) {
                if (entity.getType() == EntityType.ITEM) {
                    entity.remove();
                    owCnt++;
                }
            }
        }
        if(nether.getEntities().size() >= 2700){
            for (Entity entity : nether.getEntities()) {
                if (entity.getType() == EntityType.ITEM) {
                    entity.remove();
                    netherCnt++;
                }
            }
        }
        if(the_end.getEntities().size() >= 2700) {
            for (Entity entity : the_end.getEntities()) {
                if (entity.getType() == EntityType.ITEM) {
                    entity.remove();
                    teCnt++;
                }
            }
        }
        if((owCnt | netherCnt | teCnt) != 0) {
            getServer().sendMessage(Component.empty()
                    .append(Component.object(ObjectContents.playerHead("AZ9C")))
                    .append(Component.text("["))
                    .append(Component.text("AL-1S").color(TextColor.fromCSSHexString("#76d7ea")).decorate(TextDecoration.BOLD))
                    .append(Component.text("]"))
                    .append(Component.space())
                    .append(Component.text("吃掉了").append(Component.space()).append(Component.text(owCnt + netherCnt + teCnt, TextColor.color(Color.YELLOW.getRGB()))).append(Component.space()).append(Component.text("个掉落物！")))
            );
            getServer().sendMessage(Component.text("主世界：")
                    .append(Component.text(owCnt, TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
                    .append(Component.space())
                    .append(Component.text("下界："))
                    .append(Component.text(netherCnt, TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
                    .append(Component.space())
                    .append(Component.text("末地："))
                    .append(Component.text(teCnt, TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
            );
        }
    }

    public static void tipsBehaviour(){
        ConfigFile DRNG_TIPS = ownerPlugin.getConfigManager().getConfig(Key.key("drng:tips"));
        JsonArray tips = DRNG_TIPS.getKey("tips").getAsJsonArray();
        getServer().sendMessage(Component.text("[")
                .decorate(TextDecoration.BOLD)
                .append(Component.text("提示").color(TextColor.color(Color.YELLOW.getRGB())))
                .append(Component.text("]:"))
        );
        getServer().sendMessage(Component.text(tips.get(new Random().nextInt(tips.size())).getAsString()));
    }

    public static Component getFlowingNoticeBoard(){
        Component nb = Component.text("----------流动公告----------").append(Component.newline());
        ConfigFile NOTICE = ownerPlugin.getConfigManager().getConfig(Key.key("drng:notices"));
        if(NOTICE.has("notices")){
            for(JsonElement element : NOTICE.getKey("notices").getAsJsonArray()){
                JsonObject jsonObject = element.getAsJsonObject();
                nb = nb.append(Component.text(jsonObject.get("author").getAsString(),TextColor.color(Color.GRAY.getRGB()))).append(Component.text(":"))
                        .append(Component.space())
                        .append(Component.text(jsonObject.get("introduction").getAsString()))
                        .append(Component.newline());
            }
            nb = nb.append(Component.text("点击查看详情",TextColor.color(Color.YELLOW.getRGB())))
                    .clickEvent(ClickEvent.callback(audience -> {
                        audience.openBook(NoticeCmd.getNoticeBook());
                    }, ClickCallback.Options.builder().lifetime(Duration.ofSeconds(180)).build()))
                    .append(Component.newline());
            return nb;
        }else{
            return null;
        }
    }
}
