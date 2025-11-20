package dev.ghidora.utabridge_server.enums;

public enum OAuthProvider {
    GOOGLE("google"),
    DISCORD("discord");

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OAuthProvider fromString(String text) throws IllegalArgumentException {
        for (var provider : OAuthProvider.values()) {
            if (text.equals(provider.getValue())) {
                return provider;
            }
        }

        throw new IllegalArgumentException("Unknown provider: " + text);
    }
}
