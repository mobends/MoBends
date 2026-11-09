package net.minecraft.client.model.geom;

import java.util.Map;
import java.util.NoSuchElementException;

/** A model's part: here only its children by name, the tree model definitions name parts in. */
public final class ModelPart {
    private final Map<String, ModelPart> children;

    public ModelPart(Map<String, ModelPart> children) {
        this.children = children;
    }

    public boolean hasChild(String name) {
        return children.containsKey(name);
    }

    public ModelPart getChild(String name) {
        ModelPart part = children.get(name);
        if (part == null) {
            throw new NoSuchElementException("Can't find part " + name);
        }
        return part;
    }
}
