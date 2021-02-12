package goblinbob.mobends.core;

import goblinbob.mobends.core.exceptions.InvalidMutationException;

public interface IPuppeteerRepository<C>
{
    IPuppeteer<C> getOrCreatePuppeteer(C context, EntityBender<C> bender) throws InvalidMutationException;
}
