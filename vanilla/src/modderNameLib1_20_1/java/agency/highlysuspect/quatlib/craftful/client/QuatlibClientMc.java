package agency.highlysuspect.quatlib.craftful.client;

import agency.highlysuspect.quatlib.craftless.client.MyBlockEntityRendererProvider;
import agency.highlysuspect.quatlib.craftless.client.QuatlibClientBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class QuatlibClientMc extends QuatlibClientBase {
	//TODO: remove (only exists because of a crossroad bug)
	public <T extends BlockEntity> BlockEntityRendererProvider<T> adaptRendererProvider(MyBlockEntityRendererProvider<T> mine) {
		return mcContext -> mine.myCreate(new MyBlockEntityRendererProvider.MyContext(
			mcContext.getBlockEntityRenderDispatcher(),
			mcContext.getBlockRenderDispatcher(),
			null,
			mcContext.getItemRenderer(),
			mcContext.getEntityRenderer(),
			mcContext.getModelSet(),
			mcContext.getFont())
		);
	}
	
	public static QuatlibClientMc inst() {
		return (QuatlibClientMc) INST;
	}
}
