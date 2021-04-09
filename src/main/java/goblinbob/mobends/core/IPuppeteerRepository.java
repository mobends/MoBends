package goblinbob.mobends.core;

import goblinbob.mobends.core.exceptions.InvalidMutationException;

public interface IPuppeteerRepository<C, R extends IBenderResources>
{
    IPuppeteer<C> getOrCreatePuppeteer(C context, EntityBender<C, R> bender) throws InvalidMutationException;

    void disposePuppeteer(C context, EntityBender<C, R> bender);
}
