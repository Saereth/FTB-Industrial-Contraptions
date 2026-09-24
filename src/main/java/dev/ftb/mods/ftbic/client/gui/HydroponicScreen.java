package dev.ftb.mods.ftbic.client.gui;

import dev.ftb.mods.ftbic.block.entity.machine.HydroponicBlockEntity;
import dev.ftb.mods.ftbic.integration.jei.ClientRecipeCache;
import dev.ftb.mods.ftbic.recipe.FTBICRecipes;
import dev.ftb.mods.ftbic.screen.HydroponicMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public final class HydroponicScreen extends ElectricBlockScreen<HydroponicMenu> {
	private final boolean advanced;
	private Button modeButton;
	public HydroponicScreen(HydroponicMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 248,
				menu.blockEntity instanceof HydroponicBlockEntity machine && machine.isAdvanced() ? 233 : 198);
		advanced = menu.blockEntity instanceof HydroponicBlockEntity machine && machine.isAdvanced();
		drawDefaultArrow = false;
		energyX = 8;
		energyY = 33;
	}

	@Override protected void init() {
		super.init();
		inventoryLabelX = 42;
		inventoryLabelY = advanced ? 140 : 105;
		if (advanced) {
			modeButton = addRenderableWidget(Button.builder(Component.translatable(menu.mutationMode() ? "ftbic.hydro.mutation" : "ftbic.hydro.growth"), button -> {
				minecraft.getConnection().send(new ServerboundContainerButtonClickPacket(menu.containerId, 0));
			}).bounds(leftPos + 143, topPos + 121, 71, 18).build(IndustrialButton::new));
		}
	}

	@Override protected void drawBase(GuiGraphicsExtractor g) {
		IndustrialGui.panel(g, leftPos, topPos, imageWidth, imageHeight);
		IndustrialGui.readout(g, leftPos + 29, topPos + 29, 188, advanced ? 92 : 70);
		int inventoryY = advanced ? 150 : 115;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) drawSlot(g, leftPos + 41 + col * 18, topPos + inventoryY + row * 18);
		}
		for (int col = 0; col < 9; col++) drawSlot(g, leftPos + 41 + col * 18, topPos + (advanced ? 208 : 173));
	}

	@Override protected void extractOverlays(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		if (!(menu.blockEntity instanceof HydroponicBlockEntity machine)) return;
		if (modeButton != null) modeButton.setMessage(Component.translatable(menu.mutationMode() ? "ftbic.hydro.mutation" : "ftbic.hydro.growth"));
		int lanes = machine.isAdvanced() ? 4 : 1;
		for (int lane = 0; lane < lanes; lane++) {
			int y = topPos + (machine.isAdvanced() ? 35 + lane * 22 : 56);
			if (machine.isAdvanced()) g.text(font, Component.literal(Integer.toString(lane + 1)),
					leftPos + 34, y + 4, IndustrialGui.CYAN, false);
			int seedX = leftPos + 48;
			drawSlot(g, seedX - 1, y - 1);
			drawSlot(g, seedX + 19, y - 1);
			if (machine.isAdvanced()) {
				for (int kind = 0; kind < 3; kind++) drawSlot(g, leftPos + 148 + kind * 19, y - 1);
			} else for (int kind = 0; kind < 3; kind++) drawSlot(g, leftPos + 157 + kind * 19, y - 1);
			int barX = leftPos + (machine.isAdvanced() ? 94 : 108);
			int barWidth = machine.isAdvanced() ? 41 : 25;
			g.fill(barX, y + 6, barX + barWidth, y + 11, IndustrialGui.BORDER);
			g.fill(barX + 1, y + 7, barX + 1 + Math.round(menu.laneFraction(lane) * (barWidth - 2)), y + 10, IndustrialGui.CYAN);
		}
		if (menu.mutationMode()) g.text(font, machine.isAdvanced() ? Component.literal("1+2 / 3+4") : Component.literal("A+B"),
				leftPos + 80, topPos + 125, IndustrialGui.TEXT, false);
		g.text(font, Component.literal("H2O"), leftPos + 6, topPos + (advanced ? 57 : 47), IndustrialGui.TEXT, false);
		drawTank(g, leftPos + 7, topPos + (advanced ? 72 : 62), machine.getInputFluid(), machine.getTankCapacity());
		drawSlot(g, leftPos + 220, topPos + 105);
		for (int i = 0; i < 4; i++) drawSlot(g, leftPos + 220, topPos + 29 + i * 18);
	}

	@Override protected void extractOverlayTooltips(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractOverlayTooltips(g, mouseX, mouseY);
		if (menu.blockEntity instanceof HydroponicBlockEntity machine)
			tankTooltip(g, leftPos + 7, topPos + (advanced ? 72 : 62), mouseX, mouseY, machine.getInputFluid(), machine.getTankCapacity());
		if (!(menu.blockEntity instanceof HydroponicBlockEntity machine)) return;
		Slot hovered = hoveredSlot;
		if (hovered == null || hovered.hasItem()) return;
		int index = menu.slots.indexOf(hovered);
		if (index < 0 || index >= machine.getSlotCount()) return;
		String key;
		if (index < machine.inputItems.length) {
			key = index % 2 == 0 ? "ftbic.hydro.seed" : "ftbic.hydro.soil";
		} else {
			int output = index - machine.inputItems.length;
			key = machine.isAdvanced() ? switch (output % 3) {
				case 0 -> "ftbic.hydro.product";
				case 1 -> "ftbic.hydro.returned_seed";
				default -> "ftbic.hydro.byproduct";
			} : switch (output) {
				case 0 -> "ftbic.hydro.product";
				case 1 -> "ftbic.hydro.returned_seed";
				default -> "ftbic.hydro.byproduct";
			};
		}
		g.setTooltipForNextFrame(Component.translatable(key), mouseX, mouseY);
	}

	@Override public boolean mouseClicked(MouseButtonEvent event, boolean dragging) {
		int x = (int) event.x(), y = (int) event.y();
		if (isIn(x, y, leftPos + 96, topPos + 34, 42, 78)) {
			ClientRecipeCache.showRecipesForTypes(advanced
					? List.of(FTBICRecipes.HYDROPONIC_GROWTH.TYPE.get(), FTBICRecipes.HYDROPONIC_MUTATION.TYPE.get())
					: List.of(FTBICRecipes.HYDROPONIC_GROWTH.TYPE.get()));
			return true;
		}
		return super.mouseClicked(event, dragging);
	}
}
