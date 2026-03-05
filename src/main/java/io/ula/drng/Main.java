package io.ula.drng;

import io.ula.drng.commands.ControlCmd;

import static io.ula.drng.utils.PlayerUtils.*;

import io.ula.drng.config.Configs;
import io.ula.drng.utils.PlayerUtils;
import io.ula.drng.utils.TBUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.awt.*;
import java.io.File;


public final class Main extends JavaPlugin {
    private final String PLG_ID = "dr-ng";

    @Override
    public void onEnable() {
        ControlCmd.plugin = this;
        //为命令类的plugin对象传值
        ScoreBoardHelper.init(this);
        Configs.init(this);
        eventRegister();
        startTasks();
    }

    @Override
    public void onDisable(){

    }
    private void eventRegister(){
        this.getServer().getPluginManager().registerEvents(new PlayerBehaviourListener(this),this);
        this.getServer().getPluginManager().registerEvents(new ServerJoinListener(this),this);
    }

    private void startTasks(){
        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.runTaskTimer(this, TBUtils::tipsBehaviour,1800*20,1800*20);//定时发送提示
        scheduler.runTaskTimer(this, () -> {
            TBUtils.aliceBehaviour();
            PlayerUtils.balanceFunction();
        },20,20);
        scheduler.runTaskTimer(this, () -> playerUpdateOnlineTime(this) ,60*20,60*20);
    }
}
/*
TODO:滚动公告栏
TODO:更多配置项
TODO:优化PLG_ID与VERSION
TODO:配置文件初始内容
 */