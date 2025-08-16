package agency.highlysuspect.packages.craftful.junk;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * TODO: Kind of a stub class right now, meant to hold some of the gameplay rules.
 *  Inserting into an itemstack package has different rules than an in-world package, e.g. itemstacks don't have a concept of stickiness.
 *  That's the idea. Maybe in the future, upgraded packages with more space or whatever as well.
 */
public interface PackageRules {
	PackageRules DEFAULT = new PackageRules() {};
	
	default int packageSlotCount() {
		return 8;
	}
	
	default int packageRecursionLimit() {
		return 3;
	}
	
	//The amount of items per-internal-slot that a Package is allowed to hold, if it contained `stack`.
	//Packages normally hold eight stacks of items, but to nerf nesting a bit, packages can only hold eight nonempty packages.
	//TODO: leaky abstraction, see comment in canPlaceItem (season2: applies to PackageContainer)
	//TODO(seaosn2): Maybe should return 0 when stack is not allowed in package?
	default int maxPerPackageSlot(ItemStack stack) {
		if(!allowedInPackageAtAll(stack)) return 0;
		
		//Nerf nesting
		ImmutablePackageContents pkg = stack.get(ImmutablePackageContents.DATA_COMPONENT_TYPE);
		if(pkg != null && pkg.count() > 0) return 1;
		
		else return Math.min(stack.getMaxStackSize(), 64);
	}
	
	default int maxInPackageTotal(ItemStack stack) {
		return maxPerPackageSlot(stack) * packageSlotCount();
	}
	
	default boolean allowedInPackageAtAll(ItemStack stack) {
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
		else return cont.calcRecursionLevel() < packageRecursionLimit();
	}
	
	//TODO: subclass this to implement stickystacks
	default boolean allowedToInsertInPackage(ItemStack stack) {
		return allowedInPackageAtAll(stack);
	}
}
