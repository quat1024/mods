package uploaderthing.meta;

import java.util.Locale;

public enum Loader {
	FABRIC, NEOFORGE, FORGE;
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static Loader from(String s) {
		return Loader.valueOf(s.toUpperCase(Locale.ROOT));
	}
	
	public String toCurseforgeGameVersionName() {
		return switch(this)  {
			case FABRIC -> "Fabric";
			case NEOFORGE -> "NeoForge";
			case FORGE -> "Forge";
		};
	}
}
