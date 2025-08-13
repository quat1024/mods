package agency.highlysuspect.quatlib.craftless.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: This only has to exist because `crossroad` sucks at inner classes
 *  https://github.com/CrackedPolishedBlackstoneBricksMC/crossroad/issues/1
 */
public interface MyBlockEntityRendererProvider<T extends BlockEntity> {
	BlockEntityRenderer<T> myCreate(MyContext myContext);
	
	class MyContext {
		private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
		private final BlockRenderDispatcher blockRenderDispatcher;
		private final @Nullable Object itemModelResolver; //Not in 1.20.1 so crossroad can't find it
		private final ItemRenderer itemRenderer;
		private final EntityRenderDispatcher entityRenderer;
		private final EntityModelSet modelSet;
		private final Font font;
		
		public MyContext(BlockEntityRenderDispatcher a, BlockRenderDispatcher b, @Nullable Object c, ItemRenderer d, EntityRenderDispatcher e, EntityModelSet f, Font g) {
			this.blockEntityRenderDispatcher = a;
			this.blockRenderDispatcher = b;
			this.itemModelResolver = c;
			this.itemRenderer = d;
			this.entityRenderer = e;
			this.modelSet = f;
			this.font = g;
		}
		
		public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() { return this.blockEntityRenderDispatcher; }
		public BlockRenderDispatcher getBlockRenderDispatcher() { return this.blockRenderDispatcher; }
		public EntityRenderDispatcher getEntityRenderer() { return this.entityRenderer; }
		public @Nullable Object getItemModelResolver() { return this.itemModelResolver; }
		public ItemRenderer getItemRenderer() { return this.itemRenderer; }
		public EntityModelSet getModelSet() { return this.modelSet; }
		public ModelPart bakeLayer(ModelLayerLocation mll) { return this.modelSet.bakeLayer(mll); }
		public Font getFont() { return this.font; }
	}
}
