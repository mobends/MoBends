package net.minecraft.client.multiplayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** The client's level (see {@link Level}), with the entities added to it. */
public class ClientLevel extends Level {
    private final List<Entity> entities = new ArrayList<>();

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    @Nullable
    @Override
    public Entity getEntity(int id) {
        for (Entity entity : entities) {
            if (entity.getId() == id) {
                return entity;
            }
        }
        return null;
    }
}
