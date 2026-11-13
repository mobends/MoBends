package goblinbob.mobends.core.types;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import goblinbob.mobends.core.types.selector.SelectorExpression;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Reads type and extension files (found by {@link TypeFileDiscovery}) into types and extensions, as
 * far as the server's policy allows a resource pack's (see {@code AnimationPolicy}).
 */
final class TypeFiles {
    private TypeFiles() {
    }

    /**
     * A type file read, with the model definition it names by path made into its defined entity
     * (so its key is known before any type names it by key); the rest is resolved by {@link #build}.
     */
    record Read(TypeFileDiscovery.TypeFile file, EntityTypeDefinition definition, @Nullable DefinedEntity model) {
    }

    /**
     * Reads a type file and makes the model definition it names by path; null if the policy or its
     * {@code @when} leaves it out.
     */
    @Nullable
    static Read read(TypeFileDiscovery.TypeFile file, ResourcePackPolicy policy) throws Exception {
        EntityTypeDefinition definition = EntityTypeDefinition.parse(file.json());
        if (!definition.applies()) {
            // Its @when doesn't hold on this version: as if the file weren't there, its model not even read.
            MoBends.LOGGER.debug("Mo' Bends: leaving out the type {}: its @when doesn't hold", file.source());
            return null;
        }
        if (definition.isModelDefinition() && !file.trusted() && policy != ResourcePackPolicy.ALLOW) {
            // Custom geometry from a resource pack: the whole type is left out, so it can't hide a
            // trusted type with the same id, and its animator never runs on a model it wasn't made for.
            MoBends.LOGGER.warn("Mo' Bends: the server limits resource packs' animation: ignoring the type {}, which brings its own model ({})",
                    file.source(), definition.model);
            return null;
        }
        DefinedEntity model = definition.isModelDefinition() ? DefinedEntities.forTypeFile(Ids.parse(definition.model)) : null;
        return new Read(file, definition, model);
    }

    /**
     * The type a type file describes, once every type file has been read: a model it names by key
     * is any type file's, whichever pack it comes from and in whatever order they were read.
     */
    static EntityType build(Read read) throws Exception {
        EntityTypeDefinition definition = read.definition();
        DefinedEntity model = read.model();
        boolean vanilla = false;
        if (EntityTypeDefinition.VANILLA_MODEL.equals(definition.model)) {
            vanilla = true;
        } else if (model == null) {
            model = DefinedEntities.byKey(definition.model);
            if (model == null) {
                throw new MalformedKumoTemplateException("There is no model '" + definition.model + "'.");
            }
        }

        ISelectorCondition selector = definition.selector == null ? null : SelectorExpression.compile(definition.selector);
        ResourceLocation animator = definition.animator == null ? null : Ids.parse(definition.animator);
        OptionSet options = KumoOptions.INSTANCE.declare(definition.id, definition.options);
        return new EntityType(definition.id, read.file().source(), selector, definition.specificity(), model, vanilla, animator, options);
    }

    /** The extension an extension file describes; null if its {@code @when} leaves it out. */
    @Nullable
    static Extension loadExtension(TypeFileDiscovery.TypeFile file) throws MalformedKumoTemplateException {
        ExtensionDefinition definition = ExtensionDefinition.parse(file.json());
        if (!definition.applies()) {
            MoBends.LOGGER.debug("Mo' Bends: leaving out the extension {}: its @when doesn't hold", file.source());
            return null;
        }
        return new Extension(definition.id, file.source(), definition.type, Ids.parse(definition.animator),
                KumoOptions.INSTANCE.declare(definition.id, definition.options));
    }
}
