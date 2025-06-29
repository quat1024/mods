package agency.highlysuspect.quatlib.any.config;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConfigSection implements SectOrOpt {
	public ConfigSection(String name, List<String> comment) {
		this.name = name;
		this.comment = comment;
	}
	
	public ConfigSection(String name, String... comment) {
		this(name, Arrays.asList(comment));
	}
	
	private final String name;
	private final List<String> comment;
	
	private final List<SectOrOpt> children = new ArrayList<>(4);
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public List<String> getComment() {
		return comment;
	}
	
	public @Nullable SectOrOpt getByName(String name) {
		//todo, use a map?
		for(SectOrOpt child : children) if(child.getName().equals(name)) return child;
		return null;
	}
	
	public @Nullable ConfigSection getSectionByName(String name) {
		return getByName(name) instanceof ConfigSection section ? section : null;
	}
	
	public @Nullable ConfigOpt<?> getOptByName(String name) {
		return getByName(name) instanceof ConfigOpt<?> opt ? opt : null;
	}
	
	public Collection<SectOrOpt> getChildren() {
		return children;
	}
	
	//conveniences
	public void add(SectOrOpt... opts) {
		children.addAll(Arrays.asList(opts));
	}
	
	public ConfigSection subsection(String name, String... comment) {
		ConfigSection subsection = getSectionByName(name);
		if(subsection == null) {
			subsection = new ConfigSection(name, comment);
			add(subsection);
		}
		return subsection;
	}
}
