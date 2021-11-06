package dev.ftb.mods.ftbic.block.entity.machine;

import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.recipe.MachineRecipeResults;
import dev.ftb.mods.ftbic.recipe.RecipeCache;
import dev.ftb.mods.ftbic.util.PowerTier;

public class ReprocessorBlockEntity extends MachineBlockEntity {
	public ReprocessorBlockEntity() {
		super(FTBICElectricBlocks.REPROCESSOR.blockEntity.get(), 1, 1);
		inputPowerTier = PowerTier.MV;
		energyCapacity = 40000;
		baseEnergyUse = 80;
	}

	@Override
	public MachineRecipeResults getRecipes(RecipeCache cache) {
		return cache.reprocessing;
	}
}