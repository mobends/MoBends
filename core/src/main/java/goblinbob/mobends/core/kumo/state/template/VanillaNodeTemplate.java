package goblinbob.mobends.core.kumo.state.template;

/**
 * {@code {"core:vanilla": {...}}}: while a layer is in this node, the entity is drawn with its vanilla
 * model and vanilla animation, as if Mo' Bends didn't animate it. The animator keeps running
 * underneath (its layer still decides its node every frame, its other layers keep their clocks), so
 * leaving the node brings the animated model back where it would have been. The switch is
 * immediate either way: the two models can't be blended. It keeps the rest of a node: tags,
 * connections, {@code set}, {@code expressions}.
 */
public class VanillaNodeTemplate extends NodeTemplate
{

    public void validate() throws MalformedKumoTemplateException
    {
        requirePosesNothing("vanilla");
    }

}
