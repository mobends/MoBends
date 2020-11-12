package mobends.core.kumo;

import goblinbob.mobends.core.kumo.Animator;
import goblinbob.mobends.core.serial.SerialQueue;
import org.junit.Test;
import static org.junit.Assert.*;

public class KumoAnimatorSerializationTest
{
    @Test
    public void animator()
    {
        Animator animator = new Animator();

        SerialQueue buffer = new SerialQueue(1024);
        animator.serialize(buffer);
        Animator deserializedAnimator = Animator.deserialize(buffer);

        assertEquals(animator, deserializedAnimator);
    }
}
