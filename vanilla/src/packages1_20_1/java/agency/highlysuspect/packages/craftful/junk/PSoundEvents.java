package agency.highlysuspect.packages.craftful.junk;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class PSoundEvents {
	public static RegistryHandle<SoundEvent> PACKAGE_MAKER_CRAFT;
	public static RegistryHandle<SoundEvent> INSERT_ONE;
	public static RegistryHandle<SoundEvent> TAKE_ONE;
	public static RegistryHandle<SoundEvent> INSERT_STACK;
	public static RegistryHandle<SoundEvent> TAKE_STACK;
	public static RegistryHandle<SoundEvent> INSERT_ALL;
	public static RegistryHandle<SoundEvent> TAKE_ALL;
	
	private static RegistryHandle<SoundEvent> reg(ResourceLocation id) {
		return Packages.inst().register(BuiltInRegistries.SOUND_EVENT, id, () -> SoundEvent.createVariableRangeEvent(id));
	}
	
	public static void onInitialize() {
		PACKAGE_MAKER_CRAFT = reg(Packages.id("package_maker_craft"));
		INSERT_ONE = reg(Packages.id("insert_one"));
		TAKE_ONE = reg(Packages.id("take_one"));
		INSERT_STACK = reg(Packages.id("insert_stack"));
		TAKE_STACK = reg(Packages.id("take_stack"));
		INSERT_ALL = reg(Packages.id("insert_all"));
		TAKE_ALL = reg(Packages.id("take_all"));
	}
}
