package com.stemcraft.language;

public enum Phrase {
    ACTION_NOT_SUPPORTED,
    HEAD;

    public String getPhrase() {
        return Language.getPhrase(this);
    }

    public static String build(Phrase phrase, String... params) {
        String output = phrase.getPhrase();
        
        return output;
    }
}
