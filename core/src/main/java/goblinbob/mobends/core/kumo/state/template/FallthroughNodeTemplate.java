package goblinbob.mobends.core.kumo.state.template;

/**
 * {@code "type": "core:fallthrough"}: a node that poses nothing, so the layers below show
 * through. A transition into or out of it fades between the layer's pose and theirs. It keeps
 * the rest of a node: tags, connections, {@code set}, {@code expressions}.
 */
public class FallthroughNodeTemplate extends NodeTemplate
{

    public void validate() throws MalformedKumoTemplateException
    {
        requirePosesNothing("fallthrough");
    }

}
