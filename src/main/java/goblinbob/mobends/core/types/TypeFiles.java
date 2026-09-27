package goblinbob.mobends.core.types;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.definition.DefinedBenders;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import goblinbob.mobends.core.types.selector.SelectorConditionRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/** Reads type and extension files (found by {@link TypeFileDiscovery}) into types and extensions. */
final class TypeFiles
{

    private TypeFiles()
    {
    }

    /** The type a type file describes, its model (a model definition's bender is registered) resolved. */
    static EntityType loadType(TypeFileDiscovery.TypeFile file, @Nullable CoreClientConfig config, ResourcePackPolicy policy) throws Exception
    {
        EntityTypeDefinition definition = EntityTypeDefinition.parse(file.json);
        ISelectorCondition selector = definition.selector == null ? null : SelectorConditionRegistry.INSTANCE.parse(definition.selector);

        EntityBender<?> model = null;
        boolean vanilla = false;
        if (EntityTypeDefinition.VANILLA_MODEL.equals(definition.model))
        {
            vanilla = true;
        }
        else if (definition.isModelDefinition() && !file.trusted && policy != ResourcePackPolicy.ALLOW)
        {
            // Custom geometry from a resource pack: the entity keeps its default model.
            Core.LOG.warning("The server limits resource packs' animation: the type " + file.source + " keeps the entity's default model instead of " + definition.model);
        }
        else if (definition.isModelDefinition())
        {
            ResourceLocation location = new ResourceLocation(definition.model);
            model = DefinedBenders.createBender(location.getResourceDomain(), location, ModelDefinitions.INSTANCE.load(location));
            EntityBender<?> existing = EntityBenderRegistry.instance.getByKey(model.getKey());
            if (existing != null)
            {
                // Another type (or an addon) already made the bender with this key.
                model = existing;
            }
            else
            {
                EntityBenderRegistry.instance.registerTypeBender(model, config);
            }
        }
        else if (definition.model != null)
        {
            model = EntityBenderRegistry.instance.getByKey(definition.model);
            if (model == null)
            {
                throw new MalformedKumoTemplateException("There is no model '" + definition.model + "'.");
            }
        }

        ResourceLocation animator = definition.animator == null ? null : new ResourceLocation(definition.animator);
        return new EntityType(definition.id, file.source, selector, definition.specificity(), model, vanilla, animator, false);
    }

    /** The extension an extension file describes, with its rank. */
    static Extension loadExtension(TypeFileDiscovery.TypeFile file, @Nullable CoreClientConfig config) throws MalformedKumoTemplateException
    {
        ExtensionDefinition definition = ExtensionDefinition.parse(file.json);
        Extension extension = new Extension(definition.id, file.source, definition.type, new ResourceLocation(definition.animator));
        if (config != null)
        {
            extension.setRank(config.getExtensionRank(extension.getId()));
        }
        return extension;
    }

}
