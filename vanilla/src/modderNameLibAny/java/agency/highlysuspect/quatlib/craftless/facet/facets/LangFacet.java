package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.FileGenner;
import agency.highlysuspect.quatlib.craftless.facet.Idable;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Objects;

@Facet
public class LangFacet {
	public String file;
	public String key;
	public String value;
	
	public LangFacet file(String file) {
		this.file = file;
		return this;
	}
	
	public LangFacet key(String key) {
		this.key = key;
		return this;
	}
	
	public LangFacet block(Idable blockId) {
		this.key = blockId.getId().toLangKey("block");
		return this;
	}
	
	public LangFacet item(Idable itemId) {
		this.key = itemId.getId().toLangKey("item");
		return this;
	}
	
	public LangFacet sound(Idable soundId) {
		this.key = soundId.getId().toSubtitle();
		return this;
	}
	
	public LangFacet value(String value) {
		this.value = value;
		return this;
	}
	
	protected void check() {
		Objects.requireNonNull(this.file, this::toString);
		Objects.requireNonNull(this.key, this::toString);
		Objects.requireNonNull(this.value, this::toString);
	}
	
	@Override
	public String toString() {
		return "LangFacet(lang file '%s', key '%s', value '%s')".formatted(file, key, value);
	}
	
	public static void handle(FileGenner genner, List<LangFacet> allLangs) {
		allLangs.forEach(LangFacet::check);
		
		//for each lang file
		QuatUtil.collate(allLangs, f -> f.file).forEach((langFile, langs) -> {
			//build the json, sorting the language keys first
			JsonObject langJson = new JsonObject();
			for(LangFacet lang : QuatUtil.sortedCopy(langs, f -> f.key)) langJson.addProperty(lang.key, lang.value);
			
			//and write it out
			genner.writeJson(null, langFile, langJson);
		});
	}
}
