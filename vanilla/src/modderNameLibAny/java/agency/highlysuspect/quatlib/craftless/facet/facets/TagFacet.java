package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.Facet;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Idable;
import agency.highlysuspect.quatlib.craftless.facet.dgen.DgenHelper;
import agency.highlysuspect.quatlib.craftless.util.PhysicalVersion;
import agency.highlysuspect.quatlib.craftless.util.QuatUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

@Facet
public class TagFacet {
	public TagFacet(Type type) {
		this.type = type;
	}
	
	public static TagFacet block() {
		return new TagFacet(Type.BLOCK);
	}
	
	public static TagFacet item() {
		return new TagFacet(Type.ITEM);
	}
	
	public static TagFacet mineableAxe() {
		return block().tag("mineable/axe");
	}
	
	public Type type;
	public Id tag;
	
	public String value;
	boolean optional;
	
	public TagFacet type(Type type) {
		this.type = type;
		return this;
	}
	
	public TagFacet tag(Idable tag) {
		this.tag = tag.getId();
		return this;
	}
	
	public TagFacet tag(String tag) {
		return tag(Id.parse(tag));
	}
	
	public TagFacet value(String value) {
		this.value = value;
		return this;
	}
	
	public TagFacet value(Idable value) {
		return value(value.getId().toString());
	}
	
	public TagFacet valueTag(Idable valueTag) {
		return value("#" + valueTag.getId().toString());
	}
	
	public TagFacet valueTag(String valueTag) {
		return value("#" + valueTag);
	}
	
	public TagFacet optional(boolean optional) {
		this.optional = optional;
		return this;
	}
	
	public enum Type {
		BLOCK,
		ITEM,
		;
		
		@SuppressWarnings("EnhancedSwitchMigration") //Not all tags were covered by the singularTagNames bikeshed
		String folder(PhysicalVersion version) {
			if(version.singularTagNames()) {
				switch(this) {
					case BLOCK: return "block";
					case ITEM: return "item";
				}
			} else {
				switch(this) {
					case BLOCK: return "blocks";
					case ITEM: return "items";
				}
			}
			
			throw new IllegalArgumentException("Unknown folder for " + this);
		}
	}
	
	public void check() {
		if(type == null) throw new IllegalStateException("null type; " + this);
		if(tag == null) throw new IllegalStateException("null tag;" + this);
		if(value == null) throw new IllegalStateException("null value;" + this);
	}
	
	@Override
	public String toString() {
		return "TagFacet{type=%s, tag=%s, value='%s', optional=%s}".formatted(type, tag, value, optional);
	}
	
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
