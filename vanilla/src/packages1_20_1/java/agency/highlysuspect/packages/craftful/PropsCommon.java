package agency.highlysuspect.packages.craftful;

import agency.highlysuspect.quatlib.craftless.config.ConfigOpt;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;

public class PropsCommon {
	public static final ConfigOpt<Boolean> INVENTORY_INTERACTIONS = new ConfigOpt.BoolOpt(
		"inventoryInteractions", true,
		"Allow interacting with Packages in the player inventory, kinda like a Bundle."
	);
	
	public static final ConfigOpt<Boolean> INTERACTION_SOUNDS = new ConfigOpt.BoolOpt(
		"interactionSounds", true,
		"Play sounds when players interact with a Package."
	);
	
	public static final ConfigOpt<Boolean> PACKAGE_MAKER_ALLOW_LIST_MODE = new ConfigOpt.BoolOpt(
		"packageMakerAllowlistMode", false,
		"If 'true', the item tags `packages:allowlist_package_maker_frame` and `packages:allowlist_package_maker_inner`",
		"will be read, and only the items specified in those tags will be allowed to enter the Frame and Core slots of",
		"the Package Maker. This is mainly for modpackers to mess with; you should also probably add something to the tooltip",
		"lang keys to describe the behavior you create. See this very mod's en_us.json for some documentation on this."
	);
	
	public static final ConfigOpt<Boolean> DROP_EMPTY_PACKAGES_IN_CREATIVE = new ConfigOpt.BoolOpt(
		"dropEmptyPackagesInCreative", true,
		"In Creative mode, when you break a Package, it will always drop as an item on the floor - even if it's empty.",
		"This is different from vanilla Shulker Boxes, which only drop when nonempty. I think it's nice to always",
		"get the item, but if you don't care for the discrepancy with vanilla Shulker Boxes, feel free to turn it off."
	);
	
	public static ConfigSection visit(ConfigSection schema) {
		schema.subsection("Features").add(INVENTORY_INTERACTIONS, INTERACTION_SOUNDS, PACKAGE_MAKER_ALLOW_LIST_MODE);
		schema.subsection("Pedantry").add(DROP_EMPTY_PACKAGES_IN_CREATIVE);
		return schema;
	}
}
