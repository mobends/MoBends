package goblinbob.mobends.standard.selector;

import com.google.gson.JsonObject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import goblinbob.mobends.core.types.selector.SelectorConditionRegistry;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;

/**
 * {@code {"type": "mobends:skin_variant", "variant": "slim"}}: a player whose skin has this arm
 * width ({@code default} or {@code slim}). Players read as {@code default} until their skin has
 * downloaded, so the answer can change.
 */
public class SkinVariantCondition implements ISelectorCondition
{

    private final String variant;

    private SkinVariantCondition(String variant)
    {
        this.variant = variant;
    }

    public static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
    {
        String variant = json.has("variant") ? json.get("variant").getAsString() : null;
        if (!"default".equals(variant) && !"slim".equals(variant))
        {
            throw new MalformedKumoTemplateException("A skin variant is 'default' or 'slim', not '" + variant + "'.");
        }
        return new SkinVariantCondition(variant);
    }

    @Override
    public boolean test(EntityLivingBase entity)
    {
        return entity instanceof AbstractClientPlayer && variant.equals(((AbstractClientPlayer) entity).getSkinType());
    }

    @Override
    public boolean isStable()
    {
        return false;
    }

}
