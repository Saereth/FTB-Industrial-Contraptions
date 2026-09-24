package dev.ftb.mods.ftbic.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuperconductingCableBlockEntity extends BlockEntity {
	public static final int PULSE_DURATION = 16;
	private static final int SYNC_INTERVAL = 8;
	private long lastTransferTick = Long.MIN_VALUE;
	private long lastSyncTick = Long.MIN_VALUE;

	public SuperconductingCableBlockEntity(BlockPos pos, BlockState state) {
		super(FTBICBlockEntities.SUPERCONDUCTING_CABLE.get(), pos, state);
	}

	public void recordTransfer() {
		if (level == null || level.isClientSide()) return;
		long now = level.getGameTime();
		lastTransferTick = now;
		if (lastSyncTick == Long.MIN_VALUE || now - lastSyncTick >= SYNC_INTERVAL) {
			lastSyncTick = now;
			level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
		}
	}

	public boolean isTransferring() {
		return level != null && lastTransferTick != Long.MIN_VALUE
				&& level.getGameTime() - lastTransferTick < PULSE_DURATION;
	}

	public float pulseStrength(float partialTick) {
		if (!isTransferring()) return 0F;
		return Math.clamp((PULSE_DURATION - (level.getGameTime() - lastTransferTick) - partialTick) / 4F, 0F, 1F);
	}

	@Override
	public boolean triggerEvent(int id, int parameter) {
		if (id != 0 || level == null) return false;
		if (level.isClientSide()) lastTransferTick = level.getGameTime();
		return true;
	}
}
