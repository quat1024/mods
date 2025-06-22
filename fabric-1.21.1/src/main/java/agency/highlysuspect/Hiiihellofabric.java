package agency.highlysuspect;

import agency.highlysuspect.quatlib.floader.CrummyConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;

public class Hiiihellofabric {
	static {
		System.out.println(FabricLoader.class);
		System.out.println(Block.class); //are you remapped
		System.out.println(CrummyConfig.class);
	}
}
