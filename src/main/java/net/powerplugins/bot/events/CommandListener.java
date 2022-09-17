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
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

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
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandTabComplete(TabCompleteEvent event){
        String command = event.getBuffer();
        if(!command.startsWith("/pl") && !command.startsWith("/plugins"))
            return;
        
        String[] args = Arrays.copyOfRange(command.split("\s"), 1, command.split("\s").length);
        if(args.length == 0){
            event.setCompletions(Arrays.asList("free", "info", "premium", "private"));
            return;
        }
        
        if(args.length == 1){
            List<String> suggestions = getPartialMatches(args[0], Arrays.asList("free", "info", "premium", "private"));
            
            event.setCompletions(suggestions);
        }else
        if(args.length == 2){
            if(!args[0].equalsIgnoreCase("info")){
                event.setCompletions(Collections.emptyList());
                return;
            }
            
            List<String> plugins = plugin.retrievePlugins().stream()
                .map(Plugin::getName)
                .collect(Collectors.toList());
            List<String> suggestions = getPartialMatches(args[1], plugins);
            
            event.setCompletions(suggestions);
        }
    }
    
    public static List<String> getPartialMatches(String token, Collection<String> originals){
        if(originals == null || originals.isEmpty())
            return Collections.emptyList();
        
        if(token == null || token.isEmpty())
            return new ArrayList<>(originals);
        
        List<String> matches = new ArrayList<>();
        for(String str : originals){
            if(str.length() >= token.length() && str.regionMatches(true, 0, token, 0, token.length()))
                matches.add(str);
        }
        
        return matches;
    }
}
