package net.powerplugins.bot.manager;

import net.powerplugins.bot.PowerPlugins;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileManager{
    
    private final PowerPlugins instance;
    private final File folder;
    
    public FileManager(PowerPlugins instance){
        this.instance = instance;
        this.folder = new File(instance.getDataFolder() + "/plugins");
    }
    
    public PluginFile getPluginFile(Plugin plugin){
        File file = getFile(plugin.getName().toLowerCase());
        
        if(file == null)
            return null;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> depends = plugin.getDescription().getDepend();
        List<String> softDepends = plugin.getDescription().getSoftDepend();
        
        if(config.get("info.name") == null){
            config.set("info.name", plugin.getName());
            config.set("info.authors", plugin.getDescription().getAuthors());
            config.set("info.version", plugin.getDescription().getVersion());
            config.set("info.depends", depends);
            config.set("info.softdepends", softDepends);
            config.set("info.url", "");
            config.set("info.description", "No description provided");
            config.set("info.category", "private");
        }else{
            config.set("info.depends", depends);
            config.set("info.softdepends", softDepends);
        }
        
        try{
            config.save(file);
            
            return new PluginFile(config);
        }catch(IOException ex){
            return null;
        }
    
    }
    
    public boolean isDifferent(Plugin plugin, PluginFile pluginFile){
        if(pluginFile.isNew()){
            instance.getLogger().info(plugin.getName() + " has been recently added. Adding to Queue...");
            
            return true;
        }
        
        String currentVersion = pluginFile.getVersion();
        String newVersion = plugin.getDescription().getVersion();
        
        if(!currentVersion.equals(newVersion)){
            try{
                YamlConfiguration config = pluginFile.getConfiguration();
                File file = getFile(plugin.getName().toLowerCase());
                
                if(file == null)
                    return false;
                
                config.set("info.version", plugin.getDescription().getVersion());
                config.save(file);
                
                instance.getLogger().info(plugin.getName() + " has been updated. Adding to Queue...");
                
                return true;
            }catch(IOException ex){
                instance.getLogger().warning("Could not update file " + plugin.getName().toLowerCase() + ".yml!");
                ex.printStackTrace();
                
                return false;
            }
        }
        
        return false;
    }
    
    private File getFile(String name){
        File file = new File(folder, name + ".yml");
        if(folder.mkdirs())
            instance.getLogger().info("Created folder 'PowerPlugins/plugins/'");
        
        if(file.exists())
            return file;
        
        try{
            if(file.createNewFile())
                instance.getLogger().info("Created file " + name + ".yml!");
            
            return file;
        }catch(IOException ex){
            instance.getLogger().warning("Could not load or create file " + name + ".yml!");
            return null;
        }
    }
    
    public static class PluginFile{
        
        private final String name;
        private final List<String> authors;
        private final String version;
        
        private final String url;
        private final String description;
        private final String category;
        private final List<String> depends;
        private final List<String> softDepends;
        
        private final YamlConfiguration configuration;
        
        
        public PluginFile(YamlConfiguration configuration){
            this.name = configuration.getString("info.name");
            this.authors = configuration.getStringList("info.authors");
            this.version = configuration.getString("info.version");
            this.depends = configuration.getStringList("info.depends");
            this.softDepends = configuration.getStringList("info.softdepends");
            
            this.url = configuration.getString("info.url");
            this.description = configuration.getString("info.description");
            this.category = configuration.getString("info.category");
            
            this.configuration = configuration;
        }
    
        public String getName(){
            return name;
        }
    
        public List<String> getAuthors(){
            return authors;
        }
    
        public String getVersion(){
            return version;
        }
    
        public String getUrl(){
            return url;
        }
    
        public String getDescription(){
            return description;
        }
    
        public String getCategory(){
            return category;
        }
    
        public List<String> getDepends(){
            return depends;
        }
    
        public List<String> getSoftDepends(){
            return softDepends;
        }
    
        public YamlConfiguration getConfiguration(){
            return configuration;
        }
        
        public boolean isNew(){
            return url.isEmpty();
        }
    }
}
