package goblinbob.mobends.core.types;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A loaded extension (see {@link ExtensionDefinition}). Extensions of one type are ordered like
 * types, by rank (the user's) then id, with no specificity; the first one goes on top.
 */
public class Extension implements TypeOrder.Ranked
{

    private final String id;
    private final String source;
    private final String typeId;
    private final ResourceLocation animator;
    private int rank;

    public Extension(String id, String source, String typeId, ResourceLocation animator)
    {
        this.id = id;
        this.source = source;
        this.typeId = typeId;
        this.animator = animator;
    }

    @Override
    public String getId()
    {
        return id;
    }

    /** Where the extension came from: the pack and the file. */
    public String getSource()
    {
        return source;
    }

    /** The id of the type it extends. */
    public String getTypeId()
    {
        return typeId;
    }

    public ResourceLocation getAnimator()
    {
        return animator;
    }

    @Override
    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }

    @Override
    public int getSpecificity()
    {
        return 0;
    }

    /**
     * The animators of {@code extensions} in the order their layers are added: the last one ends
     * up on top, so it's the first one in precedence order.
     */
    public static List<ResourceLocation> layerOrder(Collection<Extension> extensions)
    {
        List<Extension> sorted = new ArrayList<>(extensions);
        sorted.sort(TypeOrder.PRECEDENCE);
        Collections.reverse(sorted);
        List<ResourceLocation> animators = new ArrayList<>();
        for (Extension extension : sorted)
        {
            animators.add(extension.animator);
        }
        return animators;
    }

}
