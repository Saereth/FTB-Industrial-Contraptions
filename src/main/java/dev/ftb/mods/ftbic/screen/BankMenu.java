package dev.ftb.mods.ftbic.screen;

import dev.ftb.mods.ftbic.block.entity.ElectricBlockEntity;
import dev.ftb.mods.ftbic.block.entity.storage.BankPortBlockEntity;
import dev.ftb.mods.ftbic.block.entity.storage.BankTopology;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;

public class BankMenu extends ElectricBlockMenu {
	@Override
	protected int getPlayerSlotOffset() {
		return 134;
	}

	@Override
	protected void addMachineSlots(Inventory playerInv) {
		machineSlotCount = 0;
		if (blockEntity instanceof BankPortBlockEntity port) {
			for (int i = 0; i < port.chargeSlots.size(); i++) {
				addChargeSlot(port.chargeSlots.get(i), 53 + i * 18, 99);
			}
		}
	}

	public boolean hasChargeSlots() {
		return blockEntity instanceof BankPortBlockEntity;
	}
	public final DataSlot cells = DataSlot.standalone();
	public final DataSlot ports = DataSlot.standalone();
	private final DataSlot storedLow = DataSlot.standalone();
	private final DataSlot storedHigh = DataSlot.standalone();
	private final DataSlot capacityLow = DataSlot.standalone();
	private final DataSlot capacityHigh = DataSlot.standalone();

	public BankMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
		super(FTBICMenus.BANK.get(), id, inventory, buffer);
		addBankSlots();
	}

	public BankMenu(int id, Inventory inventory, ElectricBlockEntity entity) {
		super(FTBICMenus.BANK.get(), id, inventory, entity);
		addBankSlots();
	}

	private void addBankSlots() {
		addDataSlot(cells);
		addDataSlot(ports);
		addDataSlot(storedLow);
		addDataSlot(storedHigh);
		addDataSlot(capacityLow);
		addDataSlot(capacityHigh);
	}

	@Override
	public void broadcastChanges() {
		if (blockEntity != null && blockEntity.getLevel() != null) {
			BankTopology.Snapshot snapshot = BankTopology.snapshot(blockEntity.getLevel(), blockEntity.getBlockPos());
			cells.set(snapshot.cells());
			ports.set(snapshot.ports());
			long storedBits = Double.doubleToRawLongBits(snapshot.stored());
			long capacityBits = Double.doubleToRawLongBits(snapshot.capacity());
			storedLow.set((int) storedBits);
			storedHigh.set((int) (storedBits >>> 32));
			capacityLow.set((int) capacityBits);
			capacityHigh.set((int) (capacityBits >>> 32));
		}
		super.broadcastChanges();
	}

	public double stored() {
		return Double.longBitsToDouble((Integer.toUnsignedLong(storedHigh.get()) << 32) | Integer.toUnsignedLong(storedLow.get()));
	}

	public double capacity() {
		return Double.longBitsToDouble((Integer.toUnsignedLong(capacityHigh.get()) << 32) | Integer.toUnsignedLong(capacityLow.get()));
	}
}
