package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.Core;

public class CommonProxy
{

    public void preInit()
    {
        // No behaviour.
    }

    public void init()
    {
        // No behaviour.
    }

    public void postInit()
    {
        // No behaviour.
    }

    public void createCore()
    {
        Core.createAsServer();
    }

}
