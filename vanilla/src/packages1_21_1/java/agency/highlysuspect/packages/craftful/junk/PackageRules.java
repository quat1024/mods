package agency.highlysuspect.packages.craftful.junk;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class PackageRules {
	public static final PackageRules DEFAULT = new PackageRules();
	
	public int slotCount() {
		return 8;
	}
	
	public int recursionLimit() {
		return 3;
	}
	
	//The amount of items per-internal-slot that a Package is allowed to hold, if it contained `stack`.
	//Packages normally hold eight stacks of items, but to nerf nesting a bit, packages can only hold eight nonempty packages.
	//TODO: leaky abstraction, see comment in canPlaceItem
	//TODO(season2): prev comment applies to PackageContainer
	//TODO(seaosn2): Should return 0 when stack is not allowed in package
	public int maxPerSlot(ItemStack stack) {
		if(stack.is(PTags.BANNED_FROM_PACKAGE)) return 0;
		
		ImmutablePackageContents pkg = stack.get(ImmutablePackageContents.DATA_COMPONENT_TYPE);
		if(pkg != null && pkg.count() > 0) return 1;
		else return Math.min(stack.getMaxStackSize(), 64);
	}
	
	public int maxInTotal(ItemStack stack) {
		return maxPerSlot(stack) * slotCount();
	}
	
	public boolean allowedInPackageAtAll(ItemStack stack) {
		if(stack.is(PTags.BANNED_FROM_PACKAGE)) return false;
		
		//Item#canFitInsideContainerItems is a vanilla API for "is this item allowed to go inside containers".
		//In vanilla it only returns `false` on shulker boxes. It also doesn't pass the whole ItemStack.
		//I think it'd be good to call this API if I can, but I need to carve out some exceptions.
		boolean checkCanFitInsideContainerItems = true;
		
		//Shulker boxes are allowed, but only if they're empty.
		if(stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
			checkCanFitInsideContainerItems = false;
			
			//TODO(season2, 1.21.1): test
			ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
			if(contents != null && contents.stream().anyMatch(s -> !s.isEmpty()))
				return false;
		}
		
		//Bundles are allowed, but only if they're empty.
		if(stack.getItem() instanceof BundleItem) {
			checkCanFitInsideContainerItems = false;
			if(stack.isBarVisible()) return false;
		}
		
		//Otherwise, if it can't fit inside container items, it's not allowed.
		if(checkCanFitInsideContainerItems && !stack.getItem().canFitInsideContainerItems()) return false;
		
		//Otherwise, if it's not a package, it is allowed.
		ImmutablePackageContents cont = stack.get(ImmutablePackageContents.DATA_COMPONENT_TYPE);
		if(cont == null) return true;
		
		//And packages are only allowed if they aren't nested too deeply.
		else return cont.calcRecursionLevel() < recursionLimit();
	}
	
	//TODO: subclass this to implement stickystacks
	public boolean allowedToInsert(ItemStack stack) {
		return allowedInPackageAtAll(stack);
	}
}
