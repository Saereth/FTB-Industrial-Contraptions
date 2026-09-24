package dev.ftb.mods.ftbic.util;

import dev.ftb.mods.ftbic.block.entity.machine.FluidMachineBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Tank 0 accepts input; tank 1 only exposes recipe output. One journal is shared by every face. */
public class FluidMachineTankHandler extends SnapshotJournal<FluidMachineTankHandler.Contents> implements ResourceHandler<FluidResource> {
	private final FluidMachineBlockEntity machine;
	public record Contents(FluidStack input, FluidStack output) {}

	public FluidMachineTankHandler(FluidMachineBlockEntity machine) { this.machine = machine; }
	@Override public int size() { return 2; }
	private FluidStack stack(int index) { return index == 0 ? machine.getInputFluid() : index == 1 ? machine.getOutputFluid() : FluidStack.EMPTY; }
	@Override public FluidResource getResource(int index) { return FluidResource.of(stack(index)); }
	@Override public long getAmountAsLong(int index) { return stack(index).getAmount(); }
	@Override public long getCapacityAsLong(int index, FluidResource resource) { return index >= 0 && index < 2 ? FluidMachineBlockEntity.TANK_CAPACITY : 0; }
	@Override public boolean isValid(int index, FluidResource resource) { return index == 0; }

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (index != 0 || resource.isEmpty() || amount <= 0) return 0;
		FluidStack input = machine.getInputFluid();
		if (!input.isEmpty() && !FluidResource.of(input).equals(resource)) return 0;
		int inserted = Math.min(amount, FluidMachineBlockEntity.TANK_CAPACITY - input.getAmount());
		if (inserted <= 0) return 0;
		updateSnapshots(transaction);
		machine.setFluids(resource.toStack(input.getAmount() + inserted), machine.getOutputFluid());
		return inserted;
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		if (index != 1 || resource.isEmpty() || amount <= 0) return 0;
		FluidStack output = machine.getOutputFluid();
		if (!FluidResource.of(output).equals(resource)) return 0;
		int extracted = Math.min(amount, output.getAmount());
		if (extracted <= 0) return 0;
		updateSnapshots(transaction);
		output.shrink(extracted);
		machine.setFluids(machine.getInputFluid(), output);
		return extracted;
	}

	@Override protected Contents createSnapshot() { return new Contents(machine.getInputFluid(), machine.getOutputFluid()); }
	@Override protected void revertToSnapshot(Contents contents) { machine.setFluids(contents.input(), contents.output()); }
	@Override protected void onRootCommit(Contents original) { machine.fluidsChanged(); }
}
