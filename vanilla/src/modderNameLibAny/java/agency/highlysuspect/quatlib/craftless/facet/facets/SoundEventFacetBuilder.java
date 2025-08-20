package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class SoundEventFacetBuilder implements FacetBuilder {
	public SoundEventFacetBuilder(Latch<SoundEvent> latch) {
		this.latch = Objects.requireNonNull(latch);
		this.creator = () -> SoundEvent.createVariableRangeEvent(latch.id.toMinecraft()); //TODO: push this default into quatlib maybe
		this.subtitleKey = latch.id.toSubtitleKey();
	}
	
	final Latch<SoundEvent> latch;
	Supplier<SoundEvent> creator;
	
	List<SoundEffectBuilder> effects = new ArrayList<>();
	
	@Nullable String subtitleKey;
	Map<String, String> subtitleValues = new HashMap<>(4);
	
	public SoundEventFacetBuilder creator(Supplier<SoundEvent> creator) {
		this.creator = creator;
		return this;
	}
	
	public SoundEventFacetBuilder effects(SoundEffectBuilder... effects) {
		this.effects.addAll(Arrays.asList(effects));
		return this;
	}
	
	public SoundEventFacetBuilder subtitleKey(@Nullable String subtitleKey) {
		this.subtitleKey = subtitleKey;
		return this;
	}
	
	public SoundEventFacetBuilder subtitle(String file, String value) {
		this.subtitleValues.put(file, value);
		return this;
	}
	
	//convenience methods to interact with SoundEffectBuilder at a distance
	public SoundEventFacetBuilder effect(String name) {
		SoundEffectBuilder e = new SoundEffectBuilder(name);
		this.effects.add(e);
		return this;
	}
	
	private SoundEffectBuilder lastEffect() {
		if(effects.isEmpty()) throw new IllegalStateException("call effect() first");
		else return effects.get(effects.size() - 1);
	}
	
	public SoundEventFacetBuilder type(String type) {
		lastEffect().type(type);
		return this;
	}
	
	public SoundEventFacetBuilder volume(float volume) {
		lastEffect().volume(volume);
		return this;
	}
	
	public SoundEventFacetBuilder pitch(float pitch) {
		lastEffect().pitch(pitch);
		return this;
	}
	
	public SoundEventFacetBuilder volume(double volume) {
		lastEffect().volume(volume);
		return this;
	}
	
	public SoundEventFacetBuilder pitch(double pitch) {
		lastEffect().pitch(pitch);
		return this;
	}
	
	@Override
	public void build(FacetBuilder.BuildCtx ctx, FacetBucket facets) {
		//register the sound event
		facets.add(new RegFacet(latch, creator));
		
		if(ctx.dgen == null) return; //not doing datagen
		
		//subtitles
		if(subtitleKey != null)
			subtitleValues.forEach((file, subtitleValue) -> facets.add(new LangFacet(file, subtitleKey, subtitleValue)));
		
		//sounds.json entry
		facets.add(new SoundEventFacet(latch.id, effects, subtitleKey));
	}
}
