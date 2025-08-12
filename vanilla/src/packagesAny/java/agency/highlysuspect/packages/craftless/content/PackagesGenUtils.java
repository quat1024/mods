package agency.highlysuspect.packages.craftless.content;

import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.facets.LangFacet;

public interface PackagesGenUtils {
	Id EN_US = new Id("packages__lang_gen", "assets/lang/en_us.json");
	
	default LangFacet enUs() {
		return new LangFacet().file(EN_US);
	}
}
