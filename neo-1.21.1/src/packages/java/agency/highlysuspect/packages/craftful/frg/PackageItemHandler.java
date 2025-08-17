package agency.highlysuspect.packages.craftful.frg;

import agency.highlysuspect.packages.craftful.junk.ImmutablePackageContents;
import agency.highlysuspect.packages.craftful.junk.PackageContainer2;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class PackageItemHandler implements IItemHandler {
	public PackageItemHandler(PackageContainer2 container) {
		this.container = container;
	}
	
	private final PackageContainer2 container;
	
	@Override
	public int getSlots() {
		return container.getRules().packageSlotCount();
	}
	
	@Override
	public ItemStack getStackInSlot(int i) {
		if(i < 0 || i >= getSlots()) return ItemStack.EMPTY;
		
		ImmutablePackageContents contents = container.getContents();
		if(contents.isEmpty()) return ItemStack.EMPTY;
		
		int stackAmountPerSlot = container.getRules().maxPerPackageSlot(contents.stack());
		if(stackAmountPerSlot == 0) return ItemStack.EMPTY; //It returns 0 if the item is not permitted to exist in packages at all
		
		//running example: say there are 138 items in the package (64 + 64 + 10)
		//slots 0 and 1 should contain 64 items, slot 2 should contain 10, and the rest should be empty
		
		int count = contents.count();
		int fullSlotCount = count / stackAmountPerSlot; //the 2 full slots (rounds down)
		int halfFilledSlotItemCount = count - (stackAmountPerSlot * fullSlotCount); //10
		
		if(i < fullSlotCount) { //slots 0 and 1 are full
			return contents.stack().copyWithCount(stackAmountPerSlot);
		} else if(i == fullSlotCount) { //slot 2 has 10 items
			return contents.stack().copyWithCount(halfFilledSlotItemCount);
		} else return ItemStack.EMPTY; //everything else is empty
	}
	
	@Override
	public int getSlotLimit(int i) {
		return container.getRules().maxPerPackageSlot(container.getContents().stack());
	}
	
	@Override
	public boolean isItemValid(int i, ItemStack stack) {
		return container.getRules().allowedInPackageAtAll(stack);
	}
	
	//returns the leftover itemstack that was NOT inserted
	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		ImmutablePackageContents oldContents = container.getContents();
		if(stack.isEmpty() || !oldContents.allowedToInsert(stack, container.getRules())) return stack;
		
		ImmutablePackageContents.InsertionResult result = oldContents.withInsertion(stack, Integer.MAX_VALUE, container.getRules());
		if(result.insertedAmount() <= 0) return stack;
		
		if(!simulate) {
			container.setContentsNonCommitted(result.newContents());
			container.commitContents();
		}
		
		if(stack.getCount() == result.insertedAmount()) return ItemStack.EMPTY;
		else return stack.copyWithCount(stack.getCount() - result.insertedAmount());
	}
	
	//Returns the itemstack that was extracted (modifiable)
	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		//we'll just ignore the slot parameter (TODO: is this okay to do?)
		
		ImmutablePackageContents oldContents = container.getContents();
		if(oldContents.isEmpty()) return ItemStack.EMPTY;
		
		ImmutablePackageContents.TakeResult result = oldContents.withTake(amount, container.getRules());
		if(result.takenAmount() == 0) return ItemStack.EMPTY;
		
		//using the ItemStack from oldContents, just in case the extraction made newContents empty
		ItemStack stack = oldContents.stack().copyWithCount(result.takenAmount());
		
		if(!simulate) {
			container.setContentsNonCommitted(result.newContents());
			container.commitContents();
		}
		
		return stack;
	}
}
