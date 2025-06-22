package agency.highlysuspect.crowmap.ver.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MapItem.class)
public class MapItemMixin {
	@ModifyVariable(
		at = @At("HEAD"),
		method = "inventoryTick",
		argsOnly = true
	)
	public EquipmentSlot onEntityTickPre(EquipmentSlot slot) {
		//returning MAINHAND will make the map always think it's held in the main hand,
		//and therefore it will call its "update" function to update the map
		return EquipmentSlot.MAINHAND;
	}
}
