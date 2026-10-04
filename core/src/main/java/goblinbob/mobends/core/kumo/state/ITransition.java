package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;


/** What moves a layer: a connection, or a selector branch that leads somewhere else. */
public interface ITransition
{

    /** The node or machine it leads to. */
    MachineMember getTarget();

    /** The crossfade, in ticks (0: none). */
    float getDuration();

    ConnectionTemplate.Easing getEasing();

    /** The statements it runs when it moves the layer (its {@code do}). */
    StatementList getRun();

}
