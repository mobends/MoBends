package goblinbob.mobends.core.pack;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The public keys of Mo' Bends' own resource packs: a pack whose {@link PackSignature} one of these
 * verifies is trusted as the mod is. The private keys are held by the Mo' Bends team (the
 * MOBENDS_PACK_SIGNING_KEY secret in CI), never in the repository.
 *
 * <p>To replace a key (it leaked, or was lost), add the new one, sign the packs with it, and remove
 * the old one; mods released with only the old key keep accepting packs signed by it.
 */
public final class PackSigningKeys
{

    /** X.509 (SubjectPublicKeyInfo), base64; EC over P-256. */
    private static final String[] KEYS = {
            // 2026-11: the first key
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEGOXbWQ454Ll32QylVF30qiymH9r8GIfnRxHk8Ji1QS6bSiqzCVX8DpxkfBsGAg5Hl0BoHTJU+LwL7/ywmR1GTg==",
    };

    private static List<PublicKey> trusted;

    private PackSigningKeys()
    {
    }

    public static synchronized List<PublicKey> trusted()
    {
        if (trusted == null)
        {
            List<PublicKey> keys = new ArrayList<>();
            for (String key : KEYS)
            {
                try
                {
                    keys.add(PackSignature.publicKey(key));
                }
                catch (GeneralSecurityException | IllegalArgumentException e)
                {
                    throw new IllegalStateException("A pack signing key is malformed: " + key, e);
                }
            }
            trusted = Collections.unmodifiableList(keys);
        }
        return trusted;
    }

}
