package dev.ftb.mods.ftbic.util;

import dev.ftb.mods.ftbic.block.entity.machine.HydroponicBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class HydroponicWaterHandler extends SnapshotJournal<FluidStack> implements ResourceHandler<FluidResource> {
	private final HydroponicBlockEntity machine;

	public HydroponicWaterHandler(HydroponicBlockEntity machine) { this.machine = machine; }
	@Override public int size() { return 1; }
	@Override public FluidResource getResource(int index) { return index == 0 ? FluidResource.of(machine.getInputFluid()) : FluidResource.EMPTY; }
	@Override public long getAmountAsLong(int index) { return index == 0 ? machine.getInputFluid().getAmount() : 0; }
	@Override public long getCapacityAsLong(int index, FluidResource resource) { return index == 0 ? machine.getTankCapacity() : 0; }
	@Override public boolean isValid(int index, FluidResource resource) { return index == 0 && !resource.isEmpty(); }

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (!isValid(index, resource) || amount <= 0) return 0;
		FluidStack fluid = machine.getInputFluid();
		if (!fluid.isEmpty() && !FluidResource.of(fluid).equals(resource)) return 0;
		int accepted = Math.min(amount, machine.getTankCapacity() - fluid.getAmount());
		if (accepted <= 0) return 0;
		updateSnapshots(transaction);
		machine.setInputFluid(resource.toStack(fluid.getAmount() + accepted));
		return accepted;
	}

	@Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) { return 0; }
	@Override protected FluidStack createSnapshot() { return machine.getInputFluid(); }
	@Override protected void revertToSnapshot(FluidStack snapshot) { machine.setInputFluid(snapshot); }
	@Override protected void onRootCommit(FluidStack original) { machine.fluidChanged(); }
}
