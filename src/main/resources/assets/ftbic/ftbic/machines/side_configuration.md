---
navigation:
  title: Side Configuration
  icon: ftbic:configuration_card
  parent: machines/index.md
  position: 1
item_ids:
  - ftbic:configuration_card
---

# <Color id="gold">Side Configuration</Color>

Use the **I/O** button beside a machine's screen to choose which faces can transfer resources. Select **Items**, **Fluids**, or **Energy**, then click a face to cycle through its available modes. Only resource types supported by that machine appear.

Faces are relative to the machine: **Front**, **Back**, **Left**, **Right**, **Top**, and **Bottom**. The tooltip shows the world direction too. Rotating a machine rotates its settings. Machines without a facing use north as their front.

## Modes

* **Default (*)** keeps the machine's original transfer rules. New machines start here.
* **Input (+)** allows insertion into valid input slots or tanks.
* **Output (-)** allows extraction from valid output slots or tanks.
* **Both (+/-)** allows input and output where the machine supports both.
* **Disabled (X)** blocks transfers of the selected resource through that face.

Item, fluid, and energy settings are independent. Disabling items on a face still allows energy there unless you disable that too. Manual access to the machine's inventory stays available.

**Reset to Defaults** restores all faces for all resource types. Machines in existing worlds keep their original behavior until configured.

## Example: Macerator

Set the **Top** item face to **Input**, the **Back** item face to **Output**, and the other item faces to **Disabled**. Feed ore from above and collect dust behind the machine. Energy can enter through a separately configured face.

Pipes and hoppers can extract from an output face directly. Install an <ItemLink id="ftbic:ejector_upgrade" /> if you want the machine to push finished items into an adjacent inventory itself. The ejector uses only faces that permit item output. Side configuration does not add automatic input pulling.

## Machine Rules

Side settings preserve the machine's slot restrictions, energy limits, and conversion rules. Battery boxes retain their front energy output. Transformers retain their front input, and rectifiers retain their front FE input. The remaining electrical faces keep their existing roles; they can be disabled individually.

Energy settings affect both zaps and FE wherever those interfaces are available. **Default** retains the original behavior of your energy compatibility mode.

A pump offers fluid output; a geothermal generator offers fluid input. On teleporters, item/fluid input fills the send inventory/tank and output drains the receive inventory/tank. Side settings control local access, while the link between teleporters continues operating normally.

Reactor chambers share their reactor's settings, using the outward face being accessed. A top-face setting applies to the top of the reactor and the top of each attached chamber. Reactor component slots still hold one item each. The Reactor Planner has no physical resource I/O to configure.

## Configuration Card

<ItemImage id="ftbic:configuration_card" scale="2" />

<RecipeFor id="ftbic:configuration_card" />

* **Sneak+Use** on a machine copies its side settings onto the card, overwriting any previous settings.
* **Use** on another machine of the **same type** applies those settings.
* The tooltip identifies the saved machine type. Applying settings keeps the card reusable.

The card copies face settings for all supported resource types. It does not move inventory contents, energy, upgrades, or reactor layouts. Use a [Reactor Blueprint](../nuclear/blueprint.md) for reactor designs.
