package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.FileGenner;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Facet
public record SoundEventFacet(Id soundEventId, List<SoundEffectBuilder> effects, @Nullable String subtitleKey) {
	public static void handle(FileGenner genner, List<SoundEventFacet> allSoundEvents) {
		QuatUtil.collate(allSoundEvents, s -> s.soundEventId.ns).forEach((namespace, events) -> {
			JsonObject allResults = new JsonObject();
			for(SoundEventFacet event : events) {
				JsonObject result = new JsonObject();
				
				JsonArray sounds = new JsonArray();
				for(SoundEffectBuilder x : event.effects) sounds.add(x.toJson());
				result.add("sounds", sounds);
				
				if(event.subtitleKey != null) result.addProperty("subtitle", event.subtitleKey);
				
				allResults.add(event.soundEventId.path, result);
			}
			
			String filename = "assets/" + namespace + "/sounds.json";
			genner.writeJson(null, filename, allResults);
		});
	}
}
