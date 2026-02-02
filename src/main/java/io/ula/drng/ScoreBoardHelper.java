package io.ula.drng;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreBoardHelper {
    private static ScoreboardManager scoreboardManager;
    private static Map<UUID,Objective> objectives = new HashMap<>();

    private static JavaPlugin plugin;
    public static void init(JavaPlugin plg){
        plugin=plg;
        scoreboardManager = plugin.getServer().getScoreboardManager();
    }
    public static void createObjective(Player player){
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Component server_name = Component.text("希").color(TextColor.fromCSSHexString("#00CCFF"))
                .append(Component.text("望").color(TextColor.fromCSSHexString("#0099CC")))
                .append(Component.text("之").color(TextColor.fromCSSHexString("#006699")))
                .append(Component.text("地").color(TextColor.fromCSSHexString("#003366")))
                .append(Component.text(" - NextGen").color(TextColor.fromCSSHexString("#003366")))
                .decorate(TextDecoration.ITALIC)
                .decorate(TextDecoration.BOLD);
        Objective sidebar= scoreboard.registerNewObjective(player.getName(), Criteria.DUMMY, server_name);
        Objective health_display = scoreboard.registerNewObjective("health",Criteria.HEALTH,Component.text("health"),RenderType.HEARTS);

        health_display.setAutoUpdateDisplay(true);
        sidebar.getScore("§e欢迎参加测试!").setScore(8);
        sidebar.getScore("在线时长: " + getOnlineTime(player.getMetadata("onlineTime").getFirst().asInt())).setScore(7);
        sidebar.getScore(String.format("死亡计数: §b§l%d",player.getMetadata("deathCount").getFirst().asInt())).setScore(6);
        sidebar.getScore(String.format("挖掘计数: §b§l%d",player.getMetadata("digCount").getFirst().asInt())).setScore(5);
        sidebar.getScore(String.format("使用 §e§l/notice")).setScore(4);
        sidebar.getScore("来发布和查看公告").setScore(3);
        sidebar.getScore(String.format("使用 §e§l/pms")).setScore(2);
        sidebar.getScore(" 来申请权限").setScore(1);

        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        health_display.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        player.setScoreboard(scoreboard);
        objectives.put(player.getUniqueId(),sidebar);
    }

    public static void removeObjective(Player player) {
        objectives.remove(player.getUniqueId());
        player.setScoreboard(scoreboardManager.getMainScoreboard());
    }
    private static String getOnlineTime(int time){
        int hour = time / 60;
        int minute = time % 60;
        return String.format("§e%d§r小时§e%d§r分钟",hour,minute);
    }

    public static void updateScores(Player player,int type) {
        Objective objective = objectives.get(player.getUniqueId());
        //更新数值
        switch(type) {
            case 1:
                player.getScoreboard().resetScores(String.format("死亡计数: §b§l%d", player.getMetadata("deathCountCache").getFirst().asInt()));
                objective.getScore(String.format("死亡计数: §b§l%d", player.getMetadata("deathCount").getFirst().asInt())).setScore(6);
                break;
            case 2:
                player.getScoreboard().resetScores(String.format("挖掘计数: §b§l%d", player.getMetadata("digCountCache").getFirst().asInt()));
                objective.getScore(String.format("挖掘计数: §b§l%d", player.getMetadata("digCount").getFirst().asInt())).setScore(5);
                break;
            case 3:
                player.getScoreboard().resetScores("在线时长: " + getOnlineTime(player.getMetadata("onlineTimeCache").getFirst().asInt()));
                objective.getScore("在线时长: " + getOnlineTime(player.getMetadata("onlineTime").getFirst().asInt())).setScore(7);
                break;
        }
    }
}
