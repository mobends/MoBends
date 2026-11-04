package goblinbob.mobends.tools;

import goblinbob.mobends.core.pack.PackSignature;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * Makes a new key pair for signing Mo' Bends' own resource packs (see PackSignature). The private
 * key (PKCS#8, base64) is written to the given file, readable only by its owner, and never to the
 * repository; it is what {@code gradle legacyPack} signs with (the MOBENDS_PACK_SIGNING_KEY secret
 * in CI). The public key is printed, for PackSigningKeys.
 *
 * <p>Run with {@code gradle generatePackSigningKey -Ppack_signing_key=<file>}. Refuses to overwrite
 * a key: a lost key can't sign packs the released mods accept.
 */
public final class GenPackSigningKey
{

    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.println("usage: GenPackSigningKey <private-key-file>");
            System.exit(2);
        }
        Path file = Paths.get(args[0]).toAbsolutePath();
        if (Files.exists(file))
        {
            System.err.println(file + " already exists; not overwriting a signing key.");
            System.exit(1);
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance(PackSignature.KEY_ALGORITHM);
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair pair = generator.generateKeyPair();

        Files.createDirectories(file.getParent());
        Files.createFile(file, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        Files.write(file, (Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()) + "\n").getBytes(StandardCharsets.US_ASCII));

        System.out.println("Private key written to " + file + " (keep it secret, and back it up).");
        System.out.println("Public key, for PackSigningKeys:");
        System.out.println(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
    }

}
