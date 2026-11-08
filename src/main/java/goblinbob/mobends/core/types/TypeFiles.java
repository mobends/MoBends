package goblinbob.mobends.core.types;

import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import goblinbob.mobends.core.types.selector.SelectorExpression;
import net.minecraft.resources.ResourceLocation;

/**
 * Reads type and extension files (found by {@link TypeFileDiscovery}) into types and extensions.
 * Every pack is trusted for now: there's no pack signing, nor any server policy, on this version yet.
 */
final class TypeFiles {
    private TypeFiles() {
    }

    /** The type a type file describes, its model (a model definition's defined entity) resolved. */
    static EntityType loadType(TypeFileDiscovery.TypeFile file) throws Exception {
        EntityTypeDefinition definition = EntityTypeDefinition.parse(file.json());
        ISelectorCondition selector = definition.selector == null ? null : SelectorExpression.compile(definition.selector);

        DefinedEntity model = null;
        boolean vanilla = false;
        if (EntityTypeDefinition.VANILLA_MODEL.equals(definition.model)) {
            vanilla = true;
        } else if (definition.isModelDefinition()) {
            model = DefinedEntities.forTypeFile(ResourceLocation.parse(definition.model));
        } else if (definition.model != null) {
            model = DefinedEntities.byKey(definition.model);
            if (model == null) {
                throw new MalformedKumoTemplateException("There is no model '" + definition.model + "'.");
            }
        }

        ResourceLocation animator = definition.animator == null ? null : ResourceLocation.parse(definition.animator);
        OptionSet options = KumoOptions.INSTANCE.declare(definition.id, definition.options);
        return new EntityType(definition.id, file.source(), selector, definition.specificity(), model, vanilla, animator, options);
    }

    /** The extension an extension file describes. */
    static Extension loadExtension(TypeFileDiscovery.TypeFile file) throws MalformedKumoTemplateException {
        ExtensionDefinition definition = ExtensionDefinition.parse(file.json());
        return new Extension(definition.id, file.source(), definition.type, ResourceLocation.parse(definition.animator),
                KumoOptions.INSTANCE.declare(definition.id, definition.options));
    }
}
