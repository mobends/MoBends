package net.minecraft.world.phys.shapes;

/** Who collides (the game shapes some blocks by it). The lab's blocks are the same to everyone. */
public interface CollisionContext {
    CollisionContext EMPTY = new CollisionContext() {
    };

    static CollisionContext empty() {
        return EMPTY;
    }
}
