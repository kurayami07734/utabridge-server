package dev.ghidora.utabridge_server.enums;

public enum IdentityProvider {
    GOOGLE("google"),
    DISCORD("discord");

    private final String value;

    IdentityProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IdentityProvider fromString(String text) throws IllegalArgumentException {
        for (var provider : IdentityProvider.values()) {
            if (text.equals(provider.getValue())) {
                return provider;
            }
        }

        throw new IllegalArgumentException("Unknown provider: " + text);
    }
}
