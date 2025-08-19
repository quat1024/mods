package agency.highlysuspect.quatlib.craftless.facet.facets;

import com.google.gson.JsonObject;

import java.util.Objects;

//part of SoundEventFacetBuilder
public class SoundEffectBuilder {
	public SoundEffectBuilder(String name) {
		this.name = Objects.requireNonNull(name, "null name");
	}
	
	final String name;
	String type = "event";
	float volume = 1;
	float pitch = 1;
	
	public SoundEffectBuilder type(String type) {
		this.type = type;
		return this;
	}
	
	public SoundEffectBuilder volume(float volume) {
		this.volume = volume;
		return this;
	}
	
	public SoundEffectBuilder pitch(float pitch) {
		this.pitch = pitch;
		return this;
	}
	
	public SoundEffectBuilder volume(double volume) {
		return volume((float) volume);
	}
	
	public SoundEffectBuilder pitch(double pitch) {
		return pitch((float) pitch);
	}
	
	public JsonObject toJson() {
		JsonObject o = new JsonObject();
		o.addProperty("type", type);
		o.addProperty("name", name);
		if(volume != 1) o.addProperty("volume", volume);
		if(pitch != 1) o.addProperty("pitch", pitch);
		return o;
	}
}
