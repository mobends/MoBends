package goblinbob.mobends.test.core.pack;

import goblinbob.mobends.core.pack.PackSignature;
import goblinbob.mobends.core.pack.PackSigningKeys;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class PackSignatureTest
{

    private static final String PLAYER = "mobends:bends/animators/player.json";
    private static final String ZOMBIE = "mobends:bends/animators/zombie.json";

    private static KeyPair keyPair() throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(PackSignature.KEY_ALGORITHM);
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    private static Map<String, byte[]> files()
    {
        Map<String, byte[]> files = new TreeMap<>();
        files.put(PLAYER, "{\"formatVersion\": 2}".getBytes(StandardCharsets.UTF_8));
        files.put(ZOMBIE, "{\"formatVersion\": 2, \"layers\": []}".getBytes(StandardCharsets.UTF_8));
        return files;
    }

    private static PackSignature reread(PackSignature signature) throws IOException
    {
        return PackSignature.read(new StringReader(signature.toJson().toString()));
    }

    private static PackSignature withFiles(PackSignature signature, String key, String hash) throws IOException
    {
        com.google.gson.JsonObject json = signature.toJson();
        json.getAsJsonObject("files").addProperty(key, hash);
        return PackSignature.read(new StringReader(json.toString()));
    }

    @Test
    public void aSignatureVerifiesWithItsKeyAndCoversItsFiles() throws Exception
    {
        KeyPair pair = keyPair();
        PackSignature signature = reread(PackSignature.sign(files(), pair.getPrivate()));

        assertTrue(signature.verify(Collections.singletonList(pair.getPublic())));
        assertTrue(signature.covers(PLAYER, files().get(PLAYER)));
        assertTrue(signature.covers(ZOMBIE, files().get(ZOMBIE)));
    }

    @Test
    public void aSignatureDoesNotVerifyWithAnotherKey() throws Exception
    {
        PackSignature signature = PackSignature.sign(files(), keyPair().getPrivate());

        assertFalse(signature.verify(Collections.singletonList(keyPair().getPublic())));
        assertFalse(signature.verify(Collections.<PublicKey>emptyList()));
    }

    @Test
    public void changedBytesAreNotCovered() throws Exception
    {
        PackSignature signature = PackSignature.sign(files(), keyPair().getPrivate());

        assertFalse(signature.covers(PLAYER, "{\"formatVersion\": 2, \"layers\": [{}]}".getBytes(StandardCharsets.UTF_8)));
        assertFalse(signature.covers("mobends:bends/animators/biped.json", files().get(PLAYER)));
    }

    @Test
    public void aFileAddedToTheListBreaksTheSignature() throws Exception
    {
        KeyPair pair = keyPair();
        PackSignature signature = PackSignature.sign(files(), pair.getPrivate());
        byte[] evil = "{\"formatVersion\": 2, \"layers\": [{\"evil\": true}]}".getBytes(StandardCharsets.UTF_8);

        PackSignature tampered = withFiles(signature, "mobends:bends/animators/biped.json", PackSignature.sha256(evil));
        assertFalse(tampered.verify(Collections.singletonList(pair.getPublic())));

        PackSignature rehashed = withFiles(signature, PLAYER, PackSignature.sha256(evil));
        assertFalse(rehashed.verify(Collections.singletonList(pair.getPublic())));
    }

    @Test
    public void keysWithWhitespaceAreRefused() throws Exception
    {
        PackSignature signature = PackSignature.sign(files(), keyPair().getPrivate());
        try
        {
            withFiles(signature, "mobends:a\n0000  mobends:b", PackSignature.sha256(new byte[0]));
            fail("a key with a line break was read");
        }
        catch (IOException expected)
        {
        }
    }

    @Test(expected = IOException.class)
    public void anotherFormatVersionIsRefused() throws Exception
    {
        com.google.gson.JsonObject json = PackSignature.sign(files(), keyPair().getPrivate()).toJson();
        json.addProperty("formatVersion", 2);
        PackSignature.read(new StringReader(json.toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aSignatureDoesNotSignItself() throws Exception
    {
        Map<String, byte[]> files = files();
        files.put(PackSignature.LOCATION, new byte[0]);
        PackSignature.sign(files, keyPair().getPrivate());
    }

    @Test
    public void theShippedKeysAreWellFormed()
    {
        List<PublicKey> keys = PackSigningKeys.trusted();
        assertFalse(keys.isEmpty());
    }

}
