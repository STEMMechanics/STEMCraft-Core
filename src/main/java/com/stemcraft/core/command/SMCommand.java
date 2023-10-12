package com.stemcraft.core.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.exception.SMCommandException;
import com.stemcraft.core.interfaces.SMCommandProcessor;
import com.stemcraft.core.tabcomplete.SMTabComplete;
import lombok.Getter;

public class SMCommand implements TabCompleter {
    // private PluginCommand command = null;

    /*
     * Command label.
     */
    @Getter
    private String label = "";

    /**
     * The list of command aliases.
     */

    private List<String> aliases = new ArrayList<>();

    /**
     * The permission required for this command.
     */
    private String permission = "";

    /**
     * The command processor (callback).
     */
    private SMCommandProcessor processor = null;

    /**
     * Command tab completion data
     */
    private List<Object[]> tabCompletionList = new ArrayList<>();

    /**
     * Constructor
     * @param label
     */
    public SMCommand(String label) {
        this.label = label;
    }

    /**
     * Add aliases to the command.
     * @param aliases
     * @return
     */
    public SMCommand alias(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    /**
     * Set the permission required for the command.
     * @param permission
     * @return
     */
    public SMCommand permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SMCommand tabComplete(String[] completion) {
        this.tabCompletionList.add(completion);
        return this;
    }

    public SMCommand tabComplete(Object... completions) {
        for (Object completion : completions) {
            if (!(completion instanceof String) && !(completion instanceof String[])) {
                // Unsupported type - you might want to throw an exception or log a warning
                throw new IllegalArgumentException("Unsupported completion type: " + completion.getClass());
            }
        }

        this.tabCompletionList.add(completions);
        return this;
    }

    /**
     * Set the callback processor when the command is used.
     * @param processor
     * @return
     */
    public SMCommand action(SMCommandProcessor processor) {
        this.processor = processor;
        return this;
    }

    /**
     * Register the command on the server
     * @return
     */
    public SMCommand register() {
        CommandMap commandMap = SMBridge.getCommandMap();
        if(commandMap != null) {
            PluginCommand pluginCommand = null;

            try {
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);

                pluginCommand = c.newInstance(label, STEMCraft.getPlugin());
            } catch(Exception e) {
                e.printStackTrace();
            }
        
            if(pluginCommand != null) {
                pluginCommand.setTabCompleter(this);

                if (this.aliases.size() > 0) {
                    pluginCommand.setAliases(aliases);
                }

                pluginCommand.setExecutor((sender, command, label, args) -> {
                    SMCommandContext context = new SMCommandContext(this, sender, label, args);
                    
                    try {
                        if(permission != "" && !sender.hasPermission(permission)) {
                            throw new SMCommandException("You do not have permission to use this command");
                        }

                        processor.process(context);
                    } catch(final SMCommandException ex) {
                        if(ex.getMessages() != null) {
                            SMMessenger.error(sender, Arrays.asList(ex.getMessages()));
                        }
                    }

                    return true;
                });    
                
                if (aliases.size() > 0) {
                    pluginCommand.setAliases(aliases);
                }

                commandMap.register(label, "stemcraft", pluginCommand);
            }
        }

        return this;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> stringList = new ArrayList<>();
        Map<String, List<String>> supplierList = new HashMap<>();

        tabCompletionList.forEach(list -> {
            if(list.length > args.length - 1) {
                Boolean ignoreList = false;

                for (int i = 0; i < args.length - 1; i++) {
                    String[] listToCheck = null;

                    if(list[i] instanceof String) {
                        String listItem = list[i].toString();

                        if(listItem.startsWith("{") && listItem.endsWith("}")) {
                            String tabCompleteFunc = listItem.substring(1, listItem.length() - 1);
                            List<String> tabCompleteFuncList = SMTabComplete.get(tabCompleteFunc);

                            supplierList.put(tabCompleteFunc, tabCompleteFuncList);
                            if(!tabCompleteFuncList.contains(args[i])) {
                                ignoreList = true;
                                break;
                            }

                        } else {
                            // string does not match arg
                            if(args[i] != listItem) {
                                ignoreList = true;
                                break;
                            }
                        }
                    } else if(list[i] instanceof String[]) {
                        listToCheck = (String[])list[i];
                    }

                    if(listToCheck != null) {
                        if(!Arrays.asList(listToCheck).contains(args[i])) {
                            ignoreList = true;
                            break;
                        }
                    }
                }

                if(!ignoreList) {
                    if(list[args.length - 1] instanceof String) {
                        String listItem = (String)list[args.length - 1];
                        if(listItem.startsWith("{") && listItem.endsWith("}")) {
                            String tabCompleteFunc = listItem.substring(1, listItem.length() - 1);
                            if(!supplierList.containsKey(tabCompleteFunc)) {
                                List<String> tabCompleteFuncList = SMTabComplete.get(tabCompleteFunc);
                                supplierList.put(tabCompleteFunc, tabCompleteFuncList);
                            }

                            stringList.addAll(supplierList.get(tabCompleteFunc));
                        } else {
                            stringList.add(listItem);
                        }
                    } else if(list[args.length - 1] instanceof String[]) { 
                        stringList.addAll(Arrays.asList((String[])list[args.length - 1]));
                    }
                }
            }
        });

        if(args[args.length - 1].length() > 0) {
            String startingWith = args[args.length - 1];
            Iterator<String> iterator = stringList.iterator();

            while (iterator.hasNext()) {
                String item = iterator.next();
                if (!item.startsWith(startingWith)) {
                    iterator.remove();
                }
            }
        }

        return stringList;

        // String[] argsList = new String[args.length + 1];
        // argsList[0] = cmd.getName();
        // System.arraycopy(args, 0, argsList, 1, args.length);

        // if (!(sender instanceof Player) || args.length == 0) {
        //     return null;
        // }

        // // Check if the current args match the elements in tabCompletion
        // for (String[] completion : this.tabCompletionList) {
        //     if(completion.length < argsList.length) {
        //         continue;
        //     }

        //     boolean match = true;
        //     for (int i = 0; i < argsList.length - 1; i++) {
        //         if(completion[i].startsWith("%") && completion[i].endsWith("%")) {
        //             String key = completion[i].substring(1, completion[i].length() - 1);
                    
        //             if (this.tabCompletionPlaceholders.containsKey(key)) {
        //                 BiFunction<Server, String, List<String>> biFunction = tabCompletionPlaceholders.get(key);

        //                 if(biFunction.apply(sender.getServer(), argsList[i]).isEmpty()) {
        //                     match = false;
        //                     break;
        //                 }
        //             }
        //         } else if (!completion[i].equals(argsList[i])) {
        //             match = false;
        //             break;
        //         }
        //     }

        //     if (match) {
        //         String targetCompletion = completion[argsList.length - 1];

        //         if(targetCompletion.startsWith("%") && targetCompletion.endsWith("%")) {
        //             String key = targetCompletion.substring(1, targetCompletion.length() - 1);
                    
        //             if (this.tabCompletionPlaceholders.containsKey(key)) {
        //                 BiFunction<Server, String, List<String>> biFunction = tabCompletionPlaceholders.get(key);

        //                 for(String valueItem : biFunction.apply(sender.getServer(), argsList[argsList.length - 1])) {
        //                     tabCompleteList.add(valueItem);
        //                 }
        //             }
        //         } else if(targetCompletion.startsWith(argsList[argsList.length - 1])) {
        //             tabCompleteList.add(targetCompletion);
        //         }
        //     }
        // }

        // return tabCompleteList;
    }

    // AFTER
    
    
    // protected HashMap<String, BiFunction<Server, String, List<String>>> tabCompletionPlaceholders = new HashMap<>();
    // protected HashMap<String, BiFunction<CommandSender, String[], Boolean>> stemcraftOptions = new HashMap<>();

    // public void addTabCompletions(String[][] tabCompletions) {
    //     if(tabCompletions != null && tabCompletions.length > 0) {
    //         int newSize = this.tabCompletionList.length + tabCompletions.length;
    //         String[][] combinedArray = new String[newSize][];

    //         System.arraycopy(this.tabCompletionList, 0, combinedArray, 0, this.tabCompletionList.length);
    //         System.arraycopy(tabCompletions, 0, combinedArray, this.tabCompletionList.length, tabCompletions.length);
    //         this.tabCompletionList = combinedArray;
    //     }
    // }



    // public void registerTabPlaceholder(String placeholder, BiFunction<Server, String, List<String>> callback) {
    //     this.tabCompletionPlaceholders.put(placeholder, callback);
    // }

    // public void registerStemCraftOption(String option, BiFunction<CommandSender, String[], Boolean> callback) {
    //     this.registerStemCraftOption(option, callback, null);
    // }
    
    // public void registerStemCraftOption(String option, BiFunction<CommandSender, String[], Boolean> callback, String[][] tabCompletions) {
    //     this.stemcraftOptions.put(option, callback);
    //     if(tabCompletions != null) {
    //         this.addTabCompletions(tabCompletions);
    //     } else {
    //         this.addTabCompletions(new String[][] {{"stemcraft", option}});
    //     }
    // }

    // protected List<String> player(Server server, String match) {
    //     List<String> playerList = new ArrayList<>();

    //     for(Player player : server.getOnlinePlayers()) {
    //         String playerName = player.getName();
    //         if(playerName.startsWith(match)) {
    //             playerList.add(playerName);
    //         }
    //     }

    //     return playerList;
    // }
}
