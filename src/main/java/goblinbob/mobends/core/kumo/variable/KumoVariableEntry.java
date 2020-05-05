package goblinbob.mobends.core.kumo.variable;

import net.minecraft.client.resources.I18n;

public class KumoVariableEntry
{

    private IKumoVariable variable;
    private String key;

    public KumoVariableEntry(IKumoVariable variable, String key)
    {
        this.variable = variable;
        this.key = key;
    }

    public String getLocalizedName()
    {
        return I18n.format(String.format("mobends.variable.%s", this.key));
    }

}
