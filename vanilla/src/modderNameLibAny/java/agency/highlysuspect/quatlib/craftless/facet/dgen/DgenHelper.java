package agency.highlysuspect.quatlib.craftless.facet.dgen;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.FileGenner;
import agency.highlysuspect.quatlib.craftless.util.PhysicalVersion;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DgenHelper implements FileGenner {
	public DgenHelper(String modid) {
		this.modid = modid;
		this.propNameBase = "quatlib.dgen." + modid;
	}
	
	public final String modid;
	private final String propNameBase;
	
	@Override
	public void writeFile(@Nullable PhysicalVersion ver, String subpath, String toWrite) {
		String propName = ver == null ? propNameBase : propNameBase + "." + ver;
		String prop = System.getProperty(propName);
		if(prop == null) throw new IllegalArgumentException("Can't find system property " + propName);
		
		Path path = Paths.get(prop).resolve(subpath);
		
		//QuatlibBase.LOG.info("Would write to path: {}", path);
		//if(true) return;
		
		try {
			QuatlibBase.LOG.info("Writing to file {}", path);
			if(Files.notExists(path)) {
				Files.createDirectories(path.getParent());
				Files.writeString(path, toWrite, StandardCharsets.UTF_8);
			} else if(Files.exists(path)) {
				//only write if it changed
				if(!Files.readString(path).equals(toWrite)) Files.writeString(path, toWrite, StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to write file to " + path, e);
		}
	}
}
