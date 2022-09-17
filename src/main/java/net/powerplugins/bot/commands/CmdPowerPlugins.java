package net.powerplugins.bot.commands;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.events.CommandListener;
import net.powerplugins.bot.manager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CmdPowerPlugins implements CommandExecutor, TabCompleter{
    
    private final TextColor BRAND_COLOUR = TextColor.color(0xF39C12);
    
    private final PowerPlugins plugin;
    
    public CmdPowerPlugins(PowerPlugins plugin){
        this.plugin = plugin;
    }
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
        if(!(sender instanceof Player player)){
            sender.sendMessage(Component.text("Only Players can execute this command!", NamedTextColor.RED));
            return true;
        }
    
        if(args.length == 0){
            clear(player);
    
            TextComponent component = Component.text()
                .append(getFooter(0))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Please click on a category", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(
                    Component.text("[", NamedTextColor.GRAY)
                        .append(Component.text("Free Plugins", NamedTextColor.GREEN))
                        .append(Component.text("]", NamedTextColor.GRAY))
                        .hoverEvent(HoverEvent.showText(
                            Component.text("Plugins you can download for free.", NamedTextColor.GRAY)
                                .append(Component.newline())
                                .append(Component.newline())
                                .append(Component.text("/plugins free", NamedTextColor.GREEN))
                        ))
                        .clickEvent(ClickEvent.runCommand("/powerplugins free"))
                )
                .append(Component.newline())
                .append(
                    Component.text("[", NamedTextColor.GRAY)
                        .append(Component.text("Premium Plugins", NamedTextColor.GOLD))
                        .append(Component.text("]", NamedTextColor.GRAY))
                        .hoverEvent(HoverEvent.showText(
                            Component.text("Plugins you have to pay for to download.", NamedTextColor.GRAY)
                                .append(Component.newline())
                                .append(Component.newline())
                                .append(Component.text("/plugins premium", NamedTextColor.GOLD))
                        ))
                        .clickEvent(ClickEvent.runCommand("/powerplugins premium"))
                )
                .append(Component.newline())
                .append(
                    Component.text("[", NamedTextColor.GRAY)
                        .append(Component.text("Private Plugins", NamedTextColor.GRAY))
                        .append(Component.text("]", NamedTextColor.GRAY))
                        .hoverEvent(HoverEvent.showText(
                            Component.text("Selfmade plugins used for this server.", NamedTextColor.GRAY)
                                .append(Component.newline())
                                .append(Component.newline())
                                .append(Component.text("/plugins private", NamedTextColor.GRAY))
                        ))
                        .clickEvent(ClickEvent.runCommand("/powerplugins private"))
                )
                .append(Component.newline())
                .append(Component.newline())
                .append(getFooter(0))
                .build();
    
            player.sendMessage(component);
            return true;
        }else
        if(args[0].equalsIgnoreCase("info")){
            if(args.length == 1){
                player.sendMessage(Component.text("Please provide the name of a plugin.", NamedTextColor.RED));
                return true;
            }
            
            Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
            if(pl == null){
                player.sendMessage(
                    Component.text("The provided plugin doesn't exist!")
                        .append(Component.newline())
                        .append(Component.text("Make sure you typed the name correctly."))
                        .color(NamedTextColor.RED)
                );
                return true;
            }
    
            FileManager.PluginFile file = plugin.getFileManager().getPluginFile(pl);
            if(file == null){
                player.sendMessage(
                    Component.text("The provided plugin doesn't exist!")
                        .append(Component.newline())
                        .append(Component.text("Make sure you typed the name correctly."))
                        .color(NamedTextColor.RED)
                );
                return true;
            }
            
            TextComponent info = getAdvancedPluginInfo(file);
            if(info == null){
                player.sendMessage(Component.text("Could not retrieve information about the plugin!", NamedTextColor.RED));
                return true;
            }
            
            clear(player);
            player.sendMessage(info);
            return true;
        }else{
            Category category = Category.getFromName(args[0]);
            if(category == null){
                player.sendMessage(Component.text("Received unknown category " + args[0], NamedTextColor.RED));
                return true;
            }
            
            List<TextComponent> pages = getPages(category);
            
            if(pages.isEmpty()){
                player.sendMessage(Component.text("This category does not have any plugins listed.", NamedTextColor.RED));
                return true;
            }
            
            if(args.length == 1){
                clear(player);
                player.sendMessage(pages.get(0));
                
                return true;
            }else{
                int page;
                try{
                    page = Integer.parseInt(args[1]);
                }catch(NumberFormatException ignored){
                    player.sendMessage(Component.text("Invalid argument! Expected number but got " + args[1], NamedTextColor.RED));
                    return true;
                }
                
                if(page <= 0){
                    player.sendMessage(Component.text("You can't provide a page number below 1.", NamedTextColor.RED));
                    return true;
                }else
                if(page > pages.size()){
                    player.sendMessage(
                        Component.text("Received a too high number!")
                            .append(Component.newline())
                            .append(
                                Component.text("Category ")
                                    .append(Component.text(category.getTitle()))
                                    .append(Component.text(" only has a total of "))
                                    .append(Component.text(pages.size()))
                                    .append(Component.text(" pages."))
                                    .color(NamedTextColor.RED)
                            )
                    );
                    return true;
                }else{
                    clear(player);
                    player.sendMessage(pages.get(page - 1));
                    return true;
                }
            }
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
        if(args.length == 1){
            return CommandListener.getPartialMatches(args[0], Arrays.asList("free", "info", "premium", "private"));
        }
        
        if(args.length == 2){
            if(!args[1].equalsIgnoreCase("info"))
                return Collections.emptyList();
            
            List<String> plugins = plugin.retrievePlugins().stream()
                .map(Plugin::getName)
                .collect(Collectors.toList());
            
            return CommandListener.getPartialMatches(args[1], plugins);
        }
        
        return Collections.emptyList();
    }
    
    private void clear(Player player){
        for(int i = 0; i < 20; i++){
            player.sendMessage("");
        }
    }
    
    private TextComponent getHeader(Category category){
        return switch(category){
            case FREE -> Component.text()
                .append(Component.text("---------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text("[ ", NamedTextColor.GRAY))
                .append(Component.text("Free", NamedTextColor.GREEN))
                .append(Component.text(" ]", NamedTextColor.GRAY))
                .append(Component.text("---------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .build();
            case PREMIUM -> Component.text()
                .append(Component.text("---------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text("[ ", NamedTextColor.GRAY))
                .append(Component.text("Premium", NamedTextColor.GOLD))
                .append(Component.text(" ]", NamedTextColor.GRAY))
                .append(Component.text("---------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .build();
            case PRIVATE -> Component.text()
                .append(Component.text("---------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text("[ ", NamedTextColor.GRAY))
                .append(Component.text("Private", NamedTextColor.GRAY))
                .append(Component.text(" ]", NamedTextColor.GRAY))
                .append(Component.text("---------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .build();
        };
    }
    
    private TextComponent getFooter(int page){
        if(page == 0){
            return Component.text("-------------------------------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH);
        }else{
            return Component.text()
                .append(Component.text("--------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text("[ ", NamedTextColor.GRAY))
                .append(Component.text("Page " + page, BRAND_COLOUR))
                .append(Component.text(" ]", NamedTextColor.GRAY))
                .append(Component.text("--------------", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .build();
        }
    }
    
    private List<TextComponent> getPages(Category category){
        List<List<FileManager.PluginFile>> rawPages = Lists.partition(getPlugins(category), 7);
        List<TextComponent> pages = new ArrayList<>();
        
        int currentPage = 0;
        final int totalPages = rawPages.size();
        
        for(List<FileManager.PluginFile> page : rawPages){
            currentPage++;
            
            TextComponent.Builder component = Component.text()
                .append(getPrevNavButton(category, currentPage))
                .append(getHeader(category))
                .append(getNextNavButton(category, currentPage, totalPages));
            
            for(FileManager.PluginFile file : page){
                component.append(getSimplePluginInfo(file));
            }
            
            if(page.size() < 7){
                int padding = page.size();
                
                while(padding < 7){
                    padding++;
    
                    component.append(Component.newline());
                }
            }
            
            component.append(Component.newline())
                .append(getPrevNavButton(category, currentPage))
                .append(getFooter(currentPage))
                .append(getNextNavButton(category, currentPage, totalPages));
            
            pages.add(component.build());
        }
        
        return pages;
    }
    
    private List<FileManager.PluginFile> getPlugins(Category category){
        return plugin.retrievePlugins().stream()
            .map(plugin.getFileManager()::getPluginFile)
            .filter(Objects::nonNull)
            .filter(file -> file.getCategory().equalsIgnoreCase(category.getTitle()))
            .collect(Collectors.toList());
    }
    
    private TextComponent getSimplePluginInfo(FileManager.PluginFile file){
        String author = file.getAuthors().isEmpty() ? "Unknown Author" : file.getAuthors().get(0);
        String description = file.getDescription().length() > 30 ? file.getDescription().substring(0, 25) + "..." : file.getDescription();
        
        return Component.text()
            .append(Component.newline())
            .append(
                Component.text(file.getName(), BRAND_COLOUR)
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(author, NamedTextColor.WHITE))
                    .append(Component.text(" [", NamedTextColor.GRAY))
                    .append(Component.text(file.getVersion(), BRAND_COLOUR))
                    .append(Component.text("]", NamedTextColor.GRAY))
            ).hoverEvent(
                HoverEvent.showText(
                    Component.text(description, NamedTextColor.GRAY)
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("Click for more information.", BRAND_COLOUR))
                )
            ).clickEvent(
                ClickEvent.runCommand("/powerplugins info " + file.getName())
            ).build();
    }
    
    private TextComponent getAdvancedPluginInfo(FileManager.PluginFile file){
        Category category = Category.getFromName(file.getCategory());
        if(category == null){
            return null;
        }
        
        TextComponent.Builder component = Component.text()
            .append(
                Component.text("[", NamedTextColor.GRAY)
                    .append(Component.text("<", BRAND_COLOUR))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .hoverEvent(
                        HoverEvent.showText(Component.text("Back to category " + category.getTitle(), NamedTextColor.GRAY))
                    ).clickEvent(
                        ClickEvent.runCommand("/powerplugins " + category.getTitle())
                    )
            ).append(getHeader(category))
            .append(getNextNavButton(category))
            .append(Component.newline())
            
            .append(Component.text(file.getName(), BRAND_COLOUR))
            .append(Component.text(" [", NamedTextColor.GRAY))
            .append(Component.text(file.getVersion(), NamedTextColor.WHITE))
            .append(Component.text("]", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.newline())
            
            .append(Component.text("Authors: ", NamedTextColor.GRAY))
            .append(Component.text(plugin.getAuthors(file.getAuthors())))
            .append(Component.newline());
        
        Map<String, Boolean> dependencies = new TreeMap<>();
        
        for(String depends : file.getDepends()){
            dependencies.put(depends, true);
        }
        
        for(String softDepends : file.getSoftDepends()){
            dependencies.put(softDepends, false);
        }
        
        if(!dependencies.isEmpty()){
            component.append(Component.text("Dependencies:", NamedTextColor.GRAY));
            
            for(String dependency : dependencies.keySet()){
                component.append(Component.newline())
                    .append(getDependencyInfo(dependency, dependencies.get(dependency)));
            }
            
            component.append(Component.newline());
        }
        
        component.append(Component.newline())
            .append(Component.text("Plugin Page: ", NamedTextColor.GRAY))
            .append(
                Component.text(file.getUrl(), BRAND_COLOUR)
                    .hoverEvent(
                        HoverEvent.showText(Component.text("Click to open URL", NamedTextColor.GRAY))
                    ).clickEvent(
                        ClickEvent.openUrl(file.getUrl())
                    )
            ).append(Component.newline())
            
            .append(Component.text("Description:", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text(file.getDescription(), NamedTextColor.WHITE))
            .append(Component.newline())
    
            .append(
                Component.text("[", NamedTextColor.GRAY)
                    .append(Component.text("<", BRAND_COLOUR))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .hoverEvent(
                        HoverEvent.showText(Component.text("Back to category " + category.getTitle(), NamedTextColor.GRAY))
                    ).clickEvent(
                        ClickEvent.runCommand("/powerplugins " + category.getTitle())
                    )
            ).append(getHeader(category))
            .append(getNextNavButton(category));
        
        return component.build();
    }
    
    private TextComponent getPrevNavButton(Category category, int page){
        TextComponent component = Component.empty()
            .append(
                Component.text("[", NamedTextColor.GRAY)
                    .append(Component.text("<", BRAND_COLOUR))
                    .append(Component.text("]", NamedTextColor.GRAY))
            );
        
        if(page > 1){
            return component.hoverEvent(
                HoverEvent.showText(Component.text("Page " + (page - 1), NamedTextColor.GRAY))
            ).clickEvent(
                ClickEvent.runCommand("/powerplugins " + category.getTitle() + " " + (page - 1))
            );
        }else{
            return component.hoverEvent(
                HoverEvent.showText(Component.text("Back to selection", NamedTextColor.GRAY))
            ).clickEvent(
                ClickEvent.runCommand("/powerplugins")
            );
        }
    }
    
    private TextComponent getNextNavButton(Category category){
        return getNextNavButton(category, 0, 0);
    }
    
    private TextComponent getNextNavButton(Category category, int page, int totalPages){
        TextComponent component = Component.empty();
        
        if(page < totalPages){
            return component.append(
                Component.text("[", NamedTextColor.GRAY)
                    .append(Component.text(">", BRAND_COLOUR))
                    .append(Component.text("]", NamedTextColor.GRAY))
            ).hoverEvent(
                HoverEvent.showText(Component.text("Page " + (page + 1), NamedTextColor.GRAY))
            ).clickEvent(
                ClickEvent.runCommand("/powerplugins " + category.getTitle() + " " + (page + 1))
            );
        }else{
            return component.append(Component.text("[>]", NamedTextColor.GRAY));
        }
    }
    
    private TextComponent getDependencyInfo(String dependency, boolean required){
        TextComponent.Builder hover = Component.text()
            .append(Component.text("Type: ", NamedTextColor.GRAY));
        
        if(required){
            hover.append(Component.text("Depend ", BRAND_COLOUR))
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text("Required", NamedTextColor.RED))
                .append(Component.text("]", NamedTextColor.GRAY));
        }else{
            hover.append(Component.text("Soft Depend ", BRAND_COLOUR))
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text("Not Required", NamedTextColor.GREEN))
                .append(Component.text("]", NamedTextColor.GRAY));
        }
        
        hover.append(Component.newline())
            .append(Component.newline())
            .append(
                Component.text("Click to view more information if available.")
                    .append(Component.newline())
                    .append(Component.text("Note: The dependency type is determined through the plugin's plugin.yml"))
                    .color(NamedTextColor.GRAY)
            );
        
        return Component.text("- ", NamedTextColor.GRAY)
            .append(
                Component.text(dependency, BRAND_COLOUR)
            ).hoverEvent(
                HoverEvent.showText(hover)
            ).clickEvent(
                ClickEvent.runCommand("/powerplugins info " + dependency)
            );
    }
    
    private enum Category{
        FREE("Free", NamedTextColor.GREEN),
        PREMIUM("Premium", NamedTextColor.GOLD),
        PRIVATE("Private", NamedTextColor.GRAY);
        
        final String title;
        final NamedTextColor color;
        
        Category(String title, NamedTextColor color){
            this.title = title;
            this.color = color;
        }
        
        public static Category getFromName(String title){
            for(Category category : values()){
                if(category.getTitle().equalsIgnoreCase(title))
                    return category;
            }
            
            return null;
        }
        
        public String getTitle(){
            return title;
        }
    }
}
