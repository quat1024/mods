package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.TagType;
import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import agency.highlysuspect.quatlib.craftless.util.PhysicalVersion;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//"stringly typed" tag values here
@Facet
public record TagFacet(TagType type, Id tag, String value, boolean optional) {
	public static void handle(DgenHelper genner, List<TagFacet> allFacets) {
		QuatUtil.collate(allFacets, f -> f.tag).forEach((tagId, facetsForTagId) -> {
			QuatUtil.collate(facetsForTagId, f -> f.type).forEach((tagType, facets) -> {
				JsonObject obj = new JsonObject();
				obj.addProperty("replace", false);
				
				JsonArray values = new JsonArray();
				for(TagFacet f : QuatUtil.sortedCopy(facets, f -> f.value)) {
					if(f.optional) {
						JsonObject optional = new JsonObject();
						optional.addProperty("id", f.value);
						optional.addProperty("required", false);
						values.add(optional);
					} else {
						values.add(f.value);
					}
				}
				obj.add("values", values);
				
				//thank you mojang for renaming tag folders!
				Map<PhysicalVersion, String> tagFolderMap = new HashMap<>();
				for(PhysicalVersion ver : genner.supportedVersions) tagFolderMap.put(ver, tagType.folder(ver));
				if(QuatUtil.allEqual(tagFolderMap.values())) {
					String filename = "data/" + tagId.ns + "/tags/" + tagFolderMap.values().iterator().next() + "/" + tagId.path;
					genner.writeJson(null, filename, obj);
				} else {
					tagFolderMap.forEach((ver, folder) -> {
						String filename = "data/" + tagId.ns + "/tags/" + folder + "/" + tagId.path;
						genner.writeJson(ver, filename, obj);
					});
				}
			});
		});
	}
}
