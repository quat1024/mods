package agency.highlysuspect.mods.test;

import net.minecraft.DetectedVersion;

public class Hi {
	public static void main(String... args) {
		System.out.println("Detecting version");
		System.out.println("Got " + DetectedVersion.tryDetectVersion().getName());
	}
}
