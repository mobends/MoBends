package goblinbob.mobends.core.pack.variable;

import net.minecraft.client.resources.I18n;

public class BendsVariableEntry
{

    private IBendsVariable variable;
    private String key;

    public BendsVariableEntry(IBendsVariable variable, String key)
    {
        this.variable = variable;
        this.key = key;
    }

    public String getLocalizedName()
    {
        return I18n.format(String.format("mobends.variable.%s", this.key));
    }

}
