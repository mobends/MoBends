package net.gobbob.mobends.core.pack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

public class PackManager
{
	public static File localDirectory;
	public static File cacheDirectory;
	public static File databaseCacheFile;
	public static URL publicDirectoryURL;
	public static BPDFile cachedDatabase;
	public static BPDFile publicDatabase;
	private static HashMap<String, BendsPack> localPacks = new HashMap<String, BendsPack>();
	private static HashMap<String, BendsPack> publicPacks = new HashMap<String, BendsPack>();
	public static final int NOT_LOADED = 0;
	public static final int LOADED = 1;
	public static final int ERROR = 2;
	private static int publicPackLoadState;
	private static BendsPack currentPack;

	public static void initialize(Configuration config)
	{
		localDirectory = new File(Minecraft.getMinecraft().mcDataDir, "bendspacks");
		localDirectory.mkdir();
		cacheDirectory = new File(localDirectory, "public_cache");
		cacheDirectory.mkdir();

		publicDatabase = BPDFile.parse(downloadPublicDatabase());
		parsePublicDatabase(publicDatabase);
		databaseCacheFile = new File(cacheDirectory, "database");
		if (databaseCacheFile.exists())
		{
			cachedDatabase = BPDFile.parse(GUtil.readLines(databaseCacheFile));
		}
		else
		{
			try
			{
				cachedDatabase = new BPDFile();
				databaseCacheFile.createNewFile();
				cachedDatabase.saveToFile(databaseCacheFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			initPacks();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String savedPackName = config.get("General", "Current Pack", "none").getString();
		if (localPacks.containsKey(savedPackName))
			PackManager.choose(localPacks.get(savedPackName));
		else if (publicPacks.containsKey(savedPackName))
			PackManager.choose(publicPacks.get(savedPackName));
	}

	public static void initPacks() throws IOException
	{
		File[] files = localDirectory.listFiles();
		localPacks.clear();
		for (File file : files)
		{
			if (file.getAbsolutePath().endsWith(".bends"))
			{
				BendsPack pack = new BendsPack();
				pack.readBasicInfo(file);
				if (pack.getFilename() != null && pack.getDisplayName() != null)
					localPacks.put(pack.getFilename(), pack);
			}
		}
	}

	public static boolean addLocal(BendsPack newPack)
	{
		if (!PackManager.localPacks.containsKey(newPack.getFilename()))
		{
			PackManager.localPacks.put(newPack.getFilename(), newPack);
			return true;
		}
		return false;
	}

	public static boolean addPublic(BendsPack newPack)
	{
		if (!PackManager.publicPacks.containsKey(newPack.getFilename()))
		{
			PackManager.publicPacks.put(newPack.getFilename(), newPack);
			return true;
		}
		return false;
	}

	public static void choose(BendsPack newPack)
	{
		currentPack = newPack;
		if (currentPack != null)
			currentPack.apply();
		else
			BendsPack.targets.clear();
	}

	public static String[] downloadPublicDatabase()
	{
		publicPacks.clear();
		try
		{
			publicDirectoryURL = new URL("https://www.dropbox.com/s/s6pra0y76aslcyg/packDatabase.bpd?dl=1");
			BufferedReader in = new BufferedReader(new InputStreamReader(publicDirectoryURL.openStream()));

			List<String> lines = new ArrayList<String>();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				lines.add(inputLine);
			in.close();

			return lines.toArray(new String[0]);
		}
		catch (MalformedURLException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void parsePublicDatabase(BPDFile parser)
	{
		publicPackLoadState = NOT_LOADED;
		if (parser == null || parser.getPackEntries() == null)
		{
			publicPackLoadState = ERROR;
		}
		else
		{
			for (BPDFile.Entry entry : parser.getPackEntries().values())
			{
				addPublic(new BendsPack(entry.get("name"), entry.get("displayName"), entry.get("author"),
						entry.get("description")).setThumbnailURL(entry.get("thumbnail"))
								.setDownloadURL(entry.get("downloadLink")));
			}
			publicPackLoadState = LOADED;
		}
	}

	public static void updatePublicDatabase()
	{
		BPDFile parser = BPDFile.parse(downloadPublicDatabase());
		parsePublicDatabase(parser);
	}

	public static void renamePack(String originalName, String name)
	{
		if (!localPacks.containsKey(originalName))
			return;
		BendsPack pack = localPacks.get(originalName);

		if (pack.getFilename().equalsIgnoreCase(name) || pack.isPublic())
			return;

		pack.rename(name);

		File packFile = new File(PackManager.localDirectory, originalName);
		if (packFile.exists())
		{
			// Renaming the file.
			File newPackFile = new File(PackManager.localDirectory, name);
			try
			{
				final BufferedWriter os = new BufferedWriter(new FileWriter(newPackFile));
				os.write(GUtil.readFile(packFile));
				os.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		localPacks.remove(originalName);
		localPacks.put(name, pack);
	}

	public static BendsPack getCurrentPack()
	{
		return currentPack;
	}

	public static boolean isCurrentPackPublic()
	{
		return getCurrentPack() != null && getCurrentPack().isPublic();
	}

	public static boolean isCurrentPackLocal()
	{
		return getCurrentPack() != null && !getCurrentPack().isPublic();
	}

	public static boolean arePublicPacksLoaded()
	{
		return publicPackLoadState == LOADED;
	}

	public static int getPublicPackLoadState()
	{
		return publicPackLoadState;
	}

	public static BendsPack getPublic(String name)
	{
		return publicPacks.get(name);
	}

	public static BendsPack getLocal(String name)
	{
		return localPacks.get(name);
	}

	public static Collection<BendsPack> getPublicPacks()
	{
		return publicPacks.values();
	}

	public static Collection<BendsPack> getLocalPacks()
	{
		return localPacks.values();
	}
}
