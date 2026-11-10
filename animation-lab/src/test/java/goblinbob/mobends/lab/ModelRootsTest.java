package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.model.ModelRoots;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The root of a model that doesn't keep it ({@link ModelRoots}): the recorded tree whose parts the
 * model holds in its fields, whatever they're named.
 */
class ModelRootsTest
{
    private final ModelPart head = new ModelPart(Map.of());
    private final ModelPart body = new ModelPart(Map.of("head", head));
    private final ModelPart leg = new ModelPart(Map.of());
    private final ModelPart root = new ModelPart(Map.of("body", body, "leg", leg));

    private final ModelPart otherPart = new ModelPart(Map.of());
    private final ModelPart otherRoot = new ModelPart(Map.of("other", otherPart));

    /** A model as a mod (or the llama's) keeps its parts: fields of its own, private, of any name. */
    static class OwnDrawModel
    {
        private final ModelPart a;
        private final ModelPart[] b;

        OwnDrawModel(ModelPart a, ModelPart... b)
        {
            this.a = a;
            this.b = b;
        }
    }

    static class SubModel extends OwnDrawModel
    {
        final List<ModelPart> more;

        SubModel(ModelPart a, List<ModelPart> more)
        {
            super(a);
            this.more = more;
        }
    }

    @Test
    void theTreeTheModelHoldsPartsOf()
    {
        ModelRoots.record(root);
        assertSame(root, ModelRoots.find(new OwnDrawModel(head, leg)));
        assertSame(root, ModelRoots.rootOfPart(head));
        assertSame(root, ModelRoots.rootOfPart(root));
    }

    @Test
    void fieldsOfSuperclassesAndCollectionsCount()
    {
        ModelRoots.record(root);
        assertSame(root, ModelRoots.find(new SubModel(null, List.of(body))));
    }

    @Test
    void theTreeMostPartsComeFrom()
    {
        ModelRoots.record(root);
        ModelRoots.record(otherRoot);
        assertSame(root, ModelRoots.find(new OwnDrawModel(otherPart, head, body, leg)));
        assertSame(otherRoot, ModelRoots.find(new OwnDrawModel(otherPart)));
    }

    @Test
    void theRootOutlivesItsLastReference()
    {
        // As a model keeps only the parts it poses, nothing holds the root but its parts.
        ModelPart part = new ModelPart(Map.of());
        ModelRoots.record(new ModelPart(Map.of("part", part)));
        System.gc();
        assertNotNull(ModelRoots.find(new OwnDrawModel(part)));
    }

    @Test
    void noneWithoutARecordedTree()
    {
        ModelPart loose = new ModelPart(Map.of());
        assertNull(ModelRoots.find(new OwnDrawModel(loose)));
        assertNull(ModelRoots.find(new Object()));
    }
}
