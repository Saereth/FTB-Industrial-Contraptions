package dev.ftb.mods.ftbic.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;

public record MachineConfiguration(Identifier machine, SideConfiguration sides, List<GhostItem> inputLocks, List<GhostItem> batch, SimpleFluidContent batchFluid) {
	public MachineConfiguration {
		inputLocks = List.copyOf(inputLocks);
		batch = List.copyOf(batch);
	}

	public MachineConfiguration(Identifier machine, SideConfiguration sides) {
		this(machine, sides, List.of(), List.of(), SimpleFluidContent.EMPTY);
	}

	public static final Codec<MachineConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
			Identifier.CODEC.fieldOf("machine").forGetter(MachineConfiguration::machine),
			SideConfiguration.CODEC.fieldOf("sides").forGetter(MachineConfiguration::sides),
			GhostItem.LIST_CODEC.optionalFieldOf("input_locks", List.of()).forGetter(MachineConfiguration::inputLocks),
			GhostItem.LIST_CODEC.optionalFieldOf("batch", List.of()).forGetter(MachineConfiguration::batch),
			SimpleFluidContent.CODEC.optionalFieldOf("batch_fluid", SimpleFluidContent.EMPTY).forGetter(MachineConfiguration::batchFluid)
	).apply(i, MachineConfiguration::new));
}
