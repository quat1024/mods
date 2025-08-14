package agency.highlysuspect.quatlib.craftless.facet;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RegType<T> {
	public static final RegType<Block> BLOCKS = new RegType<>("Block");
	public static final RegType<Item> ITEMS = new RegType<>("Item");
	public static final RegType<CreativeModeTab> CREATIVE_TABS = new RegType<>("CreativeModeTab");
	public static final RegType<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new RegType<>("BlockEntityType");
	public static final RegType<SoundEvent> SOUND_EVENTS = new RegType<>("SoundEvent");
	public static final RegType<MenuType<?>> MENU_TYPES = new RegType<>("MenuType");
	
	private final String displayName;
	
	public RegType(String displayName) {
		this.displayName = displayName;
	}
	
	public <X extends T> Latch<X> latch(Id id) {
		return Latch.open(this, id);
	}
	
	@Override
	public String toString() {
		return "RegType<" + displayName + ">";
	}
}
