package agency.highlysuspect.quatlib.craftless.util;

import agency.highlysuspect.quatlib.craftless.failure.CtxChain;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Season1CrummyConfigUpgrader {
	public Season1CrummyConfigUpgrader(LogFacade log, CtxChain ctx) {
		this.log = log;
		this.ctx = ctx;
	}
	
	private final LogFacade log;
	private final CtxChain ctx;
	
	public Season1CrummyConfigUpgrader configureForPackages() {
		//TODO: If this class needs configuration for more legacy config formats, tune those settings here
		return this;
	}
	
	public void upgrade(Path from, Path to) {
		try {
			doUpgrade(from, to);
		} catch (Exception e) {
			throw ctx.cause(e).detail("Failed to upgrade old config file").uncheckedReportError();
		}
	}
	
	protected final Pattern OOPS_ALL_HASHES = Pattern.compile("^#*$");
	protected final Pattern HASHHASH_STUFF_HASHHASH = Pattern.compile("^##(.*)##$");
	protected final Pattern KEY_COLON_VALUE = Pattern.compile("^(.*):(.*)$");
	
	protected void doUpgrade(Path from, Path to) throws Exception {
		if(Files.notExists(from)) return; //nothing to do
		log.info("Upgrading config file '{}' to the Season 2 format, putting the result in '{}'", from.getFileName(), to.getFileName());
		
		List<String> oldLines = Files.readAllLines(from, StandardCharsets.UTF_8);
		List<String> newLines = new ArrayList<>();
		
		boolean wroteAnyHeader = false;
		
		for(String line : oldLines) {
			//part of the section header ascii art
			if(OOPS_ALL_HASHES.matcher(line).matches())
				continue;
			
			//section header
			Matcher hhshhM = HASHHASH_STUFF_HASHHASH.matcher(line);
			if(hhshhM.matches()) {
				if(wroteAnyHeader) newLines.add("}");
				String sectionName = hhshhM.group(1);
				newLines.add(sectionName.trim() + " {");
				wroteAnyHeader = true;
				continue;
			}
			
			//comment
			if(line.trim().startsWith("#"))
				continue;
			
			//key-colon-value
			Matcher kcvM = KEY_COLON_VALUE.matcher(line);
			if(kcvM.matches()) {
				newLines.add(kcvM.group(1) + " = " + kcvM.group(2));
				continue;
			}
			
			//otherwise pass it along (probably whitespace, new line, something
			newLines.add(line);
		}
		
		if(wroteAnyHeader) newLines.add("}");
		
		Files.write(to, newLines, StandardCharsets.UTF_8);
		
		//TODO(season2): debugging spew
		//Files.write(to.resolveSibling(to.getFileName().toString() + ".upgraded"), newLines, StandardCharsets.UTF_8);
		Files.deleteIfExists(from);
		log.info("Upgraded config successfully.");
	}
}
