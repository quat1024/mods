package agency.highlysuspect.packages.craftful.junk;

import net.minecraft.world.item.ItemStack;

public class ItemStackPackageContainer2 implements PackageContainer2 {
	public ItemStackPackageContainer2(ItemStack stack, PackageRules rules) {
		this.stack = stack;
		this.rules = rules;
	}
	
	private final ItemStack stack;
	private final PackageRules rules;
	
	@Override
	public ImmutablePackageContents getContents() {
		return stack.get(ImmutablePackageContents.DATA_COMPONENT_TYPE);
	}
	
	@Override
	public void setContentsNonCommitted(ImmutablePackageContents newContents) {
		stack.set(ImmutablePackageContents.DATA_COMPONENT_TYPE, newContents);
	}
	
	@Override
	public void commitContents() {
		//I'd call setChanged() here if I needed to. It's an itemstack though.
	}
	
	@Override
	public PackageRules getRules() {
		return rules;
	}
}
