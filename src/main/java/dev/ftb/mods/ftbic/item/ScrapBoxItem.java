package dev.ftb.mods.ftbic.item;

import dev.ftb.mods.ftbic.FTBIC;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class ScrapBoxItem extends Item {
	private static final ResourceKey<LootTable> REWARDS =
			ResourceKey.create(Registries.LOOT_TABLE, FTBIC.id("gameplay/scrap_box"));

	public ScrapBoxItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

		LootParams params = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN, player.position())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.create(LootContextParamSets.GIFT);
		List<ItemStack> rewards = serverLevel.getServer().reloadableRegistries()
				.getLootTable(REWARDS).getRandomItems(params);
		if (rewards.isEmpty()) return InteractionResult.PASS;

		player.getItemInHand(hand).consume(1, player);
		for (ItemStack reward : rewards) {
			if (!player.addItem(reward)) player.drop(reward, false);
		}
		return InteractionResult.SUCCESS;
	}
}
