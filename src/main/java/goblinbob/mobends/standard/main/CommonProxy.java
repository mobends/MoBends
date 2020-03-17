package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.Core;

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
