package goblinbob.mobends.core.client.definition;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * What a definition leaves in a vanilla model's field when the bone renders inside another
 * (a parent, or the bone it lies over): it draws nothing, but vanilla code that goes through the
 * field still reaches the bone. Its visibility ({@code showModel}, {@code isHidden}: a player's
 * skin-part toggles) is the bone's, and {@code postRender} (a held item, a hat) is the bone's.
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

    @Override
    public void render(float scale)
    {
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
