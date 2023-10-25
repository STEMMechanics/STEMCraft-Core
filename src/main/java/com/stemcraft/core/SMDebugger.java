package com.stemcraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.config.SMConfig;

public final class SMDebugger {
    
    /**
     * Store messsages to be printed out at once when requested.
     */
    private static final Map<String, ArrayList<String>> pendingMessages = new HashMap<>();

    public static boolean isDebugging(String section) {
        return SMConfig.main().getBoolean("debug." + section, false);
    }
    
    /**
     * Prints debug messages to the console
     * 
     * @param section
     * @param messages
     */
    public static void debug(Object section, String... messages) {
        String sectionName;
        if (section == null) {
            sectionName = "unknown";
        } else if (section instanceof String) {
            sectionName = (String) section;
        } else if (section instanceof Class) {
            sectionName = ((Class<?>) section).getSimpleName();
        } else {
            sectionName = section.getClass().getSimpleName();
        }
        
        if(isDebugging(sectionName)) {
            for(final String message : messages) {
                STEMCraft.info("[" + section + "] " + message);
            }
        }
    }

    /**
     * Put a message for a specific section into the queue until push is called.
     * @param section
     * @param message
     */
    public static void put(String section, String message) {
        if(!isDebugging(section)) {
            return;
        }

        final ArrayList<String> messages = pendingMessages.getOrDefault(section, new ArrayList<>());
        messages.add(message);

        pendingMessages.put(section, messages);
    }

    /**
	 * Logs the error in the console and writes all details into the errors.log file
	 *
	 * @param t
	 * @param messages
	 */
	public static void printError(Throwable t, String... messages) {

		if (Bukkit.getServer() == null) {
			return;
        }

		try {
			final List<String> lines = new ArrayList<>();
            
            lines.add(STEMCraft.getNamed() + " " + STEMCraft.getVersion() + " encountered a series error");
            if(messages != null) {
                lines.addAll(Arrays.asList(messages));
            }

            // Get stack trace
            lines.addAll(getStackTrace(t));

			// Log to the console
			STEMCraft.severe(String.join("\n", lines));

		} catch (final Throwable secondError) {
			STEMCraft.severe("Got error when saving another error!");
		}
	}

    public static List<String> getStackTrace(Throwable t) {
        List<String> lines = new ArrayList<>();

        do {
            // Write the error header
            SMCommon.append(lines, t == null ? "Unknown error" : t.getClass().getSimpleName() + " " + SMCommon.getOrDefault(t.getMessage(), SMCommon.getOrDefault(t.getLocalizedMessage(), "(Unknown cause)")));

            if(t == null) {
                break;
            }

            int count = 0;

            for (final StackTraceElement el : t.getStackTrace()) {
                count++;

                final String trace = el.toString();

                if (trace.contains("sun.reflect"))
                    continue;

                if (count > 6 && trace.startsWith("net.minecraft.server"))
                    break;

                SMCommon.append(lines, "\t at " + el.toString());
            }
        } while ((t = t.getCause()) != null);

        return lines;
    }
}
