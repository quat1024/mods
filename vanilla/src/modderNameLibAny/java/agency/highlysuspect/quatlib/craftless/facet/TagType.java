package agency.highlysuspect.quatlib.craftless.facet;

import agency.highlysuspect.quatlib.craftless.util.PhysicalVersion;

public enum TagType {
	BLOCK,
	ITEM,
	//...TODO more...
	;
	
	@SuppressWarnings("EnhancedSwitchMigration")
	//Not all tags were covered by the singularTagNames bikeshed
	public String folder(PhysicalVersion version) {
		if(version.singularTagNames()) {
			switch(this) {
				case BLOCK:
					return "block";
				case ITEM:
					return "item";
			}
		} else {
			switch(this) {
				case BLOCK:
					return "blocks";
				case ITEM:
					return "items";
			}
		}
		
		throw new IllegalArgumentException("Unknown folder for " + this);
	}
}
