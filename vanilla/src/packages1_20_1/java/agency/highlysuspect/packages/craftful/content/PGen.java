package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.facets.LangFacet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface PGen extends Gen {
	String EN_US = "assets/packages__lang_gen/lang/en_us.json";
	
	default LangFacet enUs() {
		return new LangFacet().file(EN_US);
	}
	
	default ResourceLocation toMc(Id id) {
		return QuatlibBase.inst().rlBridge.fromId(id);
	}
	
	default void simpleSoundEvent(Ctx ctx, Latch<SoundEvent> l) {
		ctx.reg(l, () -> SoundEvent.createVariableRangeEvent(toMc(l.id)));
	}
}
