package net.gobbob.mobends.core.pack;

import java.util.HashMap;
import java.util.Map;

public class BendsTarget
{

    private String name;
    private Map<String, IActivationCondition> conditions = new HashMap<>();

    public BendsTarget(String name)
    {
        this.name = name;
    }

}
