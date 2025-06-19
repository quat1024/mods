package agency.highlysuspect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendsTheExtendedBlock extends HelloIExtendBlock {
	public ExtendsTheExtendedBlock(Properties props) {
		super(props);
	}
	
	@Override
	public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		super.destroy(levelAccessor, blockPos, blockState);
	}
}
