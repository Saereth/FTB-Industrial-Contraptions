package dev.ftb.mods.ftbic.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record MachineConfiguration(Identifier machine, SideConfiguration sides) {
	public static final Codec<MachineConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
			Identifier.CODEC.fieldOf("machine").forGetter(MachineConfiguration::machine),
			SideConfiguration.CODEC.fieldOf("sides").forGetter(MachineConfiguration::sides)
	).apply(i, MachineConfiguration::new));
}
