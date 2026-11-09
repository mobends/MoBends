package net.minecraft.client.resources;

/** A player's skin; the lab's only says which model it's drawn with. */
public record PlayerSkin(Model model) {
    public enum Model {
        SLIM("slim"),
        WIDE("default");

        private final String id;

        Model(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}
