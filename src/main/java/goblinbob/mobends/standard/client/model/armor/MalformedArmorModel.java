package goblinbob.mobends.standard.client.model.armor;

/**
 * To be thrown when an armor model doesn't comply with the standard:
 * @see https://github.com/mobends/MoBends/wiki/Armor-Mutation
 */
public class MalformedArmorModel extends RuntimeException
{
    public MalformedArmorModel(String message)
    {
        super(message);
    }
}
