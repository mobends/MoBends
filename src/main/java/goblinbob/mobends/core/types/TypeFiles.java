package goblinbob.mobends.core.types;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.definition.DefinedBenders;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import goblinbob.mobends.core.types.selector.SelectorExpression;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/** Reads type and extension files (found by {@link TypeFileDiscovery}) into types and extensions. */
final class TypeFiles
{

    private TypeFiles()
    {
    }

    /**
     * A type file read, with the model definition it names by path made into its bender and
     * registered (so its key is known before any type names it by key); the rest is resolved by
     * {@link #build}.
     */
    static final class Read
    {
        final TypeFileDiscovery.TypeFile file;
        final EntityTypeDefinition definition;
        @Nullable
        final EntityBender<?> model;

        Read(TypeFileDiscovery.TypeFile file, EntityTypeDefinition definition, @Nullable EntityBender<?> model)
        {
            this.file = file;
            this.definition = definition;
            this.model = model;
        }
    }

    /**
     * Reads a type file, and makes and registers the bender of the model definition it names by
     * path; null if the policy leaves it out.
     */
    @Nullable
    static Read read(TypeFileDiscovery.TypeFile file, @Nullable CoreClientConfig config, ResourcePackPolicy policy) throws Exception
    {
        EntityTypeDefinition definition = EntityTypeDefinition.parse(file.json);
        if (definition.isModelDefinition() && !file.trusted && policy != ResourcePackPolicy.ALLOW)
        {
            // Custom geometry from a resource pack: the whole type is left out, so it can't hide a
            // trusted type with the same id, and its animator never runs on a model it wasn't made for.
            Core.LOG.warning("The server limits resource packs' animation: ignoring the type " + file.source + ", which brings its own model (" + definition.model + ")");
            return null;
        }
        EntityBender<?> model = null;
        if (definition.isModelDefinition())
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
                model.setResourcePack(file.resourcePack);
            }
        }
        return new Read(file, definition, model);
    }

    /**
     * The type a type file describes, once every type file has been read: a model it names by key
     * is any type file's (or an addon's), whichever pack it comes from and in whatever order they
     * were read.
     */
    static EntityType build(Read read) throws Exception
    {
        EntityTypeDefinition definition = read.definition;
        EntityBender<?> model = read.model;
        boolean vanilla = false;
        if (EntityTypeDefinition.VANILLA_MODEL.equals(definition.model))
        {
            vanilla = true;
        }
        else if (model == null)
        {
            model = EntityBenderRegistry.instance.getByKey(definition.model);
            if (model == null)
            {
                throw new MalformedKumoTemplateException("There is no model '" + definition.model + "'.");
            }
        }

        ISelectorCondition selector = definition.selector == null ? null : SelectorExpression.compile(definition.selector);
        ResourceLocation animator = definition.animator == null ? null : new ResourceLocation(definition.animator);
        OptionSet options = KumoOptions.INSTANCE.declare(definition.id, definition.options);
        return new EntityType(definition.id, read.file.source, selector, definition.specificity(), model, vanilla, animator, options);
    }

    /** The extension an extension file describes, with its rank. */
    static Extension loadExtension(TypeFileDiscovery.TypeFile file, @Nullable CoreClientConfig config) throws MalformedKumoTemplateException
    {
        ExtensionDefinition definition = ExtensionDefinition.parse(file.json);
        Extension extension = new Extension(definition.id, file.source, definition.type, new ResourceLocation(definition.animator),
                KumoOptions.INSTANCE.declare(definition.id, definition.options));
        if (config != null)
        {
            extension.setRank(config.getExtensionRank(extension.getId()));
        }
        return extension;
    }

}
