package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;

public class Animator implements ISerializable
{
    private int bruh;
    private float some;

    @Override
    public void serialize(ISerialOutput serialOutput)
    {
        serialOutput.writeInt(bruh);
        serialOutput.writeFloat(some);
    }

    public static Animator deserialize(ISerialInput serialInput)
    {
        Animator animator = new Animator();

        animator.bruh = serialInput.readInt();
        animator.some = serialInput.readFloat();

        return animator;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Animator))
            return false;

        Animator other = (Animator) obj;
        return other.bruh == this.bruh && other.some == this.some;
    }
}
