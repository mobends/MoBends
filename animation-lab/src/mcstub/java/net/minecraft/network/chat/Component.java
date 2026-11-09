package net.minecraft.network.chat;

/** Text: here only plain text. */
public interface Component {
    String getString();

    static Component literal(String text) {
        return () -> text;
    }
}
