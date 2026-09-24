package dev.ftb.mods.ftbic.integration.jade;

import dev.ftb.mods.ftbic.FTBIC;
import dev.ftb.mods.ftbic.block.entity.storage.BankCellBlockEntity;
import dev.ftb.mods.ftbic.block.entity.storage.BankPortBlockEntity;
import dev.ftb.mods.ftbic.block.entity.storage.BankTopology;
import dev.ftb.mods.ftbic.util.ZapFEConversion;
import net.minecraft.resources.Identifier;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public final class BankEnergyStorageProvider implements IServerExtensionProvider<EnergyView.Data>,
		IClientExtensionProvider<EnergyView.Data, EnergyView> {
	public static final BankEnergyStorageProvider INSTANCE = new BankEnergyStorageProvider();
	private static final Identifier UID = FTBIC.id("bank_energy_storage");

	@Override
	public Identifier getUid() {
		return UID;
	}

	@Override
	public List<ViewGroup<EnergyView.Data>> getGroups(Accessor<?> accessor) {
		if (!(accessor instanceof BlockAccessor block)
				|| !(block.getBlockEntity() instanceof BankCellBlockEntity || block.getBlockEntity() instanceof BankPortBlockEntity)) {
			return null;
		}
		BankTopology.Snapshot bank = BankTopology.snapshot(block.getLevel(), block.getPosition());
		if (bank.capacity() <= 0D) return List.of();
		double rate = ZapFEConversion.rate();
		// Jade supports long totals; the transaction conversion helper caps at a 32-bit FE offer.
		return List.of(new ViewGroup<>(List.of(new EnergyView.Data(
				(long) (bank.stored() * rate), (long) (bank.capacity() * rate)))));
	}

	@Override
	public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<EnergyView.Data>> groups) {
		return groups.stream().map(group -> new ClientViewGroup<>(group.views.stream()
				.map(data -> EnergyView.read(data, "FE")).toList())).toList();
	}
}
