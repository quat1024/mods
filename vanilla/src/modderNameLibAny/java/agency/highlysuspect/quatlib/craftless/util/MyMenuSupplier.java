package agency.highlysuspect.quatlib.craftless.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

//MenuType.MenuSupplier but public
public interface MyMenuSupplier<T extends AbstractContainerMenu> {
	T create(int var1, Inventory var2);
}
