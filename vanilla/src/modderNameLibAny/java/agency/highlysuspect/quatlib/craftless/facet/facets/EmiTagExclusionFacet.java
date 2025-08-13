package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * TODO: flesh this out a bit more
 */
@Facet
public class EmiTagExclusionFacet {
	public EmiTagExclusionFacet(Id itemTag) {
		this.itemTag = itemTag;
	}
	
	public EmiTagExclusionFacet(String itemTag) {
		this(Id.parse(itemTag));
	}
	
	Id itemTag;
	
	public static void handle(DgenHelper dgen, List<EmiTagExclusionFacet> facets) {
		JsonObject obj = new JsonObject();
		
		JsonArray item = new JsonArray();
		for(EmiTagExclusionFacet facet : QuatUtil.sortedCopy(facets, f -> f.itemTag))
			item.add(facet.itemTag.toString());
		obj.add("item", item);
		
		dgen.writeJson(null, "assets/emi/tag/exclusions/" + dgen.modid, obj);
	}
}
