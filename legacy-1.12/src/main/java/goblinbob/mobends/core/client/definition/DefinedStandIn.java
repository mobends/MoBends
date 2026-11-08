package goblinbob.mobends.core.client.definition;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * What a definition leaves in a vanilla model's field when the bone renders inside another
 * (a parent, or the bone it lies over): it draws nothing, but vanilla code that goes through the
 * field still reaches the bone. Its visibility ({@code showModel}, {@code isHidden}: a player's
 * skin-part toggles) is the bone's, and {@code postRender} (a held item, a hat) is the bone's.
 * While the first-person hand is drawn (vanilla draws the arm's field alone), it draws the bone.
 */
public class DefinedStandIn extends ModelRenderer
{

    private final DefinedModelPart bone;

    public DefinedStandIn(ModelBase model, DefinedModelPart bone)
    {
        super(model);
        // A ModelRenderer adds itself to the model's box list; a stand-in is no part of the model.
        model.boxList.remove(this);
        this.bone = bone;
        bone.setVisibleWith(this);
    }

    /** Whether it draws the bone itself (the first-person hand), the bone's parent not being drawn. */
    private boolean drawsAlone;

    public void setDrawsAlone(boolean drawsAlone)
    {
        this.drawsAlone = drawsAlone;
    }

    @Override
    public void render(float scale)
    {
        if (drawsAlone)
        {
            // With its parents' transforms, and its own children (its segments, a sleeve).
            bone.renderPart(scale);
        }
    }

    @Override
    public void renderWithRotation(float scale)
    {
    }

    @Override
    public void postRender(float scale)
    {
        bone.postRender(scale);
    }

}
