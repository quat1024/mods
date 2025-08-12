package agency.highlysuspect.quatlib.craftless.facet;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RegType<T> {
	public static final RegType<Block> BLOCKS = new RegType<>("Block");
	public static final RegType<Item> ITEMS = new RegType<>("Item");
	public static final RegType<CreativeModeTab> CREATIVE_TABS = new RegType<>("CreativeModeTab");
	public static final RegType<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new RegType<>("BlockEntityType");
	
	private final String displayName;
	
	protected RegType(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return "RegType<" + displayName + ">";
	}
}
