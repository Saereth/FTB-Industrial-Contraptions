package dev.ftb.mods.ftbic.world;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ResourceElements {
    TIN,
    DEEPSLATE_TIN,
    LEAD,
    DEEPSLATE_LEAD,
    URANIUM,
    DEEPSLATE_URANIUM,
    IRIDIUM,
    DEEPSLATE_IRIDIUM,
    ALUMINUM,
    DEEPSLATE_ALUMINUM,
    ENDERIUM(Requirements.builder().all().remove(ResourceType.ORE).remove(ResourceType.CHUNK)),
	DIAMOND(Requirements.builder().add(ResourceType.DUST));

    public static final List<ResourceElements> VALUES = Arrays.stream(ResourceElements.values()).toList();
    public static final List<ResourceElements> NO_DEEPSLATE_VALUES = VALUES.stream().filter(e -> !e.getName().contains("deepslate_")).toList();

	/**
	 * Creates a map of the resource types which link together the elements that the resource contains.
	 * For example, if you get the {@link ResourceType#ORE} from the list, you will not get an ore version of {@link ResourceElements#ENDERIUM}
	 */
	public static final Map<ResourceType, List<ResourceElements>> RESOURCES_BY_REQUIREMENT = ResourceType.VALUES.stream().collect(Collectors.toMap(Function.identity(), e -> NO_DEEPSLATE_VALUES.stream().filter(a -> a.requirements.has(e)).collect(Collectors.toList())));

    private final Requirements requirements;

    ResourceElements(Requirements requirements) {
        this.requirements = requirements;
    }

    ResourceElements() {
        this(Requirements.builder().all());
    }

    public String getName() {
        return name().toLowerCase();
    }

	public Requirements requirements() {
		return requirements;
	}

	/**
	 * Stores the requirements of the resource, you can use this to remove and add different resources.
	 * Use an 8 bit space to store all the resource requirements, this is kind overkill... but I kinda love it.
	 *
	 * @implNote defaults to no requirements! We also don't allow users to do this as OR order is important here.
	 */
	public static class Requirements {
		private int requirements = 0; // Start with nothing

		private Requirements() {
		}

		/**
		 * Use AND to check if we have the correct bite
		 */
		public boolean has(ResourceType type) {
			return (requirements & type.bit) != 0;
		}

		public static Requirements builder() {
			return new Requirements();
		}

		/**
		 * OR every resource, so we have them all, simple math 1 + 2 + 4 + 8 + 16 + 32 + 64 + 128 = 255 (8 bits)
		 */
		public Requirements all() {
			this.requirements = ResourceType.ORE.bit | ResourceType.INGOT.bit | ResourceType.CHUNK.bit | ResourceType.DUST.bit | ResourceType.NUGGET.bit | ResourceType.PLATE.bit | ResourceType.ROD.bit | ResourceType.GEAR.bit;
			return this;
		}

		/**
		 * OR the bit of the type
		 */
		public Requirements add(ResourceType type) {
			this.requirements |= type.bit;
			return this;
		}

		/**
		 * NOT the bit of the type to remove it
		 */
		public Requirements remove(ResourceType type) {
			this.requirements &= ~type.bit;
			return this;
		}
	}
}
