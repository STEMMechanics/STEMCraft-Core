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
    private List<String[]> tabCompletionList = new ArrayList<>();

    /**
     * Constructor
     * 
     * @param label
     */
    public SMCommand(String label) {
        this.label = label;
    }

    /**
     * Add aliases to the command.
     * 
     * @param aliases
     * @return
     */
    public SMCommand alias(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    /**
     * Set the permission required for the command.
     * 
     * @param permission
     * @return
     */
    public SMCommand permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SMCommand tabComplete(String... completion) {
        this.tabCompletionList.add(completion);
        return this;
    }

    public SMCommand tabComplete(Object... completion) {
        List<List<String>> lists = new ArrayList<>();
        for (Object obj : completion) {
            List<String> list = new ArrayList<>();
            if (obj instanceof String) {
                list.add((String) obj);
            } else if (obj instanceof String[]) {
                list.addAll(Arrays.asList((String[]) obj));
            }
            lists.add(list);
        }

        generateCombinationsRecursive(lists, new String[lists.size()], 0);
        return this;
    }

    public void generateCombinationsRecursive(List<List<String>> lists, String[] result, int depth) {
        if (depth == lists.size()) {
            tabComplete(Arrays.copyOf(result, result.length));
            return;
        }

        for (String s : lists.get(depth)) {
            result[depth] = s;
            generateCombinationsRecursive(lists, result, depth + 1);
        }
    }

    /**
     * Set the callback processor when the command is used.
     * 
     * @param processor
     * @return
     */
    public SMCommand action(SMCommandProcessor processor) {
        this.processor = processor;
        return this;
    }

    /**
     * Register the command on the server
     * 
     * @return
     */
    public SMCommand register() {
        CommandMap commandMap = SMBridge.getCommandMap();
        if (commandMap != null) {
            PluginCommand pluginCommand = null;

            try {
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);

                pluginCommand = c.newInstance(label, STEMCraft.getPlugin());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pluginCommand != null) {
                pluginCommand.setTabCompleter(this);

                if (this.aliases.size() > 0) {
                    pluginCommand.setAliases(aliases);
                }

                pluginCommand.setExecutor((sender, command, label, args) -> {
                    SMCommandContext context = new SMCommandContext(this, sender, label, args);

                    try {
                        if (permission != "" && !sender.hasPermission(permission)) {
                            throw new SMCommandException("You do not have permission to use this command");
                        }

                        processor.process(context);
                    } catch (final SMCommandException ex) {
                        if (ex.getMessages() != null) {
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

    private static class TabCompleteValueOption {
        String option = null;
        String value = null;

        TabCompleteValueOption(String option, String value) {
            this.option = option;
            this.value = value;
        }
    }

    private class TabCompleteArgParser {
        List<String> optionArgsAvailable = new ArrayList<>();
        Map<String, List<String>> valueOptionArgsAvailable = new HashMap<>();
        List<String> optionArgsUsed = new ArrayList<>();
        List<String> valueOptionArgsUsed = new ArrayList<>();
        Integer argIndex = 0;
        String[] args;

        public TabCompleteArgParser(String[] args) {
            this.args = args;
        }

        public static String getStringAsOption(String arg) {
            if (arg.startsWith("-")) {
                return arg.toLowerCase();
            }

            return null;
        }

        public void addOption(String option) {
            optionArgsAvailable.add(option);
        }

        public static TabCompleteValueOption getStringAsValueOption(String arg) {
            if (arg.matches("^[a-zA-Z0-9-_]:.*")) {
                String option = arg.substring(0, arg.indexOf(':')).toLowerCase();
                String value = arg.substring(arg.indexOf(':') + 1);

                return new TabCompleteValueOption(option, value);
            }

            return null;
        }

        public void addValueOption(TabCompleteValueOption option) {
            valueOptionArgsAvailable.put(option.option, parseValue(option.value));
        }

        public static List<String> parseValue(String value) {
            List<String> list = new ArrayList<>();

            if (value.startsWith("{") && value.endsWith("}")) {
                String placeholder = value.substring(1, value.length() - 1);
                List<String> placeholderList = SMTabComplete.getCompletionList(placeholder);
                list.addAll(placeholderList);
            } else {
                list.add(value);
            }

            return list;
        }


        public Boolean hasRemainingArgs() {
            return argIndex < args.length - 1;
        }

        public void next() {
            nextMatches(null);
        }

        public Boolean nextMatches(String tabCompletionItem) {
            for (; argIndex < args.length; argIndex++) {
                String arg = args[argIndex];

                String option = getStringAsOption(arg);
                if (option != null) {
                    optionArgsUsed.add(option);
                    optionArgsAvailable.remove(option);
                    continue;
                }

                TabCompleteValueOption valueOption = getStringAsValueOption(arg);
                if (valueOption != null) {
                    valueOptionArgsUsed.add(valueOption.option);
                    valueOptionArgsAvailable.remove(valueOption.option);
                    continue;
                }

                if (tabCompletionItem == null) {
                    argIndex++;
                    return true;
                }

                List<String> values = parseValue(tabCompletionItem);
                if (values.contains(arg)) {
                    argIndex++;
                    return true;
                }

                return false;
            }

            // To get here we are out of args to parse
            return null;
        }

        public void processRemainingArgs() {
            while (hasRemainingArgs()) {
                next();
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabCompletionResults = new ArrayList<>();
        List<String> optionArgsAvailable = new ArrayList<>();
        Map<String, List<String>> valueOptionArgsAvailable = new HashMap<>();
        String[] fullArgs = new String[args.length - 1];

        System.arraycopy(args, 0, fullArgs, 0, args.length - 1);

        // iterate each tab completion list
        tabCompletionList.forEach(list -> {
            Boolean matches = true;
            Integer listIndex = 0;

            // Copy the elements except the last one
            TabCompleteArgParser argParser = new TabCompleteArgParser(fullArgs);

            // iterate each tab completion list item
            for (listIndex = 0; listIndex < list.length; listIndex++) {
                String listItem = list[listIndex];

                // list item is an option
                String option = TabCompleteArgParser.getStringAsOption(listItem);
                if (option != null) {
                    argParser.addOption(option);
                    continue;
                }

                // list item is a value option
                TabCompleteValueOption valueOption = TabCompleteArgParser.getStringAsValueOption(listItem);
                if (valueOption != null) {
                    argParser.addValueOption(valueOption);
                    continue;
                }

                // list item is a string or placeholder
                Boolean nextMatches = argParser.nextMatches(listItem);
                if (nextMatches == null) {
                    tabCompletionResults.addAll(TabCompleteArgParser.parseValue(listItem));
                    break;
                } else if (nextMatches == false) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                // parse remaining arg items
                argParser.processRemainingArgs();

                optionArgsAvailable.addAll(argParser.optionArgsAvailable);
                valueOptionArgsAvailable.putAll(argParser.valueOptionArgsAvailable);
            }
        });

        // remove non-matching items from the results based on what the player has already entered
        if (args[args.length - 1].length() > 0) {
            String arg = args[args.length - 1];

            // if the player has only a dash in the arg, only show dash arguments
            if (arg.equals("-")) {
                return optionArgsAvailable;
            }

            // if the player has written the start of a option arg
            if (arg.contains(":")) {
                // if the option arg is available
                String key = arg.substring(0, arg.indexOf(":"));
                if (valueOptionArgsAvailable.containsKey(key)) {
                    tabCompletionResults.clear();
                    String prefix = key + ":";
                    for (String item : valueOptionArgsAvailable.get(key)) {
                        tabCompletionResults.add(prefix + item);
                    }
                }
            }

            // remove items in tabCompletionResults that do not contain the current arg text
            Iterator<String> iterator = tabCompletionResults.iterator();

            while (iterator.hasNext()) {
                String item = iterator.next();
                if (!item.contains(arg)) {
                    iterator.remove();
                }
            }
        }

        return tabCompletionResults;
    }
}
