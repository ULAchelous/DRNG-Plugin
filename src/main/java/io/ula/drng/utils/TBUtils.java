package io.ula.drng.utils;

import com.google.gson.JsonArray;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.awt.*;
import java.util.Random;

import static io.ula.drng.config.Configs.DRNG_TIPS;
import static org.bukkit.Bukkit.getServer;

public class TBUtils {
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
        DRNG_TIPS.reload();
        JsonArray tips = DRNG_TIPS.getKey("tips").getAsJsonArray();
        getServer().sendMessage(Component.text("[")
                .decorate(TextDecoration.BOLD)
                .append(Component.text("提示").color(TextColor.color(Color.YELLOW.getRGB())))
                .append(Component.text("]:"))
        );
        getServer().sendMessage(Component.text(tips.get(new Random().nextInt(tips.size())).getAsString()));
    }
}
