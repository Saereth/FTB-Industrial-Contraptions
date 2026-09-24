package dev.ftb.mods.ftbic.util;

import dev.ftb.mods.ftbic.FTBICConfig;
import dev.ftb.mods.ftbic.block.CableBlock;
import dev.ftb.mods.ftbic.block.entity.SuperconductingCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Stateless FE input: the receiver participates in the source's transaction. */
public final class CableFEHandler implements EnergyHandler {
	// External handlers may forward back into this network synchronously.
	private static final Set<ServerLevel> TRANSFERRING = new HashSet<>();
	private final ServerLevel level;
	private final BlockPos pos;
	private final Direction side;

	public CableFEHandler(ServerLevel level, BlockPos pos, Direction side) {
		this.level = level;
		this.pos = pos.immutable();
		this.side = side;
	}

	@Override
	public long getAmountAsLong() { return 0; }

	@Override
	public long getCapacityAsLong() {
		return level.getBlockState(pos).getBlock() instanceof CableBlock cable
				? ZapFEConversion.zapsToFEFloor(cable.tier.transferRate()) : 0;
	}

	@Override
	public int extract(int amount, TransactionContext transaction) { return 0; }

	@Override
	public int insert(int amount, TransactionContext transaction) {
		if (amount <= 0 || !FTBICConfig.ENERGY.FULL_FE_MODE.get()
				|| !(level.getBlockState(pos).getBlock() instanceof CableBlock cable)
				|| !TRANSFERRING.add(level)) return 0;
		try {
			int remaining = Math.min(amount, ZapFEConversion.zapsToFEFloor(cable.tier.transferRate()));
			int offered = remaining;
			Set<BlockPos> visited = new HashSet<>();
			Set<BlockPos> receivers = new HashSet<>();
			if (side != null) receivers.add(pos.relative(side));
			ArrayDeque<Route> queue = new ArrayDeque<>();
			queue.add(new Route(pos, 1, List.of()));
			visited.add(pos);
			int maxLength = FTBICConfig.ENERGY.MAX_CABLE_LENGTH.get();
			while (!queue.isEmpty() && remaining > 0) {
				Route route = queue.removeFirst();
				if (route.distance > maxLength || !level.hasChunkAt(route.pos)) continue;
				var state = level.getBlockState(route.pos);
				List<BlockPos> path = route.path;
				if (level.getBlockEntity(route.pos) instanceof SuperconductingCableBlockEntity) {
					path = new ArrayList<>(path);
					path.add(route.pos);
				}
				for (Direction direction : Direction.values()) {
					if (!state.getValue(CableBlock.CONNECTION[direction.ordinal()])) continue;
					BlockPos neighbor = route.pos.relative(direction);
					if (!level.hasChunkAt(neighbor)) continue;
					if (level.getBlockState(neighbor).getBlock() instanceof CableBlock next) {
						if (next.tier == cable.tier && visited.add(neighbor)) {
							queue.addLast(new Route(neighbor, route.distance + 1, path));
						}
						continue;
					}
					if (receivers.contains(neighbor)) continue;
					Direction face = direction.getOpposite();
					EnergyHandler receiver = level.getCapability(Capabilities.Energy.BLOCK, neighbor, face);
					if (receiver == null) continue;
					int limit = remaining;
					// Respect native input limits without overvolting machines during FE insertion.
					ZapEnergyHandler zap = level.getCapability(FTBICCapabilities.ZAP_ENERGY_BLOCK, neighbor, face);
					if (zap instanceof SidedZapHandler nativeHandler && nativeHandler.machine().canBurn()) {
						limit = Math.min(limit, ZapFEConversion.zapsToFEFloor(nativeHandler.machine().getMaxInputEnergy()));
					}
					int accepted = receiver.insert(limit, transaction);
					if (accepted > 0) {
						receivers.add(neighbor);
						remaining -= accepted;
						new TransferPulse(level, path).updateSnapshots(transaction);
					}
					if (remaining == 0) break;
				}
			}
			return offered - remaining;
		} finally {
			TRANSFERRING.remove(level);
		}
	}

	private record Route(BlockPos pos, int distance, List<BlockPos> path) {}

	private static final class TransferPulse extends SnapshotJournal<Boolean> {
		private final ServerLevel level;
		private final List<BlockPos> path;

		private TransferPulse(ServerLevel level, List<BlockPos> path) {
			this.level = level;
			this.path = List.copyOf(path);
		}

		@Override protected Boolean createSnapshot() { return false; }
		@Override protected void revertToSnapshot(Boolean snapshot) {}
		@Override protected void onRootCommit(Boolean snapshot) {
			for (BlockPos pos : path) {
				if (level.getBlockEntity(pos) instanceof SuperconductingCableBlockEntity cable) cable.recordTransfer();
			}
		}
	}
}
