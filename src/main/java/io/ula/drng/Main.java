package io.ula.drng;

import io.ula.drng.commands.*;


import io.ula.drng.config.ConfigManager;
import io.ula.drng.config.Configs;
import io.ula.drng.utils.PlayerUtils;
import io.ula.drng.utils.TBUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;


public final class Main extends JavaPlugin {
    private final String PLG_ID = "dr-ng";
    private ConfigManager configManager = DrngBootstrap.configManager;
    @Override
    public void onEnable() {
        ControlCmd.plugin = this;
        ConfigCmd.ownerPlugin = this;
        BindCmd.ownerPlugin = this;
        HomeCmd.ownerPlugin = this;
        NoticeCmd.ownerPlugin = this;
        PlayerUtils.ownerPlugin = this;
        TBUtils.ownerPlugin = this;
        //为命令类的plugin对象传值
        ScoreBoardHelper.init(this);
        Configs.init(this);
        eventRegister();
        startTasks();
        //主插件
        io.ula.hg.HgMain.init(this);
        //猎人游戏
    }

    @Override
    public void onDisable(){
        this.configManager.onDisabled();
    }
    private void eventRegister(){
        this.getServer().getPluginManager().registerEvents(new PlayerBehaviourListener(this),this);
        this.getServer().getPluginManager().registerEvents(new ServerJoinListener(this),this);
        //主插件
        this.getServer().getPluginManager().registerEvents(new io.ula.hg.HgPlayerListener(this),this);
    }

    public ConfigManager getConfigManager(){
        return this.configManager;
    }

    private void startTasks(){
        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.runTaskTimer(this, TBUtils::tipsBehaviour,1800*20,1800*20);//定时发送提示
        scheduler.runTaskTimer(this, TBUtils::aliceBehaviour,20,20);
        scheduler.runTaskTimer(this, () -> PlayerUtils.playerUpdateOnlineTime(this) ,60*20,60*20);
    }
}
/*
TODO:滚动公告栏
TODO:更多配置项
TODO:优化PLG_ID与VERSION
TODO:配置文件初始内容
 */