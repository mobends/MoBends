package net.gobbob.mobends.pack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BPDFile {
	private HashMap<String, Entry> entries;
	
	public BPDFile() {
		this.entries = new HashMap<String, Entry>();
	}
	
	public BPDFile(String[] lines) {
		this();
		this.parseLines(lines);
	}
	
	public void parseLines(String[] lines) {
		Entry entry = null;
		for(String line : lines) {
			if(line.startsWith("#")) continue;
			if(line.startsWith("PACK")) {
				if(entry != null) entries.put(entry.get("name"), entry);
				entry = new Entry();
			}else if(entry != null) {
				String[] s = new String[] {
					line.substring(0, line.indexOf("=")),
					line.substring(line.indexOf("=")+1)
				};
				entry.set(s[0].trim(), s[1].substring(0, s[1].length()-2).replaceAll("\"", "").trim());
			}
		}
		if(entry != null) entries.put(entry.get("name"), entry);
	}
	
	public void saveToFile(File file) {
		BufferedWriter os;
		try {
			os = new BufferedWriter(new FileWriter(file));
			os.write("# Bends Pack Database (.bpd) file\n");
			for(Entry entry : entries.values()) {
				for(java.util.Map.Entry<String, String> property : entry.properties.entrySet())
				os.write("\t"+property.getKey()+"="+"\""+property.getValue()+"\"");
				os.newLine();
			}
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Entry addEntry(String name) {
		Entry entry = new Entry();
		entry.set("name", name);
		entries.put(name, entry);
		return entry;
	}
	
	public Entry getEntry(String name) {
		return entries.get(name);
	}
	
	public HashMap<String, Entry> getPackEntries() {
		return entries;
	}
	
	public static class Entry {
		HashMap<String, String> properties;
		
		public Entry() {
			properties = new HashMap<String, String>();
		}

		public void set(String key, String value) {
			properties.put(key, value);
		}
		
		public String get(String key) {
			return properties.containsKey(key) ? properties.get(key) : null;
		}
	}

	public static BPDFile parse(String[] lines) {
		if(lines == null)
			return null;
		
		return new BPDFile(lines);
	}
}
