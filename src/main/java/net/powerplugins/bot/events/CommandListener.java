package net.powerplugins.bot.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.commands.CmdPowerPlugins;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener{
    
    private final PowerPlugins plugin;
    
    public CommandListener(PowerPlugins plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event){
        final String msg = event.getMessage().toLowerCase().substring(1);
        if(msg.startsWith("pl ") || msg.startsWith("plugins ") || msg.equals("pl") || msg.equals("plugins")){
            event.setCancelled(true);
            
            final String[] cmd = msg.split("\\s", 2);
            String [] args = new String[0];
            if(cmd.length > 1)
                args = cmd[1].split("\\s");
            
            Command command = plugin.getCommand("powerplugins");
            if(command == null){
                event.getPlayer().sendMessage(Component.text("Could not process the command!", NamedTextColor.RED));
                return;
            }
            
            final CmdPowerPlugins plugins = plugin.getCmdPowerPlugins();
            plugins.onCommand(event.getPlayer(), command, "", args);
        }
    }
}
