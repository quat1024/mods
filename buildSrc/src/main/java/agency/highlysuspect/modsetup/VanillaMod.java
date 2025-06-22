package agency.highlysuspect.modsetup;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Named;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//it's a vanilla mod. it makes sense if you don't think about it
public class VanillaMod implements Named {
	public VanillaMod(String modid) {
		this.modid = modid;
		
		vars.put("modid", modid);
		vars.put("name", StringGroovyMethods.capitalize(modid));
	}
	
	String modid;
	Set<String> versions = new LinkedHashSet<>();
	Map<String, Object> vars = new HashMap<>();
	boolean quatlib = true;
	
	@Nullable String simpleRunMainClass;
	
	@Override
	public String getName() {
		return modid;
	}
}
