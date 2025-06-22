package agency.highlysuspect.modsetup;

import org.gradle.api.Named;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

//it's a vanilla mod. it makes sense if you don't think about it
public class VanillaMod implements Named {
	public VanillaMod(String modid) {
		this.modid = modid;
	}
	
	String modid;
	Set<String> versions = new LinkedHashSet<>();
	
	@Nullable String simpleRunMainClass;
	
	@Override
	public String getName() {
		return modid;
	}
}
