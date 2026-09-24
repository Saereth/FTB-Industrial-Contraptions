---
navigation:
  title: Batteries & Battery Boxes
  icon: ftbic:hv_battery
  parent: energy/index.md
  position: 2
item_ids:
  - ftbic:single_use_battery
  - ftbic:lv_battery
  - ftbic:mv_battery
  - ftbic:hv_battery
  - ftbic:ev_battery
  - ftbic:creative_battery
  - ftbic:lv_battery_box
  - ftbic:mv_battery_box
  - ftbic:hv_battery_box
  - ftbic:ev_battery_box
  - ftbic:industrial_bank_cell
  - ftbic:industrial_bank_port
  - ftbic:energy_crystal
---

# <Color id="gold">Batteries & Battery Boxes</Color>

## <Color id="gold">Industrial Battery Bank</Color>

Place <ItemLink id="industrial_bank_cell" /> and <ItemLink id="industrial_bank_port" /> face to face to form one bank. Each cell adds storage; each port accepts and supplies energy through its exposed faces. Add more ports for more connections. Right-click any cell or port to see the bank's charge, capacity, and member counts. The side configuration on a port can restrict input and output faces.

The default cell holds 10 million zaps. Each port automatically outputs up to 8,192 zaps per tick and accepts up to that amount in one input transaction. Incoming energy moves into connected cells so the port can keep accepting power. Server config controls both values and the maximum number of blocks searched per bank. Energy remains in each cell if you split the bank, and reconnecting cells restores the shared readout. Bank casings join visually along touching faces.

Sneak and right-click a port to cycle its appearance: **Port** (the default connection symbol), **Gauge**, then **Basic** (plain steel like a cell). The choice belongs to that port and survives a world reload. Changing the appearance does not change energy input or output. The selected style appears briefly above the hotbar.

Gauge shows the whole bank's fill level with a glowing bar. Set neighboring ports to Gauge on the same flat face to form a wider or taller display. A rectangle of Gauge ports fills from the bottom of the entire panel. Basic and Port styles interrupt the display, as do gaps, covered faces, and corners. Irregular Gauge shapes form smaller rectangular panels.

<RecipeFor id="industrial_bank_cell" />
<RecipeFor id="industrial_bank_port" />

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="hv_battery_box" scale="2" />

  Batteries are portable energy. Battery boxes are network buffers — drop batteries in and they charge, or pull them out charged to power a handheld device.
</Column>

<ItemImage id="minecraft:air" scale="0.25"/>
***

<Column alignItems="center" fullWidth={true}>
  ## <Color id="gold">Handheld Batteries</Color>
</Column>

<Row>
  <ItemImage id="single_use_battery" />
  ### <Color id="aqua">Single Use Battery</Color>
</Row>

A cheap throwaway battery. 2,400 zaps. Good for emergencies, useless for infrastructure.

<RecipeFor id="single_use_battery" />

<Row>
  <ItemImage id="lv_battery" />
  ### <Color id="aqua">LV Battery</Color>
</Row>

4,000 zap capacity, LV tier. The first rechargeable battery.

<RecipeFor id="lv_battery" />

<Row>
  <ItemImage id="mv_battery" />
  ### <Color id="aqua">MV Battery</Color>
</Row>

40,000 zaps. Upgrade from the LV battery using <ItemLink id="energy_crystal" />.

<RecipeFor id="mv_battery" />

<Row>
  <ItemImage id="hv_battery" />
  ### <Color id="aqua">HV Battery</Color>
</Row>

400,000 zaps. Feeds mid- to late-game handheld gear.

<RecipeFor id="hv_battery" />

<Row>
  <ItemImage id="ev_battery" />
  ### <Color id="aqua">EV Battery</Color>
</Row>

10,000,000 zaps. The largest portable battery in the mod.

<RecipeFor id="ev_battery" />

<Row>
  <ItemImage id="creative_battery" />
  ### <Color id="aqua">Creative Battery</Color>
</Row>

Infinite energy. Creative-only.

<ItemImage id="minecraft:air" scale="0.25"/>
***

<Column alignItems="center" fullWidth={true}>
  ## <Color id="gold">Battery Boxes</Color>
</Column>

A battery box has four **charge** slots and four **discharge** slots. Place charged batteries in the discharge slots to feed the network; drop empty batteries in the charge slots to refill them. Each box also has an internal buffer matched to its tier.

Open **I/O**, choose **Energy**, then set any face to **Output** to send power there. You can set several faces to Output. Set a face to **Input** to receive power, **Both** to allow both directions, or **Disabled** to disconnect it. **Default** keeps the original arrangement: the front outputs, and the other faces input. Each face changes between the input and output texture when its output role changes. The same controls work on LV, MV, HV, and EV Battery Boxes.

<Row>
  <ItemImage id="lv_battery_box" />
  ### <Color id="aqua">LV Battery Box — 40,000 zaps</Color>
</Row>

<RecipeFor id="lv_battery_box" />

<Row>
  <ItemImage id="mv_battery_box" />
  ### <Color id="aqua">MV Battery Box — 400,000 zaps</Color>
</Row>

<RecipeFor id="mv_battery_box" />

<Row>
  <ItemImage id="hv_battery_box" />
  ### <Color id="aqua">HV Battery Box — 4,000,000 zaps</Color>
</Row>

<RecipeFor id="hv_battery_box" />

<Row>
  <ItemImage id="ev_battery_box" />
  ### <Color id="aqua">EV Battery Box — 40,000,000 zaps</Color>
</Row>

<RecipeFor id="ev_battery_box" />

<ItemImage id="minecraft:air" scale="0.25"/>

<Row>
  <ItemImage id="energy_crystal" />
  ### <Color id="aqua">Energy Crystal</Color>
</Row>

A compressed block of diamonds and redstone. Required to craft MV+ batteries and high-capacity devices.

<RecipeFor id="energy_crystal" />
