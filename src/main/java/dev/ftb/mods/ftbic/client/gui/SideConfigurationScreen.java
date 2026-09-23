package dev.ftb.mods.ftbic.client.gui;

import dev.ftb.mods.ftbic.net.FTBICNet;
import dev.ftb.mods.ftbic.net.SideConfigurationPayload;
import dev.ftb.mods.ftbic.screen.ElectricBlockMenu;
import dev.ftb.mods.ftbic.util.SideConfiguration;
import dev.ftb.mods.ftbic.util.SideConfiguration.Face;
import dev.ftb.mods.ftbic.util.SideConfiguration.Mode;
import dev.ftb.mods.ftbic.util.SideConfiguration.Resource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Locale;

/** A modal GUI layer keeps the machine menu open and server-authoritative. */
public class SideConfigurationScreen extends Screen {
	private final ElectricBlockMenu menu;
	private Resource selected = Resource.ITEMS;
	private final EnumMap<Face, Button> faces = new EnumMap<>(Face.class);
	private final EnumMap<Resource, Button> tabs = new EnumMap<>(Resource.class);
	private int left, top;

	public SideConfigurationScreen(ElectricBlockMenu menu) {
		super(Component.translatable("ftbic.sides.title"));
		this.menu = menu;
		for (Resource resource : Resource.values()) if (menu.blockEntity.supportsResource(resource)) { selected = resource; break; }
	}

	private static Component name(Enum<?> value) {
		return Component.translatable("ftbic.sides." + value.name().toLowerCase(Locale.ROOT));
	}

	@Override protected void init() {
		left = (width - 280) / 2;
		top = (height - 218) / 2;
		faces.clear();
		tabs.clear();
		int tab = 0;
		for (Resource resource : Resource.values()) {
			if (!menu.blockEntity.supportsResource(resource)) continue;
			tabs.put(resource, addRenderableWidget(Button.builder(name(resource), b -> { selected = resource; refresh(); })
					.bounds(left + 10 + tab++ * 88, top + 28, 84, 20).build()));
		}
		for (Face face : Face.values()) {
			int column = face.ordinal() % 2, row = face.ordinal() / 2;
			faces.put(face, addRenderableWidget(Button.builder(name(face), b -> cycle(face))
					.bounds(left + 10 + column * 132, top + 56 + row * 34, 128, 30).build()));
		}
		addRenderableWidget(Button.builder(Component.translatable("ftbic.sides.reset"), b ->
				FTBICNet.sendToServer(new SideConfigurationPayload(menu.containerId, -1, -1, -1)))
				.bounds(left + 10, top + 186, 166, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
				.bounds(left + 182, top + 186, 88, 20).build());
		refresh();
	}

	private void cycle(Face face) {
		var machine = menu.blockEntity;
		int mode = machine.getSideConfiguration().mode(selected, face).ordinal();
		do { mode = (mode + 1) % Mode.values().length; }
		while (!machine.supportsSideMode(selected, face, Mode.values()[mode]));
		FTBICNet.sendToServer(new SideConfigurationPayload(menu.containerId, selected.ordinal(), face.ordinal(), mode));
	}

	private void refresh() {
		var machine = menu.blockEntity;
		for (var entry : tabs.entrySet()) entry.getValue().active = entry.getKey() != selected;
		for (var entry : faces.entrySet()) {
			Face face = entry.getKey();
			Mode mode = machine.getSideConfiguration().mode(selected, face);
			entry.getValue().setMessage(name(face).copy().append(": " + mode.symbol + " ").append(name(mode)));
			Direction worldSide = face.direction(machine.getFacing(Direction.NORTH));
			entry.getValue().setTooltip(Tooltip.create(Component.translatable("ftbic.sides.face_hint", name(face),
					Component.translatable("ftbic.sides.direction." + worldSide.name().toLowerCase(Locale.ROOT)), name(mode))));
		}
	}

	@Override public void tick() {
		if (minecraft.player == null || minecraft.player.containerMenu != menu || menu.blockEntity.isRemoved()) {
			onClose();
			return;
		}
		refresh();
	}

	@Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		g.fill(left, top, left + 280, top + 218, 0xFF373737);
		g.fill(left + 2, top + 2, left + 278, top + 216, 0xFFC6C6C6);
		g.text(font, title, left + 10, top + 10, 0xFF303030, false);
		g.text(font, Component.translatable("ftbic.sides.default_hint"), left + 10, top + 164, 0xFF303030, false);
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		for (var entry : faces.entrySet()) {
			Button button = entry.getValue();
			Mode mode = menu.blockEntity.getSideConfiguration().mode(selected, entry.getKey());
			g.fill(button.getX() + 2, button.getY() + 2, button.getX() + 5, button.getY() + 28, mode.color);
		}
	}

	@Override public boolean isPauseScreen() { return false; }
}
