package io.ula.drng.config;

import com.google.gson.JsonParser;
import io.ula.drng.Main;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class InlineConfigFile extends ConfigFile{

    private String path;
    public InlineConfigFile(@NonNull String path,@NonNull String name, JavaPlugin plugin){
        super(name,null,null,plugin);
        this.path = path + '/' + name;
        read();
    }
    private void read(){
        InputStream is = Main.class.getClassLoader().getResourceAsStream(path);
        String content = "";
        if(is != null) content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        try {
            jsonObject = JsonParser.parseString(content).getAsJsonObject();
        }catch(Exception e){
            LOGGER.error(String.format("Failed to read config file \"%s\" : ",file_name) + e.getMessage());
            return;
        }
    }

    @Override
    public void createDir() {}
    @Override
    public void createFile() {}

    @Override
    public void write() {}

    @Override
    public void reload() {}
}
