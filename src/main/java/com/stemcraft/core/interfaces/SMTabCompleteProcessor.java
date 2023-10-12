package com.stemcraft.core.interfaces;

import java.util.List;
import com.stemcraft.core.tabcomplete.SMTabCompleteContext;

@FunctionalInterface
public interface SMTabCompleteProcessor {
    List<String> process(SMTabCompleteContext context);
}
