package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.item.StickySyrupItem;
import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.menu.PackageMakerMenu;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PLatches {
	public static class Blocks {
		public static Latch<PackageBlock> PACKAGE = Latch.open(RegType.BLOCKS, Packages.id("package"));
		public static Latch<PackageMakerBlock> PACKAGE_MAKER = Latch.open(RegType.BLOCKS, Packages.id("package_maker"));
	}
	
	public static class Items {
		public static Latch<PackageItem> PACKAGE = Latch.open(RegType.ITEMS, Blocks.PACKAGE.id);
		public static Latch<BlockItem> PACKAGE_MAKER = Latch.open(RegType.ITEMS, Blocks.PACKAGE_MAKER.id);
		public static Latch<StickySyrupItem> STICKY_SYRUP = Latch.open(RegType.ITEMS, Packages.id("sticky_syrup"));
	}
	
	public static class BlockEntityTypes {
		public static Latch<BlockEntityType<PackageBlockEntity>> PACKAGE = Latch.open(RegType.BLOCK_ENTITY_TYPES, Blocks.PACKAGE.id);
		public static Latch<BlockEntityType<PackageMakerBlockEntity>> PACKAGE_MAKER = Latch.open(RegType.BLOCK_ENTITY_TYPES, Blocks.PACKAGE_MAKER.id);
	}
	
	public static class MenuTypes {
		public static Latch<MenuType<PackageMakerMenu>> PACKAGE_MAKER = RegType.MENU_TYPES.latch(Blocks.PACKAGE_MAKER.id);
	}
	
	public static class SoundEvents {
		public static Latch<SoundEvent> PACKAGE_MAKER_CRAFT = RegType.SOUND_EVENTS.latch(Packages.id("package_maker_craft"));
		public static Latch<SoundEvent> INSERT_ONE = RegType.SOUND_EVENTS.latch(Packages.id("insert_one"));
		public static Latch<SoundEvent> TAKE_ONE = RegType.SOUND_EVENTS.latch(Packages.id("take_one"));
		public static Latch<SoundEvent> INSERT_STACK = RegType.SOUND_EVENTS.latch(Packages.id("insert_stack"));
		public static Latch<SoundEvent> TAKE_STACK = RegType.SOUND_EVENTS.latch(Packages.id("take_stack"));
		public static Latch<SoundEvent> INSERT_ALL = RegType.SOUND_EVENTS.latch(Packages.id("insert_all"));
		public static Latch<SoundEvent> TAKE_ALL = RegType.SOUND_EVENTS.latch(Packages.id("take_all"));
	}
}
