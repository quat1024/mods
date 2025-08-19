package agency.highlysuspect.quatlib.craftless.facet.facets;

import agency.highlysuspect.quatlib.craftless.facet.FacetBucket;
import agency.highlysuspect.quatlib.craftless.facet.Idable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LangFacetBuilder implements FacetBuilder {
	public LangFacetBuilder(String file) {
		this.file = Objects.requireNonNull(file);
	}
	
	final @NotNull String file;
	String key;
	String value;
	
	public LangFacetBuilder key(String key) {
		this.key = key;
		return this;
	}
	
	public LangFacetBuilder block(Idable id) {
		this.key = id.getId().toLangKey("block");
		return this;
	}
	
	public LangFacetBuilder item(Idable id) {
		this.key = id.getId().toLangKey("item");
		return this;
	}
	
	public LangFacetBuilder subtitle(Idable id) {
		this.key = id.getId().toSubtitleKey();
		return this;
	}
	
	public LangFacetBuilder value(String value) {
		this.value = value;
		return this;
	}
	
	public LangFacetBuilder block(Idable id, String value) {
		return block(id).value(value);
	}
	
	public LangFacetBuilder item(Idable id, String value) {
		return item(id).value(value);
	}
	
	public LangFacetBuilder subtitle(Idable id, String value) {
		return subtitle(id).value(value);
	}
	
	@Override
	public void build(FacetBucket facets) {
		Objects.requireNonNull(this.key, this::toString);
		Objects.requireNonNull(this.value, this::toString);
		
		facets.add(new LangFacet(file, key, value));
	}
	
	@Override
	public String toString() {
		return "LangFacetBuilder(lang file '%s', key '%s', value '%s')".formatted(file, key, value);
	}
}
