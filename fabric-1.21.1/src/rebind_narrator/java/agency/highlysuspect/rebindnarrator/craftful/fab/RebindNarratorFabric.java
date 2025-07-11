package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import agency.highlysuspect.quatlib.craftless.config.hdc.HalfDecentConfigFile;
import agency.highlysuspect.rebindnarrator.craftful.RebindNarratorMc;
import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

import java.util.List;

public class RebindNarratorFabric extends RebindNarratorMc implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		super.initConfig();
		
		//pick an impl
		if(config.get(AMECS_SUPPORT)) {
			List<String> idsToTry = List.of("amecs-reborn", "amecsapi");
			for(String id : idsToTry) {
				if(FabricLoader.getInstance().isModLoaded(id)) {
					LOG.info("AMECS support is enabled and mod '{}' is present, enabling support.", id);
					backend = new AmecsRebornBackend();
					break;
				}
			}
			
			if(backend == null) {
				LOG.info("AMECS support is enabled but none of '{}' are present, using regular impl.", String.join("', '", idsToTry));
				backend = new BasicFabricBackend();
			}
		} else {
			LOG.info("AMECS support is disabled, using regular impl.");
			backend = new BasicFabricBackend();
		}
		
		super.init(); //sets up the key predicate
		
		backend.registerKeybinding();
	}
	
	public interface Backend {
		void registerKeybinding();
		NarratorKeyPredicate makeKeyPredicate();
	}
	
	public Backend backend;
	
	public static final ConfigOpt<Boolean> CTRL = new ConfigOpt.BoolOpt("Require CTRL", true,
		"Does the Control key need to be held to activate the narrator keybind?",
		"(Does nothing if AMECS support is enabled and AMECS is loaded - it adds its",
		"own, better way to bind key modifiers through the controls screen.)"
	);
	
	public static final ConfigOpt<Boolean> AMECS_SUPPORT = new ConfigOpt.BoolOpt("AMECS support", true,
		"Provide support for key modifiers through the 'Amecs Reborn' mod?",
		"Does nothing if AMECS isn't loaded. Requires a game restart to turn on/off."
	);
	
	@Override
	public ConfigSection visitConfigSchema(ConfigSection root) {
		root = super.visitConfigSchema(root);
		root.add(CTRL);
		root.subsection("Compat").add(AMECS_SUPPORT);
		return root;
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return HalfDecentConfigFile.make(
			failures.context(),
			schema,
			FabricLoader.getInstance().getConfigDir().resolve("rebind_narrator.txt"),
			LOG,
			Util.ioPool()
		);
	}
	
	@Override
	public NarratorKeyPredicate makeKeyPredicate() {
		return backend.makeKeyPredicate();
	}
}
