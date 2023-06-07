package com.stemcraft.language;

import java.util.concurrent.ConcurrentHashMap;

public class Language {
    private static ConcurrentHashMap<Phrase, String> phrases = new ConcurrentHashMap<>();

    protected static String getPhrase(Phrase phrase) {
        return phrases.get(phrase);
    }
}
