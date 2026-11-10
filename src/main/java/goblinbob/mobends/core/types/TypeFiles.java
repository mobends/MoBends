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
     * The type a type file describes, its model (a model definition's defined entity) resolved; null
     * if the policy leaves it out.
     */
    @Nullable
    static EntityType loadType(TypeFileDiscovery.TypeFile file, ResourcePackPolicy policy) throws Exception {
        EntityTypeDefinition definition = EntityTypeDefinition.parse(file.json());
        if (definition.isModelDefinition() && !file.trusted() && policy != ResourcePackPolicy.ALLOW) {
            // Custom geometry from a resource pack: the whole type is left out, so it can't hide a
            // trusted type with the same id, and its animator never runs on a model it wasn't made for.
            MoBends.LOGGER.warn("Mo' Bends: the server limits resource packs' animation: ignoring the type {}, which brings its own model ({})",
                    file.source(), definition.model);
            return null;
        }
        ISelectorCondition selector = definition.selector == null ? null : SelectorExpression.compile(definition.selector);

        DefinedEntity model = null;
        boolean vanilla = false;
        if (EntityTypeDefinition.VANILLA_MODEL.equals(definition.model)) {
            vanilla = true;
        } else if (definition.isModelDefinition()) {
            model = DefinedEntities.forTypeFile(Ids.parse(definition.model));
        } else if (definition.model != null) {
            model = DefinedEntities.byKey(definition.model);
            if (model == null) {
                throw new MalformedKumoTemplateException("There is no model '" + definition.model + "'.");
            }
        }

        ResourceLocation animator = definition.animator == null ? null : Ids.parse(definition.animator);
        OptionSet options = KumoOptions.INSTANCE.declare(definition.id, definition.options);
        return new EntityType(definition.id, file.source(), selector, definition.specificity(), model, vanilla, animator, options);
    }

    /** The extension an extension file describes. */
    static Extension loadExtension(TypeFileDiscovery.TypeFile file) throws MalformedKumoTemplateException {
        ExtensionDefinition definition = ExtensionDefinition.parse(file.json());
        return new Extension(definition.id, file.source(), definition.type, Ids.parse(definition.animator),
                KumoOptions.INSTANCE.declare(definition.id, definition.options));
    }
}
