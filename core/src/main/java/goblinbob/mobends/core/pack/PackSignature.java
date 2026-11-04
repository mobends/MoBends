package goblinbob.mobends.core.pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.util.FormatVersion;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The signature of a resource pack Mo' Bends makes itself (the Legacy Mo' Bends pack): the SHA-256
 * of each of its files, signed with a key only the Mo' Bends team holds. A file of an enabled
 * resource pack is untrusted, unless the pack's signature verifies with one of the keys the mod
 * ships ({@link PackSigningKeys}) and lists that file with the hash of exactly the bytes it has;
 * then it is trusted as the mod's own files are. Nothing is fetched: the check needs only the pack
 * and the mod.
 *
 * <p>The pack carries it as {@code assets/mobends/bends/signature.json} ({@link #LOCATION}):
 * <pre>{"formatVersion": 1,
 *  "files": {"mobends:bends/animators/player.json": "&lt;sha-256, hex&gt;", ...},
 *  "signature": "&lt;SHA256withECDSA over the files, base64&gt;"}</pre>
 * What is signed is {@link #signedContent}: a header, then a line per file, sorted by key.
 */
public final class PackSignature
{

    public static final int FORMAT_VERSION = 1;
    /** Where a pack carries its signature, as a resource key. */
    public static final String LOCATION = "mobends:bends/signature.json";
    /** ECDSA over P-256, which every Java 8 runtime has (Ed25519 came in Java 15). */
    public static final String ALGORITHM = "SHA256withECDSA";
    public static final String KEY_ALGORITHM = "EC";

    /** Names what the signature is of, so it can't be taken for a signature of anything else. */
    private static final String HEADER = "mobends-pack-signature " + FORMAT_VERSION + "\n";
    private static final Pattern KEY = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final Pattern SHA_256 = Pattern.compile("[0-9a-f]{64}");

    /** Resource key ({@code namespace:path}) to the SHA-256 of its bytes, in lower-case hex. */
    private final Map<String, String> files;
    private final byte[] signature;

    private PackSignature(Map<String, String> files, byte[] signature)
    {
        this.files = Collections.unmodifiableMap(new TreeMap<>(files));
        this.signature = signature.clone();
    }

    /** Signs {@code files} (resource key to its bytes). */
    public static PackSignature sign(Map<String, byte[]> files, PrivateKey key) throws GeneralSecurityException
    {
        Map<String, String> hashes = new TreeMap<>();
        for (Map.Entry<String, byte[]> file : files.entrySet())
        {
            checkKey(file.getKey());
            if (file.getKey().equals(LOCATION))
            {
                throw new IllegalArgumentException("A signature can't sign itself (" + LOCATION + ")");
            }
            hashes.put(file.getKey(), sha256(file.getValue()));
        }
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(key);
        signer.update(signedContent(hashes));
        return new PackSignature(hashes, signer.sign());
    }

    /**
     * Reads a signature file. Says nothing of whether it verifies.
     *
     * @throws IOException when it isn't one
     */
    public static PackSignature read(Reader reader) throws IOException
    {
        try
        {
            JsonObject json = FormatVersion.check(new JsonParser().parse(reader), "pack signature", FORMAT_VERSION);
            JsonElement filesJson = json.get("files");
            JsonElement signatureJson = json.get("signature");
            if (filesJson == null || !filesJson.isJsonObject() || signatureJson == null || !signatureJson.isJsonPrimitive())
            {
                throw new JsonParseException("A pack signature needs \"files\" (an object) and \"signature\" (a string).");
            }
            Map<String, String> files = new TreeMap<>();
            for (Map.Entry<String, JsonElement> file : filesJson.getAsJsonObject().entrySet())
            {
                checkKey(file.getKey());
                String hash = file.getValue().getAsString();
                if (!SHA_256.matcher(hash).matches())
                {
                    throw new JsonParseException("The hash of " + file.getKey() + " has to be a SHA-256 in lower-case hex.");
                }
                files.put(file.getKey(), hash);
            }
            return new PackSignature(files, Base64.getDecoder().decode(signatureJson.getAsString()));
        }
        catch (RuntimeException e) // JsonParseException, IllegalStateException, IllegalArgumentException (base64)
        {
            throw new IOException("Not a pack signature: " + e.getMessage(), e);
        }
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("formatVersion", FORMAT_VERSION);
        JsonObject filesJson = new JsonObject();
        for (Map.Entry<String, String> file : files.entrySet())
        {
            filesJson.addProperty(file.getKey(), file.getValue());
        }
        json.add("files", filesJson);
        json.addProperty("signature", Base64.getEncoder().encodeToString(signature));
        return json;
    }

    /** The files it signs, by resource key. */
    public Collection<String> files()
    {
        return files.keySet();
    }

    /** Whether one of {@code keys} made this signature. */
    public boolean verify(Collection<PublicKey> keys)
    {
        byte[] content = signedContent(files);
        for (PublicKey key : keys)
        {
            try
            {
                Signature verifier = Signature.getInstance(ALGORITHM);
                verifier.initVerify(key);
                verifier.update(content);
                if (verifier.verify(signature)) return true;
            }
            catch (GeneralSecurityException e)
            {
                // a malformed signature, or a key of another kind: not made by this key
            }
        }
        return false;
    }

    /**
     * Whether the signature lists {@code key} with exactly these bytes. Only means anything once
     * {@link #verify} has said the signature is genuine.
     */
    public boolean covers(String key, byte[] content)
    {
        String hash = files.get(key);
        return hash != null && MessageDigest.isEqual(hash.getBytes(StandardCharsets.US_ASCII), sha256(content).getBytes(StandardCharsets.US_ASCII));
    }

    /** What is signed: the header, then {@code <sha-256>  <key>\n} for each file, sorted by key. */
    static byte[] signedContent(Map<String, String> files)
    {
        StringBuilder content = new StringBuilder(HEADER);
        for (Map.Entry<String, String> file : new TreeMap<>(files).entrySet())
        {
            content.append(file.getValue()).append("  ").append(file.getKey()).append('\n');
        }
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String sha256(byte[] content)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest)
            {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Every Java runtime has SHA-256", e);
        }
    }

    /** A public key, as {@link PackSigningKeys} holds them: X.509 (SubjectPublicKeyInfo), base64. */
    public static PublicKey publicKey(String base64) throws GeneralSecurityException
    {
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64.trim())));
    }

    /** A private key, as the build signs with it: PKCS#8, base64. */
    public static PrivateKey privateKey(String base64) throws GeneralSecurityException
    {
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64.trim())));
    }

    /** Keys can't hold whitespace, so no two sets of files have the same {@link #signedContent}. */
    private static void checkKey(String key)
    {
        if (!KEY.matcher(key).matches())
        {
            throw new JsonParseException("\"" + key + "\" isn't a resource key (namespace:path, in lower case).");
        }
    }

}
