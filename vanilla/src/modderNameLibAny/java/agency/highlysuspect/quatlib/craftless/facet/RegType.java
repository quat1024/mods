package agency.highlysuspect.quatlib.craftless.facet;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RegType<T> {
	public static final RegType<Block> BLOCKS = new RegType<>("Block");
	public static final RegType<Item> ITEMS = new RegType<>("Item");
	
	private final String displayName;
	
	protected RegType(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return "RegType<" + displayName + ">";
	}
}
