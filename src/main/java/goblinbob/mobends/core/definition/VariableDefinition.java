package goblinbob.mobends.core.definition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.List;

/**
 * An animator variable read from the entity: a numeric field (interpolated from a previous-tick
 * field when one is given), or the product of two other variables.
 */
public class VariableDefinition
{

    public String name;

    /** Candidate field names (deobfuscated and SRG), the first that exists is used. */
    public List<String> field;
    /** Optional previous-tick field for interpolation by partial ticks. */
    public List<String> prevField;

    /** Instead of a field: the product of these variables. */
    public List<String> product;

    public float scale = 1;
    public float offset = 0;
    /** Optional function applied after scale and offset: sin, cos, mcsin, mccos, abs. */
    public String fn;
    /** Added after the function. */
    public float add = 0;

    public void validate() throws MalformedKumoTemplateException
    {
        if (name == null) throw new MalformedKumoTemplateException("A variable needs a 'name'.");
        if ((field == null || field.isEmpty()) && (product == null || product.size() < 2))
            throw new MalformedKumoTemplateException("Variable '" + name + "' needs a 'field' or a 'product' of two variables.");
    }

}
