package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.FileGenner;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Facet
public class SoundEventFacet {
	Latch<SoundEvent> latch;
	@Nullable String subtitle = DEFAULT_SUBTITLE;
	List<SoundEffect> effects = new ArrayList<>();
	
	private static final String DEFAULT_SUBTITLE = "xxx"; //dummy value
	
	public static class SoundEffect {
		public SoundEffect(String name) {
			this.name = name;
		}
		
		public SoundEffect(Id id) {
			this(id.toStringOmitMinecraft());
		}
		
		String type = "event";
		String name;
		float volume = 1;
		float pitch = 1;
		
		public SoundEffect type(String type) {
			this.type = type;
			return this;
		}
		
		public SoundEffect volume(float volume) {
			this.volume = volume;
			return this;
		}
		
		public SoundEffect volume(double volume) {
			return volume((float) volume);
		}
		
		public SoundEffect pitch(float pitch) {
			this.pitch = pitch;
			return this;
		}
		
		public SoundEffect pitch(double pitch) {
			return pitch((float) pitch);
		}
		
		private void check() {
			if(name == null) throw new IllegalArgumentException("name == null");
		}
		
		private JsonObject toJson() {
			JsonObject o = new JsonObject();
			o.addProperty("type", type);
			o.addProperty("name", name);
			if(volume != 1) o.addProperty("volume", volume);
			if(pitch != 1) o.addProperty("pitch", pitch);
			return o;
		}
	}
	
	public SoundEventFacet latch(Latch<SoundEvent> latch) {
		this.latch = latch;
		return this;
	}
	
	public SoundEventFacet subtitle(String subtitle) {
		this.subtitle = subtitle;
		return this;
	}
	
	public SoundEventFacet effects(SoundEffect... effects) {
		this.effects.addAll(Arrays.asList(effects));
		return this;
	}
	
	//returns the single effect for easier method chaining
	public SoundEffect effect(String name) {
		SoundEffect e = new SoundEffect(name);
		this.effects.add(e);
		return e;
	}
	
	private void check() {
		if(latch == null) throw new IllegalArgumentException("latch == null");
		effects.forEach(SoundEffect::check);
	}
	
	public static void handle(FileGenner genner, List<SoundEventFacet> allSoundEvents) {
		allSoundEvents.forEach(SoundEventFacet::check);
		
		QuatUtil.collate(allSoundEvents, s -> s.latch.id.ns).forEach((namespace, events) -> {
			JsonObject allResults = new JsonObject();
			for(SoundEventFacet event : events) {
				JsonObject result = new JsonObject();
				
				JsonArray sounds = new JsonArray();
				for(SoundEffect x : event.effects) sounds.add(x.toJson());
				result.add("sounds", sounds);
				
				String sub = event.subtitle;
				if(DEFAULT_SUBTITLE.equals(sub)) sub = event.latch.id.toSubtitle();
				if(sub != null) result.addProperty("subtitle", sub);
				
				allResults.add(event.latch.id.path, result);
			}
			
			String filename = "assets/" + namespace + "/sounds.json";
			genner.writeJson(null, filename, allResults);
		});
	}
}
