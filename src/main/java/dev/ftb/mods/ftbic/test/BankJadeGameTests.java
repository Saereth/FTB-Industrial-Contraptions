package dev.ftb.mods.ftbic.test;

import dev.ftb.mods.ftbic.block.FTBICElectricBlocks;
import dev.ftb.mods.ftbic.block.entity.storage.BankCellBlockEntity;
import dev.ftb.mods.ftbic.block.entity.storage.BankPortBlockEntity;
import dev.ftb.mods.ftbic.integration.jade.BankEnergyStorageProvider;
import dev.ftb.mods.ftbic.util.ZapFEConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.addon.universal.EnergyStorageProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.EnergyView;

import java.lang.reflect.Proxy;

final class BankJadeGameTests {
	static void combinedEnergyBar(GameTestHelper helper) {
		BlockPos portPos = new BlockPos(3, 2, 3);
		helper.setBlock(portPos, FTBICElectricBlocks.INDUSTRIAL_BANK_PORT.block.get());
		helper.setBlock(portPos.west(), FTBICElectricBlocks.INDUSTRIAL_BANK_CELL.block.get());
		helper.setBlock(portPos.east(), FTBICElectricBlocks.INDUSTRIAL_BANK_CELL.block.get());
		BankPortBlockEntity port = helper.getBlockEntity(portPos, BankPortBlockEntity.class);
		BankCellBlockEntity west = helper.getBlockEntity(portPos.west(), BankCellBlockEntity.class);
		BankCellBlockEntity east = helper.getBlockEntity(portPos.east(), BankCellBlockEntity.class);
		west.energyCapacity = 1_000_000_000D;
		east.energyCapacity = 1_000_000_000D;
		west.setEnergyRaw(west.getEnergyCapacity());
		double rate = ZapFEConversion.rate();
		long capacity = (long) ((west.getEnergyCapacity() + east.getEnergyCapacity() + port.getEnergyCapacity()) * rate);
		for (BlockPos pos : new BlockPos[]{portPos, portPos.west(), portPos.east()}) {
			EnergyView.Data data = jadeData(helper, pos);
			helper.assertValueEqual(capacity, data.capacity(), "Jade bar capacity includes every cell and port without 32-bit clipping");
			helper.assertValueEqual((long) (west.getEnergy() * rate), data.current(), "empty port and empty cell show the bank's stored charge");
		}
		helper.setBlock(portPos.east(), Blocks.AIR);
		port.setEnergyRaw(port.getEnergyCapacity());
		EnergyView.Data full = jadeData(helper, portPos);
		helper.assertValueEqual((long) ((west.getEnergyCapacity() + port.getEnergyCapacity()) * rate), full.capacity(), "splitting the bank updates Jade's capacity");
		helper.assertValueEqual(full.capacity(), full.current(), "full bank fills the whole bar");
		port.setEnergyRaw(0D);
		west.setEnergyRaw(0D);
		helper.assertValueEqual(0L, jadeData(helper, portPos).current(), "empty bank empties the bar");
		helper.succeed();
	}

	private static EnergyView.Data jadeData(GameTestHelper helper, BlockPos relative) {
		BlockPos pos = helper.absolutePos(relative);
		BlockAccessor accessor = (BlockAccessor) Proxy.newProxyInstance(BlockAccessor.class.getClassLoader(),
				new Class<?>[]{BlockAccessor.class}, (proxy, method, arguments) -> switch (method.getName()) {
					case "getLevel" -> helper.getLevel();
					case "getPosition" -> pos;
					case "getBlock" -> helper.getLevel().getBlockState(pos).getBlock();
					case "getBlockState" -> helper.getLevel().getBlockState(pos);
					case "getBlockEntity", "getTarget" -> helper.getLevel().getBlockEntity(pos);
					case "getAccessorType" -> BlockAccessor.class;
					case "getSide" -> Direction.NORTH;
					case "getServerData" -> new CompoundTag();
					case "isServersideContent", "showDetails" -> true;
					default -> throw new UnsupportedOperationException(method.getName());
				});
		var result = EnergyStorageProvider.BLOCK.streamData(accessor);
		helper.assertTrue(result != null, "Jade finds an energy provider for the bank");
		helper.assertValueEqual(BankEnergyStorageProvider.INSTANCE.getUid(), result.getKey(), "bank provider takes precedence over the port's FE capability");
		return result.getValue().getFirst().views.getFirst();
	}
}
