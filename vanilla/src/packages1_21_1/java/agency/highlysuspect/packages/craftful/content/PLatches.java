package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PackageBlock;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlock;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.item.StickySyrupItem;
import agency.highlysuspect.packages.craftful.junk.ImmutablePackageContents;
import agency.highlysuspect.packages.craftful.junk.PackageStyle;
import agency.highlysuspect.packages.craftful.menu.PackageMakerMenu;
import agency.highlysuspect.quatlib.craftful.facet.NewRegType;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.LatchPool;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PLatches {
	private static final LatchPool latches = LatchPool.INST;
	
	public static class Blocks {
		public static Latch<PackageBlock> PACKAGE = latches.get(RegType.BLOCKS, Packages.id("package"));
		public static Latch<PackageMakerBlock> PACKAGE_MAKER = latches.get(RegType.BLOCKS, Packages.id("package_maker"));
	}
	
	public static class Items {
		public static Latch<PackageItem> PACKAGE = latches.get(RegType.ITEMS, Blocks.PACKAGE.id);
		public static Latch<BlockItem> PACKAGE_MAKER = latches.get(RegType.ITEMS, Blocks.PACKAGE_MAKER.id);
		public static Latch<StickySyrupItem> STICKY_SYRUP = latches.get(RegType.ITEMS, Packages.id("sticky_syrup"));
	}
	
	public static class BlockEntityTypes {
		public static Latch<BlockEntityType<PackageBlockEntity>> PACKAGE = latches.get(RegType.BLOCK_ENTITY_TYPES, Blocks.PACKAGE.id);
		public static Latch<BlockEntityType<PackageMakerBlockEntity>> PACKAGE_MAKER = latches.get(RegType.BLOCK_ENTITY_TYPES, Blocks.PACKAGE_MAKER.id);
	}
	
	public static class MenuTypes {
		public static Latch<MenuType<PackageMakerMenu>> PACKAGE_MAKER = latches.get(RegType.MENU_TYPES, Blocks.PACKAGE_MAKER.id);
	}
	
	public static class SoundEvents {
		public static Latch<SoundEvent> PACKAGE_MAKER_CRAFT = latches.get(RegType.SOUND_EVENTS, Packages.id("package_maker_craft"));
		
		public static Latch<SoundEvent> INSERT_ONE = latches.get(RegType.SOUND_EVENTS, Packages.id("insert_one"));
		public static Latch<SoundEvent> TAKE_ONE = latches.get(RegType.SOUND_EVENTS, Packages.id("take_one"));
		public static Latch<SoundEvent> INSERT_STACK = latches.get(RegType.SOUND_EVENTS, Packages.id("insert_stack"));
		public static Latch<SoundEvent> TAKE_STACK = latches.get(RegType.SOUND_EVENTS, Packages.id("take_stack"));
		public static Latch<SoundEvent> INSERT_ALL = latches.get(RegType.SOUND_EVENTS, Packages.id("insert_all"));
		public static Latch<SoundEvent> TAKE_ALL = latches.get(RegType.SOUND_EVENTS, Packages.id("take_all"));
		
		public static Latch<SoundEvent> STICKY_SYRUP_APPLY = latches.get(RegType.SOUND_EVENTS, Packages.id("sticky_syrup_apply"));
		public static Latch<SoundEvent> STICKY_SYRUP_CLEAR = latches.get(RegType.SOUND_EVENTS, Packages.id("sticky_syrup_clear"));
	}
	
	public static class DataComponentTypes {
		public static final Latch<DataComponentType<ImmutablePackageContents>> PACKAGE_CONTENTS = latches.get(NewRegType.DATA_COMPONENT_TYPE, Packages.id("cont"));
		public static final Latch<DataComponentType<PackageStyle>> PACKAGE_STYLE = latches.get(NewRegType.DATA_COMPONENT_TYPE, Packages.id("style"));
	}
}
