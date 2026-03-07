package io.ula.hg;

import io.ula.drng.ScoreBoardHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;

import java.awt.*;

public class HgUtils {
    private static Boolean clock=false;
    public static void hgPreProcess(JavaPlugin plugin, Player player){
        int playerNum = plugin.getServer().getOnlinePlayers().size();
        if(!clock){
            new BukkitRunnable(){
                int cnt=0;
                int cnt_cache=-1;
                int max=0;
                @Override
                public void run() {
                    if(playerNum >= 3){
                        max = 30;
                        if(playerNum >= 6) max = 6;
                        if(max != 0 && max - cnt <= (playerNum <6?10:5)){
                            for(Player p  : plugin.getServer().getOnlinePlayers()){
                                Objective objective = ScoreBoardHelper.createObjective(p);
                                if(max-cnt == 0){
                                    if(p.getMetadata("speedrunner").getFirst().asBoolean()){
                                        p.showTitle(Title.title(Component.text("你是 ").append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)).append(Component.text("速通者",TextColor.color(Color.RED.getRGB()), TextDecoration.BOLD)).append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)),Component.text("在猎人的追杀下完成速通！")));
                                    }else{
                                        p.showTitle(Title.title(Component.text("你是 ").append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)).append(Component.text("猎人",TextColor.color(Color.CYAN.getRGB()), TextDecoration.BOLD)).append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)),Component.text("追杀并阻止速通者！")));
                                    }
                                }
                                p.showTitle(Title.title(Component.text(cnt,TextColor.color(Color.YELLOW.getRGB())),Component.empty()));
                                if(cnt_cache == -1){
                                    p.getScoreboard().resetScores("§e等待更多玩家");
                                }else{
                                    p.getScoreboard().resetScores(String.format("倒计时: §b§l%d",cnt_cache));
                                }
                                objective.getScore(String.format("倒计时: §b§l%d",max-cnt)).setScore(4);
                            }
                            cnt_cache = max-cnt;
                        }
                    }
                    cnt++;
                }
            }.runTaskTimer(plugin,0,20);
        }
    }


}
