package agency.highlysuspect.packages.craftful.frg;

import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.junk.PackageContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public class PackageItemHandler extends InvWrapper {
	public PackageItemHandler(PackageBlockEntity be) {
		super(be.getContainer());
		this.be = be;
		this.container = (PackageContainer) getInv();
	}
	
	private final PackageBlockEntity be;
	private final PackageContainer container;
	
	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		//TODO: Push this logic into PackageContainer instead of here!!!!!
		
		ItemStack stickyStack = be.getStickyStack();
		if(!stickyStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, stack)) return stack; //Doesn't fit.
		
		//using the custom package insertion method, instead of going slot-by-slot
		return container.insert(stack, stack.getCount(), simulate);
	}
	
	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		//using the custom package removal method, instead of going slot-by-slot
		return container.take(amount, simulate);
	}
}
