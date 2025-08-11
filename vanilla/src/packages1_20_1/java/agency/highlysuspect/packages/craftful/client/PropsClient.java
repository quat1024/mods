package agency.highlysuspect.packages.craftful.client;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;

public class PropsClient {
	//TODO: leverage the sn system to parse these now, instead of doing it in PackagesBaseClient#onConfigReload
	public static final ConfigOpt<String> INSERT_ONE_BINDING_UNPARSED = new ConfigOpt.StringOpt("insertOne", "use", "How do you insert one item into the package?");
	public static final ConfigOpt<String> INSERT_STACK_BINDING_UNPARSED = new ConfigOpt.StringOpt("insertStack", "sneak-use", "How do you insert one stack of items into the package?");
	public static final ConfigOpt<String> INSERT_ALL_BINDING_UNPARSED = new ConfigOpt.StringOpt("insertAll", "ctrl-use", "How do you insert everything in your inventory that fits into the package?");
	public static final ConfigOpt<String> TAKE_ONE_BINDING_UNPARSED = new ConfigOpt.StringOpt("takeOne", "punch", "How do you take one item from the package?");
	public static final ConfigOpt<String> TAKE_STACK_BINDING_UNPARSED = new ConfigOpt.StringOpt("takeStack", "sneak-punch", "How do you take one stack of items from the package?");
	public static final ConfigOpt<String> TAKE_ALL_BINDING_UNPARSED = new ConfigOpt.StringOpt("takeAll", "ctrl-punch", "How do you clear all items from the package?");
	
	public static final ConfigOpt<Integer> PUNCH_REPEAT = new ConfigOpt.IntOpt(
		"punchRepeat", -1,
		"Older versions of Packages had a \"feature\" where holding left-click would slowly trickle items out of the Package.",
		"This was actually a bug, caused by code intended to differentiatiate between 'starting a left click' and 'continuing",
		"a left click' not working correctly; the delay was my band-aid fix.",
		"I've since actually fixed it, but I don't know if people had gotten used to the broken behavior :)",
		"If you did and want it back, set this to 4."
	).setMin(-1);
	
	public static final ConfigOpt<Boolean> RED_BAR_WHEN_FULL = new ConfigOpt.BoolOpt(
		"redBarWhenFull", true,
		"Packages display a \"durability\" bar corresponding to their fill level. If 'false', the bar is always blue,",
		"and if 'true', the bar will turn red when the package is 100% filled. I think the red color looks nice,",
		"but if you don't care for the discrepancy with the vanilla Bundle item (which is always blue), feel free to turn it off."
	);
	
	public static final ConfigOpt<Boolean> CACHE_MESHES = new ConfigOpt.BoolOpt(
		"cacheMeshes", true,
		"If 'true', Package and Package Crafter 3d models will be cached in-memory, instead of rebaked from scratch every time.",
		"This probably helps chunk-bake performance less than it sounds like it would - Packages is pretty fast.",
		"It also consumes more memory. But I think it makes item rendering more efficient?",
		"Everything's a tradeoff. (Pressing F3+T will dump the caches, either way.)"
	);
	
	public static final ConfigOpt<Integer> LIGHTING_CORRECTION = new ConfigOpt.IntOpt(
		"lightingCorrection", 2,
		"When rendering a package block, what method should I use to determine how much light falls on the block?",
		"0: The light level inside the package. (Might make items turn black.)",
		"1: The light level of the block in front of the package.",
		"2: The light level inside the package if it is nonzero, otherwise the light level in front.",
		"   ^ Has best results on Create contraptions."
	).setMin(0).setMax(2);
	
	public static final ConfigOpt<Boolean> FORGE_SWAP_RED_AND_BLUE = new ConfigOpt.BoolOpt(
		"swapRedAndBlue", true,
		"Hi Forge players! For some reason, I need this to make the front face of Packages render with the right color",
		"on your modloader. If you need to reset this to 'false', I'd be interested in hearing what mods you're using."
	);
	
//	public static final ConfigProperty<Boolean> FABRIC_FREX_SUPPORT = ConfigProperty.boolOpt(
//		"frexSupport", true,
//		"If 'true' and FREX is loaded, FREX materials will be forwarded through into Packages's custom block models.",
//		"Use this if you have funky Canvas shaders. Requires a game restart to activate and deactivate."
//	);
	
	public static ConfigSection visit(ConfigSection schema) {
		schema.subsection("Keys",
			"How do you want to interact with packages in the world?",
			"Specify at least 'use' (right click) or 'punch' (left click). Optionally add",
			"any combination of 'ctrl', 'alt', or 'sneak' to require some modifier keys.",
			"Separate multiple items with hyphens. Disable an action entirely by leaving it blank."
		).add(INSERT_ONE_BINDING_UNPARSED, INSERT_STACK_BINDING_UNPARSED, INSERT_ALL_BINDING_UNPARSED,
			TAKE_ONE_BINDING_UNPARSED, TAKE_STACK_BINDING_UNPARSED, TAKE_ALL_BINDING_UNPARSED,
			PUNCH_REPEAT);
		
		schema.subsection("Pedantry").add(RED_BAR_WHEN_FULL);
		
		schema.subsection("Model").add(CACHE_MESHES, LIGHTING_CORRECTION);
		if(Packages.inst().isForge()) {
			schema.getSectionByName("Model").add(FORGE_SWAP_RED_AND_BLUE);
		}
		
		return schema;
	}
}
