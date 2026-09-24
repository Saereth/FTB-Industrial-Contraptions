package dev.ftb.mods.ftbic.test;

import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.block.entity.machine.BasicMachineBlockEntity;
import dev.ftb.mods.ftbic.item.FTBICItems;
import dev.ftb.mods.ftbic.screen.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.OVERCLOCKER_UPGRADE.get()), "Shift-click inserts only four overclockers");
		h.assertValueEqual(8, player.getInventory().getItem(9).getCount(), "Excess upgrades remain in the player's stack");
		menu.quickMoveStack(player, playerSlot);
		h.assertValueEqual(8, player.getInventory().getItem(9).getCount(), "Further shift-clicks cannot fill other upgrade slots");
		player.getInventory().setItem(10, new ItemStack(FTBICItems.TRANSFORMER_UPGRADE.get(), 4));
		menu.quickMoveStack(player, playerSlot + 1);
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.TRANSFORMER_UPGRADE.get()), "Transformers have their own four-upgrade limit");
		h.assertValueEqual(0, player.getInventory().getItem(10).getCount(), "Different upgrade types can coexist");
		h.assertValueEqual(0, machine.upgradeInventory.getSlotLimit(2, new ItemStack(FTBICItems.TRANSFORMER_UPGRADE.get())), "A fifth transformer cannot enter another slot");
		player.getInventory().setItem(11, new ItemStack(FTBICItems.EJECTOR_UPGRADE.get(), 4));
		menu.quickMoveStack(player, playerSlot + 2);
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.EJECTOR_UPGRADE.get()), "Ejectors can coexist with full transformer and overclocker stacks");
		h.succeed();
	}

	static void sneakInsert(GameTestHelper h) {
		h.setBlock(POS, FTBICElectricBlocks.MACERATOR.block.get());
		var machine = h.getBlockEntity(POS, BasicMachineBlockEntity.class);
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		player.setShiftKeyDown(true);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FTBICItems.OVERCLOCKER_UPGRADE.get(), 6));
		useHeldUpgrade(machine, player);
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.OVERCLOCKER_UPGRADE.get()), "Sneak-use inserts a stack of four at once");
		h.assertValueEqual(2, player.getMainHandItem().getCount(), "Sneak-use consumes only installed upgrades");
		useHeldUpgrade(machine, player);
		h.assertValueEqual(2, player.getMainHandItem().getCount(), "Full machine rejects extra upgrades without consuming them");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FTBICItems.TRANSFORMER_UPGRADE.get(), 4));
		useHeldUpgrade(machine, player);
		h.assertValueEqual(4, machine.upgradeInventory.countUpgrades(FTBICItems.TRANSFORMER_UPGRADE.get()), "Sneak-use accepts four of a different upgrade type");
		h.assertTrue(player.getMainHandItem().isEmpty(), "Mixed insertion consumes the installed stack");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FTBICItems.PARALLEL_PROCESSING_UPGRADE.get(), 3));
		useHeldUpgrade(machine, player);
		h.assertValueEqual(3, player.getMainHandItem().getCount(), "Unsupported upgrades are not consumed");
		h.succeed();
	}

	private static void useHeldUpgrade(BasicMachineBlockEntity machine, Player player) {
		BlockPos pos = machine.getBlockPos();
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		player.getMainHandItem().onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
	}

	private UpgradeInventoryGameTests() {}
}
