package dev.ftb.mods.ftbic.block.entity.machine;

import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.recipe.MachineRecipeResults;
import dev.ftb.mods.ftbic.recipe.RecipeCache;

public class CentrifugeBlockEntity extends MachineBlockEntity {
	public CentrifugeBlockEntity() {
		super(FTBICElectricBlocks.CENTRIFUGE.blockEntity.get(), 1, 1);
		energyCapacity = 8000;
		baseEnergyUse = 20;
	}

	@Override
	public MachineRecipeResults getRecipes(RecipeCache cache) {
		return cache.separating;
	}
}