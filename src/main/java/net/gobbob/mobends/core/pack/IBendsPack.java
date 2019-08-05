package net.gobbob.mobends.core.pack;

import net.minecraft.util.ResourceLocation;

public interface IBendsPack
{

    String getKey();

    String getDisplayName();

    String getAuthor();

    String getDescription();

    ResourceLocation getThumbnail();

    boolean canPackBeEdited();

}
