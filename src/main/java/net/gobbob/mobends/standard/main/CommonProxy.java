package net.gobbob.mobends.standard.main;

import net.gobbob.mobends.core.Core;

public class CommonProxy
{

    public void preInit() {}

    public void init() {}

    public void postInit() {}

    public void createCore()
    {
        Core.createAsServer();
    }

}
