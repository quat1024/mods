package agency.highlysuspect.rebindnarrator.craftful.neo;

import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorImpl;
import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorModBase;
import agency.highlysuspect.rebindnarrator.craftful.RebindNarratorVanilla1_21_5;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

@Mod(value = RebindNarratorModBase.MODID, dist = Dist.CLIENT)
public class RebindNarratorNeoInit {
	public RebindNarratorNeoInit(IEventBus modBus) {
		modBus.addListener(this::onRegisterKeyMappings);
		
		RebindNarratorImpl.IMPL = new Impl();
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
	
	public class Impl extends RebindNarratorVanilla1_21_5 {
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
