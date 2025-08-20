package agency.highlysuspect.stairdown.craftless;

import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.sn.SnCodec;

import java.util.ArrayList;
import java.util.List;

public class StairdownOpts {
	public static final ConfigOpt<List<Variant>> VARIANTS = new ConfigOpt<>("variants", new ArrayList<>(), SnCodec.listOf(Variant.SN_CODEC));
	
	public static ConfigSection visit(ConfigSection root) {
		root.subsection("Specification", "Which blocks to register into the game").add(VARIANTS);
		return root;
	}
}
