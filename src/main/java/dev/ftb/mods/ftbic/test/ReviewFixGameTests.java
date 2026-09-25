package dev.ftb.mods.ftbic.test;

import dev.ftb.mods.ftbic.FTBIC;
import dev.ftb.mods.ftbic.FTBICConfig;
import dev.ftb.mods.ftbic.block.ElectricBlockInstance;
import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.block.entity.machine.BasicMachineBlockEntity;
import dev.ftb.mods.ftbic.item.BatteryItem;
import dev.ftb.mods.ftbic.item.FTBICItems;
import dev.ftb.mods.ftbic.registry.ModDataComponents;
import dev.ftb.mods.ftbic.util.GhostItem;
import dev.ftb.mods.ftbic.util.MachineConfiguration;
import dev.ftb.mods.ftbic.util.SideConfiguration;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;

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
}
