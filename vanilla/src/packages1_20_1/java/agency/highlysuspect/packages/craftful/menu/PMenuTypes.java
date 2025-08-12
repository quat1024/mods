package agency.highlysuspect.packages.craftful.menu;

import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.world.inventory.MenuType;

public class PMenuTypes {
	public static Latch<MenuType<PackageMakerMenu>> PACKAGE_MAKER = RegType.MENU_TYPES.latch(PBlocks.PACKAGE_MAKER.id);
}
