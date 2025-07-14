package agency.highlysuspect.rebindnarrator.craftful.frg;

import agency.highlysuspect.rebindnarrator.craftful.RebindNarratorMc;
import agency.highlysuspect.rebindnarrator.craftful.VanillaNarratorKeyPredicate;
import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class RebindNarratorForgeInit extends RebindNarratorMc {
	public RebindNarratorForgeInit() {
		super.initConfig();
		super.init();
		
		LOG.info("Hello from RebindNarratorForgeInit");
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modBus.addListener(this::onRegisterKeyMappings);
	}
	
	public final KeyMapping NARRATOR_KEY = new KeyMapping(
		"options.narrator",
		KeyConflictContext.UNIVERSAL,
		KeyModifier.CONTROL,
		InputConstants.getKey("key.keyboard.b"),
		"key.categories.misc"
	);
	
	public void onRegisterKeyMappings(RegisterKeyMappingsEvent e) {
		LOG.info("Hello from onRegisterKeyMappings");
		e.register(NARRATOR_KEY);
	}
	
	@Override
	public NarratorKeyPredicate makeKeyPredicate() {
		LOG.info("Hello from makeKeyPredicate");
		return new ForgeNarratorKeyPredicate();
	}
	
	public class ForgeNarratorKeyPredicate extends VanillaNarratorKeyPredicate {
		@Override
		public boolean isCorrectKey(int glfwKeyToken) {
			LOG.info("Hello from isCorrectKey");
			return glfwKeyToken != -1 && glfwKeyToken == NARRATOR_KEY.getKey().getValue();
		}
		
		@Override
		public boolean correctModifiersPressed() {
			return NARRATOR_KEY.getKeyModifier().isActive(KeyConflictContext.UNIVERSAL);
		}
	}
}
