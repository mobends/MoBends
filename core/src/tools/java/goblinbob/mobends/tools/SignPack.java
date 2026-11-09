package goblinbob.mobends.tools;

import com.google.gson.GsonBuilder;
import goblinbob.mobends.core.pack.PackSignature;
import goblinbob.mobends.core.pack.PackSigningKeys;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zips a resource pack folder and signs its animation files (everything under
 * {@code assets/<namespace>/bends/}), adding the signature as {@code assets/mobends/bends/signature.json}
 * (see PackSignature). The private key is read from the MOBENDS_PACK_SIGNING_KEY environment
 * variable (PKCS#8, base64; the CI secret), or from the file given with {@code --key-file}. The
 * signature is checked against the keys the mod ships before the pack is written, so a pack signed
 * with a key the mod doesn't know fails here, not in the game.
 *
 * <p>The zip's entries are sorted and carry no times, so two builds of the same pack differ only
 * in the signature (ECDSA signs with a random nonce).
 *
 * <p>Run with {@code gradle legacyPack} (optionally {@code -Ppack_signing_key=<file>}).
 */
public final class SignPack
{

    private static final String SIGNATURE_ENTRY = "assets/mobends/bends/signature.json";

    public static void main(String[] args) throws Exception
    {
        Path keyFile = null;
        int first = 0;
        if (args.length > 1 && args[0].equals("--key-file"))
        {
            keyFile = Paths.get(args[1]);
            first = 2;
        }
        if (args.length - first != 2)
        {
            System.err.println("usage: SignPack [--key-file <private-key-file>] <pack-folder> <out.zip>");
            System.exit(2);
        }
        Path pack = Paths.get(args[first]).toAbsolutePath();
        Path out = Paths.get(args[first + 1]).toAbsolutePath();

        String encodedKey = keyFile != null ? new String(Files.readAllBytes(keyFile), StandardCharsets.US_ASCII) : System.getenv("MOBENDS_PACK_SIGNING_KEY");
        if (encodedKey == null || encodedKey.trim().isEmpty())
        {
            System.err.println("No signing key: set MOBENDS_PACK_SIGNING_KEY or pass -Ppack_signing_key=<file>.");
            System.exit(1);
        }
        PrivateKey key = PackSignature.privateKey(encodedKey);

        Map<String, byte[]> entries = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(pack))
        {
            for (Path file : walk.filter(Files::isRegularFile).sorted().collect(Collectors.toList()))
            {
                String name = pack.relativize(file).toString().replace('\\', '/');
                if (name.equals(SIGNATURE_ENTRY) || name.endsWith(".DS_Store")) continue;
                entries.put(name, Files.readAllBytes(file));
            }
        }
        if (!entries.containsKey("pack.mcmeta"))
        {
            throw new IOException(pack + " has no pack.mcmeta");
        }

        Map<String, byte[]> signed = new TreeMap<>();
        for (Map.Entry<String, byte[]> entry : entries.entrySet())
        {
            String[] path = entry.getKey().split("/", 3); // assets, namespace, the rest
            if (path.length == 3 && path[0].equals("assets") && path[2].startsWith("bends/"))
            {
                signed.put(path[1] + ":" + path[2], entry.getValue());
            }
        }
        if (signed.isEmpty())
        {
            throw new IOException(pack + " has nothing under assets/<namespace>/bends/ to sign");
        }

        PackSignature signature = PackSignature.sign(signed, key);
        List<PublicKey> trusted = PackSigningKeys.trusted();
        if (!signature.verify(trusted))
        {
            throw new IllegalStateException("The signing key isn't one PackSigningKeys trusts, so the mod would refuse the pack.");
        }
        entries.put(SIGNATURE_ENTRY, (new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(signature.toJson()) + "\n").getBytes(StandardCharsets.UTF_8));

        Files.createDirectories(out.getParent());
        try (OutputStream file = Files.newOutputStream(out); ZipOutputStream zip = new ZipOutputStream(file))
        {
            for (Map.Entry<String, byte[]> entry : entries.entrySet())
            {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setTime(0);
                zip.putNextEntry(zipEntry);
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        System.out.println("Signed " + signed.size() + " files: " + out);
    }

}
