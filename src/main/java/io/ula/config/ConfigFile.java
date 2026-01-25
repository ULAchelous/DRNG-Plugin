package io.ula.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.ula.drng.PLG_ID;
import static io.ula.drng.serverRoot;

public class ConfigFile {
    private final Logger LOGGER = LogManager.getLogger(PLG_ID);
    private JsonObject jsonObject = new JsonObject();
    private File file;
    private String file_name;
    public ConfigFile(@NonNull String name){
        file = new File(String.format(serverRoot.getPath()+"/config/dr-ng/%s",name));
        file_name = name;
        init();
    }
    public void init(){
        if(!Files.exists(Path.of(new File(serverRoot.getPath()+"/config/dr-ng").toURI()))){
            try{
                Files.createDirectory(Path.of(new File(serverRoot.getPath()+"/config/dr-ng").toURI()));
            }catch(Exception e){
                LOGGER.error("Failed to create config directory :" + e.getMessage());
                return;
            }
        }
        if(Files.exists(Path.of(file.toURI()))){
            reload();
        }else{
            try {
                Files.createFile(Path.of(file.toURI()));
            }catch(IOException e){
                LOGGER.error(String.format("Failed to create config file \"%s\" : ",file_name)+e.getMessage());
                return;
            }
            addKey("version","indev-v0.1");
        }
        LOGGER.info(String.format("Loaded config file \"%s\"",file_name));
    }
    public void addKey(String name,String key){jsonObject.addProperty(name,key);write();reload();}
    public void addKey(String name,Boolean key){jsonObject.addProperty(name,key);write();reload();}
    public void addKey(String name,Number key){jsonObject.addProperty(name,key);write();reload();}
    public void addKey(String name,JsonElement key){jsonObject.add(name,key);write();reload();}

    public JsonElement getKey(String name){return jsonObject.get(name);}

    public void write(){

        try {
            Files.write(Path.of(file.toURI()), jsonObject.toString().getBytes());
        }catch(IOException e){
            LOGGER.error(String.format("Failed to write config file \"%s\" : ",file_name)+e.getMessage());
            return;
        }
    }

    public void reload(){
        String list;
        try {
            list = new String(Files.readAllBytes(Path.of(file.toURI())));
            jsonObject = JsonParser.parseString(list).getAsJsonObject();
        }catch(Exception e){
            LOGGER.error(String.format("Failed to read config file \"%s\" : ",file_name) + e.getMessage());
            return;
        }
    }
}
