package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Idable;
import agency.highlysuspect.quatlib.craftless.facet.TagType;

public class TagFacetBuilder implements FacetBuilder {
	public TagFacetBuilder(TagType type, Idable tag) {
		this.type = type;
		this.tag = tag.getId();
	}
	
	public TagFacetBuilder(TagType type, String tag) {
		this(type, Id.parse(tag));
	}
	
	public static TagFacetBuilder block(Idable tag) {
		return new TagFacetBuilder(TagType.BLOCK, tag);
	}
	
	public static TagFacetBuilder block(String tag) {
		return new TagFacetBuilder(TagType.BLOCK, tag);
	}
	
	public static TagFacetBuilder item(Idable tag) {
		return new TagFacetBuilder(TagType.ITEM, tag);
	}
	
	public static TagFacetBuilder item(String tag) {
		return new TagFacetBuilder(TagType.ITEM, tag);
	}
	
	public static TagFacetBuilder mineableAxe() {
		return block(Id.parse("minecraft:mineable/axe"));
	}
	
	public final TagType type;
	public final Id tag;
	
	public String value;
	boolean optional;
	
	public TagFacetBuilder value(String value) {
		this.value = value;
		return this;
	}
	
	public TagFacetBuilder value(Idable value) {
		return value(value.getId().toString());
	}
	
	public TagFacetBuilder valueTag(Idable valueTag) {
		return value("#" + valueTag.getId().toString());
	}
	
	public TagFacetBuilder valueTag(String valueTag) {
		return value("#" + valueTag);
	}
	
	public TagFacetBuilder optional(boolean optional) {
		this.optional = optional;
		return this;
	}
	
	@Override
	public void build(FacetBuilder.BuildCtx ctx, FacetBucket facets) {
		if(ctx.dgen == null) return; //not doing datagen
		
		if(type == null) throw new IllegalStateException("null type; " + this);
		if(tag == null) throw new IllegalStateException("null tag;" + this);
		if(value == null) throw new IllegalStateException("null value;" + this);
		
		facets.add(new TagFacet(type, tag, value, optional));
	}
	
	@Override
	public String toString() {
		return "TagFacetBuilder{type=%s, tag=%s, value='%s', optional=%s}".formatted(type, tag, value, optional);
	}
}
