package agency.highlysuspect.rebindnarrator.craftful.neo;

import agency.highlysuspect.rebindnarrator.craftful.RebindNarratorMc;
import agency.highlysuspect.rebindnarrator.craftful.VanillaNarratorKeyPredicate;
import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

@Mod(value = RebindNarratorNeoInit.MODID, dist = Dist.CLIENT)
public class RebindNarratorNeoInit extends RebindNarratorMc {
	public RebindNarratorNeoInit(IEventBus modBus) {
		super.initConfig();
		super.init();
		
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
		e.register(NARRATOR_KEY);
	}
	
	@Override
	public NarratorKeyPredicate makeKeyPredicate() {
		return new NeoforgeNarratorKeyPredicate();
	}
	
	public class NeoforgeNarratorKeyPredicate extends VanillaNarratorKeyPredicate {
		@Override
		public boolean isCorrectKey(int glfwKeyToken) {
			return glfwKeyToken == NARRATOR_KEY.getKey().getValue();
		}
		
		@Override
		public boolean correctModifiersPressed() {
			return NARRATOR_KEY.getKeyModifier().isActive(KeyConflictContext.UNIVERSAL);
		}
	}
}
