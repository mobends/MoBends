package goblinbob.mobends.core;

import goblinbob.mobends.core.mutation.PuppeteerException;

public interface IPuppeteer<C>
{
    void perform(C context) throws PuppeteerException;

    void beforeRender(C context);
}
