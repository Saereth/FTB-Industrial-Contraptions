package dev.ftb.mods.ftbic.test;

import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.block.entity.machine.BasicMachineBlockEntity;
import dev.ftb.mods.ftbic.item.FTBICItems;
import dev.ftb.mods.ftbic.screen.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

final class UpgradeInventoryGameTests {
	private static final BlockPos POS = new BlockPos(2, 2, 2);

	static void shiftClickLimit(GameTestHelper h) {
		h.setBlock(POS, FTBICElectricBlocks.MACERATOR.block.get());
		var machine = h.getBlockEntity(POS, BasicMachineBlockEntity.class);
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		var menu = new MachineMenu(1, player.getInventory(), machine);
		player.getInventory().setItem(9, new ItemStack(FTBICItems.OVERCLOCKER_UPGRADE.get(), 12));
		int playerSlot = menu.slots.size() - 36;
		menu.quickMoveStack(player, playerSlot);
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.OVERCLOCKER_UPGRADE.get()), "Shift-click inserts only four upgrades total");
		h.assertValueEqual(8, player.getInventory().getItem(9).getCount(), "Excess upgrades remain in the player's stack");
		menu.quickMoveStack(player, playerSlot);
		h.assertValueEqual(8, player.getInventory().getItem(9).getCount(), "Further shift-clicks cannot fill other upgrade slots");
		player.getInventory().setItem(10, new ItemStack(FTBICItems.TRANSFORMER_UPGRADE.get(), 4));
		menu.quickMoveStack(player, playerSlot + 1);
		h.assertValueEqual(4, player.getInventory().getItem(10).getCount(), "Mixed upgrades share the four-upgrade capacity");
		h.assertValueEqual(0, machine.upgradeInventory.getSlotLimit(1, new ItemStack(FTBICItems.TRANSFORMER_UPGRADE.get())), "Other slots expose no space when full");
		machine.upgradeInventory.setStackInSlot(0, new ItemStack(FTBICItems.OVERCLOCKER_UPGRADE.get(), 2));
		menu.quickMoveStack(player, playerSlot + 1);
		h.assertValueEqual(2, machine.upgradeInventory.countUpgrades(FTBICItems.TRANSFORMER_UPGRADE.get()), "Freed capacity accepts only two transformers");
		h.assertValueEqual(2, player.getInventory().getItem(10).getCount(), "Partial insertion keeps the remaining transformers");
		h.succeed();
	}

	static void sneakInsert(GameTestHelper h) {
		h.setBlock(POS, FTBICElectricBlocks.MACERATOR.block.get());
		var machine = h.getBlockEntity(POS, BasicMachineBlockEntity.class);
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setShiftKeyDown(true);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FTBICItems.OVERCLOCKER_UPGRADE.get(), 6));
		h.useBlock(POS, player);
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.OVERCLOCKER_UPGRADE.get()), "Sneak-use inserts a stack of four at once");
		h.assertValueEqual(2, player.getMainHandItem().getCount(), "Sneak-use consumes only installed upgrades");
		h.useBlock(POS, player);
		h.assertValueEqual(2, player.getMainHandItem().getCount(), "Full machine rejects extra upgrades without consuming them");
		machine.upgradeInventory.setStackInSlot(0, new ItemStack(FTBICItems.OVERCLOCKER_UPGRADE.get(), 2));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FTBICItems.TRANSFORMER_UPGRADE.get(), 4));
		h.useBlock(POS, player);
		h.assertValueEqual(2, machine.upgradeInventory.countUpgrades(FTBICItems.TRANSFORMER_UPGRADE.get()), "Sneak-use fills remaining capacity with another upgrade type");
		h.assertValueEqual(2, player.getMainHandItem().getCount(), "Mixed insertion consumes only available space");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FTBICItems.PARALLEL_PROCESSING_UPGRADE.get(), 3));
		h.useBlock(POS, player);
		h.assertValueEqual(3, player.getMainHandItem().getCount(), "Unsupported upgrades are not consumed");
		h.succeed();
	}

	private UpgradeInventoryGameTests() {}
}
