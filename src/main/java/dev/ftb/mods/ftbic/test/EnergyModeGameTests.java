package dev.ftb.mods.ftbic.test;

import dev.ftb.mods.ftbic.FTBIC;
import dev.ftb.mods.ftbic.FTBICConfig;
import dev.ftb.mods.ftbic.block.ElectricBlockInstance;
import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;

final class EnergyModeGameTests {
	static void rectifiersFollowEnergyMode(GameTestHelper helper) {
		boolean fullFE = FTBICConfig.ENERGY.FULL_FE_MODE.get();
		RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
		for (ElectricBlockInstance rectifier : FTBICElectricBlocks.RECTIFIERS) {
			var key = ResourceKey.create(Registries.RECIPE, FTBIC.id("shaped/" + rectifier.id));
			helper.assertValueEqual(!fullFE, recipes.byKey(key).isPresent(), rectifier.id + " recipe loaded");
			helper.assertValueEqual(fullFE, FTBICElectricBlocks.isHidden(rectifier), rectifier.id + " hidden");
		}
		helper.assertFalse(FTBICElectricBlocks.isHidden(FTBICElectricBlocks.EV_BATTERY_BOX), "Other electric blocks stay visible");
		helper.succeed();
	}
}
