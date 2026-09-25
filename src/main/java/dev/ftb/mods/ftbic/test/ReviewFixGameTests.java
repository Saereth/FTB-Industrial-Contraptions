package dev.ftb.mods.ftbic.test;

import dev.ftb.mods.ftbic.FTBIC;
import dev.ftb.mods.ftbic.FTBICConfig;
import dev.ftb.mods.ftbic.block.ElectricBlockInstance;
import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.block.entity.machine.BasicMachineBlockEntity;
import dev.ftb.mods.ftbic.item.BatteryItem;
import dev.ftb.mods.ftbic.item.FTBICItems;
import dev.ftb.mods.ftbic.recipe.RecipeToggleCondition;
import dev.ftb.mods.ftbic.registry.ModDataComponents;
import dev.ftb.mods.ftbic.util.GhostItem;
import dev.ftb.mods.ftbic.util.MachineConfiguration;
import dev.ftb.mods.ftbic.util.SideConfiguration;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;
import java.util.Map;

final class ReviewFixGameTests {
	private static final BlockPos POS = new BlockPos(1, 2, 1);

	static void batteriesUseConfiguredCharge(GameTestHelper helper) {
		ItemStack singleUse = new ItemStack(FTBICItems.SINGLE_USE_BATTERY.get());
		BatteryItem singleUseItem = (BatteryItem) singleUse.getItem();
		double singleUseCapacity = FTBICConfig.ENERGY.SINGLE_USE_BATTERY_CAPACITY.get();
		helper.assertValueEqual(singleUseCapacity, singleUseItem.getEnergyCapacity(singleUse), "Single-use capacity follows the config");
		helper.assertValueEqual(singleUseCapacity, singleUseItem.getEnergy(singleUse), "A fresh single-use battery is fully charged");
		helper.assertValueEqual(singleUseCapacity, singleUseItem.extractEnergy(singleUse, singleUseCapacity * 2D, true), "A fresh single-use battery can discharge its full charge");

		ItemStack lv = new ItemStack(FTBICItems.LV_BATTERY.get());
		BatteryItem lvItem = (BatteryItem) lv.getItem();
		helper.assertValueEqual(FTBICConfig.ENERGY.LV_BATTERY_CAPACITY.get(), lvItem.getEnergyCapacity(lv), "LV battery capacity follows the config");
		helper.assertValueEqual(0D, lvItem.getEnergy(lv), "A fresh rechargeable battery starts empty");
		helper.succeed();
	}

	static void bankBlocksDropThemselves(GameTestHelper helper) {
		for (ElectricBlockInstance instance : List.of(FTBICElectricBlocks.INDUSTRIAL_BANK_CELL, FTBICElectricBlocks.INDUSTRIAL_BANK_PORT)) {
			helper.setBlock(POS, instance.block.get());
			BlockPos abs = helper.absolutePos(POS);
			List<ItemStack> drops = Block.getDrops(helper.getLevel().getBlockState(abs), helper.getLevel(), abs, helper.getLevel().getBlockEntity(abs));
			helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(instance.item.get())), instance.id + " drops itself when broken");
			helper.setBlock(POS, Blocks.AIR);
		}
		helper.succeed();
	}

	static void upgradedEnergySurvivesReload(GameTestHelper helper) {
		helper.setBlock(POS, FTBICElectricBlocks.MACERATOR.block.get());
		BasicMachineBlockEntity machine = helper.getBlockEntity(POS, BasicMachineBlockEntity.class);
		double baseCapacity = machine.energyCapacity;
		machine.upgradeInventory.setStackInSlot(0, new ItemStack(FTBICItems.ENERGY_STORAGE_UPGRADE.get(), 4));
		helper.assertTrue(machine.energyCapacity > baseCapacity, "Storage upgrades raise capacity");
		machine.energy = machine.energyCapacity;
		double stored = machine.energy;

		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		CompoundTag tag = machine.saveCustomOnly(registries);
		helper.setBlock(POS, Blocks.AIR);
		helper.setBlock(POS, FTBICElectricBlocks.MACERATOR.block.get());
		BasicMachineBlockEntity reloaded = helper.getBlockEntity(POS, BasicMachineBlockEntity.class);
		reloaded.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
		helper.assertValueEqual(stored, reloaded.energy, "Energy above the base capacity survives a reload");
		helper.succeed();
	}

	static void configurationCardSyncsEnchantedItems(GameTestHelper helper) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		sword.enchant(helper.getLevel().holderOrThrow(Enchantments.SHARPNESS), 3);
		MachineConfiguration configuration = new MachineConfiguration(FTBIC.id("macerator"), SideConfiguration.DEFAULT,
				List.of(GhostItem.of(0, sword, 1)), List.of(), SimpleFluidContent.EMPTY);
		var codec = ModDataComponents.MACHINE_CONFIGURATION.get().streamCodec();
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		try {
			codec.encode(buffer, configuration);
			helper.assertValueEqual(configuration, codec.decode(buffer), "Configuration Cards with enchanted items survive network sync");
		} finally {
			buffer.release();
		}
		helper.succeed();
	}

	static void recipeTogglesFollowConfig(GameTestHelper helper) {
		Map<String, ModConfigSpec.BooleanValue> toggles = Map.of(
				"add_dust_from_ore_recipes", FTBICConfig.RECIPES.ADD_DUST_FROM_ORE_RECIPES,
				"add_dust_from_material_recipes", FTBICConfig.RECIPES.ADD_DUST_FROM_MATERIAL_RECIPES,
				"add_gem_from_ore_recipes", FTBICConfig.RECIPES.ADD_GEM_FROM_ORE_RECIPES,
				"add_rod_recipes", FTBICConfig.RECIPES.ADD_ROD_RECIPES,
				"add_plate_recipes", FTBICConfig.RECIPES.ADD_PLATE_RECIPES,
				"add_gear_recipes", FTBICConfig.RECIPES.ADD_GEAR_RECIPES,
				"add_canned_food_recipes", FTBICConfig.RECIPES.ADD_CANNED_FOOD_RECIPES);
		toggles.forEach((option, value) -> helper.assertValueEqual(value.get(),
				new RecipeToggleCondition(option).test(ICondition.IContext.EMPTY), option + " condition follows the config"));
		helper.assertTrue(new RecipeToggleCondition("not_a_real_option").test(ICondition.IContext.EMPTY), "Unknown options keep recipes enabled");

		Map<String, ModConfigSpec.BooleanValue> samples = Map.of(
				"macerating/ingots/iron_to_dust", FTBICConfig.RECIPES.ADD_DUST_FROM_MATERIAL_RECIPES,
				"macerating/ores/diamond_to_dust", FTBICConfig.RECIPES.ADD_GEM_FROM_ORE_RECIPES,
				"extruding/ingots/iron_to_iron_rod", FTBICConfig.RECIPES.ADD_ROD_RECIPES,
				"rolling/ingots/iron_to_iron_plate", FTBICConfig.RECIPES.ADD_PLATE_RECIPES,
				"extruding/plates/iron_to_iron_gear", FTBICConfig.RECIPES.ADD_GEAR_RECIPES,
				"canning/apple", FTBICConfig.RECIPES.ADD_CANNED_FOOD_RECIPES);
		RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
		samples.forEach((path, value) -> helper.assertValueEqual(value.get(),
				recipes.byKey(ResourceKey.create(Registries.RECIPE, FTBIC.id(path))).isPresent(), path + " loads only when its toggle is on"));
		helper.succeed();
	}
}
