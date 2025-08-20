package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FileGenner;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public record LangFacet(@NotNull String file, @NotNull String key, @NotNull String value) {
	public static Comparator<LangFacet> BY_KEY = Comparator.comparing(LangFacet::key);
	
	public static void handle(FileGenner genner, List<LangFacet> allLangs) {
		QuatUtil.collate(allLangs, LangFacet::file).forEach((langFile, langs) -> {
			JsonObject langJson = new JsonObject();
			for(LangFacet lang : QuatUtil.sortedCopy(langs, BY_KEY))
				langJson.addProperty(lang.key, lang.value);
			
			genner.writeJson(null, langFile, langJson);
		});
	}
}
