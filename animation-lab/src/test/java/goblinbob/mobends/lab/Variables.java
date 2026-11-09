package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.IKumoSubject;

/** Reads a subject's variables by name, as an animator does. */
final class Variables
{
    private Variables()
    {
    }

    static double of(IKumoSubject subject, String name)
    {
        int index = subject.indexOfVariable(name);
        if (index < 0)
        {
            throw new IllegalArgumentException("The subject has no variable '" + name + "'.");
        }
        return subject.getVariable(index);
    }
}
