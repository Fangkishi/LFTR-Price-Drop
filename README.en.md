# LFTR: Price Drop

The GT++ LFTR is expensive to build, its fuel chain goes through Industrial Strength Hydrofluoric Acid, and its processing is slow. This mod rewrites part of that once the game has loaded: mainline materials for the casings and the controller, a fuel path that does not need Industrial HF, and fuel and blanket processing at double speed.

Optional. It applies when installed and changes nothing when it is not.

## Read this first

Do not install this if you are running for Stargate. Stargate requires an unmodified balance, and this mod changes balance on purpose.

Not affiliated with GTNH, GT++, or GT5U. The jar contains none of their code; it calls their recipe API at runtime.

## Install

GTNH 2.9.0 (currently rc2) on Minecraft 1.7.10. Recipe targets are GT5-Unofficial 5.09.54.265.

Drop `lftrpricedrop-1.0.0.jar` into `mods/`.

On load the log gets a batch of `[LFTR]` lines, one per change, with three group summaries at the end. A missing line means that change matched nothing.

## What it changes

### Build costs, 13 blocks

The original recipes stay; each gets an extra variant made from mainline materials. Only the alloy slots change. Control circuits, computer cubes, IC2 plating, cables and hulls stay as they were.

| Block | Made in | Swapped | Kept |
|---|---|---|---|
| LFTR Controller | Crafting | Hastelloy-N double plate → Titanium double plate | LFTR control circuit, 12x Naquadah cable, GT Computer Cube, Thorium plate, IV hull |
| Reactor Casing I | Crafting | Hastelloy-N double plate → Titanium double plate; Hastelloy-C276 frame → Stainless Steel frame | IC2 Heat Capacity Reactor Plating ×4 |
| Reactor Casing II (Reactor Shield) | Crafting + Assembler | Hastelloy-C276 double plate → Stainless Steel double plate; Talonite gear → Tungstensteel gear | LV Field Generator |
| Reactor Processing Unit I | Crafting | Inconel 625 plate → Titanium plate; Hastelloy-N double plate → Titanium double plate | HV Field Generator, IV Robot Arm, IV Chemical Reactor |
| Reactor Processing Unit II | Crafting | Pikyonium plate → Tungstensteel plate; HS188A double plate → Tungstensteel double plate | IV Field Generator, ZPM Robot Arm, Processing Unit I |
| Cold Trap I | Crafting | Inconel 625 plate → Titanium plate; Hastelloy-X double plate → Tungstensteel double plate | IV hull, IV Robot Arm, Frost Proof Casing |
| Cold Trap II | Crafting | Pikyonium plate → Tungstensteel plate; HS188A double plate → Tungstensteel double plate | ZPM Robot Arm, Cold Trap I, Frost Proof Casing |
| Sparge Tower Casing | Assembler | HS188A plate → Tungstensteel plate; Hastelloy-N ring and screw → Titanium ring and screw | HV hull, circuit #8, molten Stainless Steel |
| Sparge Tower Controller | Assembly Line | HS188A gear and plate → Tungstensteel; Hastelloy-N double plate and screw → Titanium | Elite circuit, machines, wire, foil |
| Fuel Refinery | Crafting | Stellite gear → Tungstensteel gear | Elite circuit, Dense Tungstensteel plate, GT Computer Cube, IV hull |
| Fuel Refinery Internal Casing | Crafting | Incoloy-DS plate and gear → Stainless Steel; Staballoy giant pipe → Stainless Steel giant pipe | IV Super Tank |
| Fuel Refinery Casing (Hastelloy-N Sealant) | Crafting + Assembler | Incoloy-MA956 plate → Stainless Steel plate; Hastelloy-N plate → Titanium plate; Hastelloy-C276 frame → Stainless Steel frame | none |
| Fuel Refinery Structural Casing | Crafting | Inconel 792 ring → Stainless Steel ring; Hastelloy-X gear → Tungstensteel gear; C276 frame → Stainless Steel frame | EV hull, Steel plate |

### Hydrofluoric acid, 6 recipes

Three new recipes, three replacements. Uses GT++'s own ratio: 1 mB Industrial HF = 2 mB HF.

| Recipe | Action | Change |
|---|---|---|
| Lithium Carbonate → Lithium Fluoride (Chemical Bath) | Add | Industrial HF 500 → HF 1000 |
| Lithium Hydroxide → Lithium Fluoride (Chemical Reactor, Large Chemical Reactor) | Add | Industrial 500 → HF 1000 |
| U-233 + U-235 → Uranium Tetrafluoride (Mixer) | Add | Industrial 10000 → HF 20000 |
| Ammonium Tetrafluoroberyllate → Beryllium Fluoride (Large Chemical Reactor) | Replace | byproduct Industrial 1000 → HF 2000 |
| Fuel base (Industrial Blast Furnace) | Replace | inputs +2 empty cells; byproduct 2 Industrial HF cells → 4 HF cells |
| Fuel base (Chemical Plant) | Replace | byproduct Industrial 2000 → HF 4000 |

The Dehydrator's Beryllium Fluoride recipe is untouched and still yields one Industrial HF cell.

### Duration and voltage, 17 recipes

Half the time, a quarter of the EU/t, same inputs and outputs. The three LFTR burn recipes are not included, so power output is unchanged.

| Recipe | Machine | Before | After |
|---|---|---|---|
| Fuel mixing LiFBeF2-ZrF4-U235 | Fuel Refinery | 30 min · 1920 EU/t | 15 min · 480 EU/t |
| Fuel mixing LiFBeF2-ZrF4-UF4 | Fuel Refinery | 40 min · 7680 EU/t | 20 min · 1920 EU/t |
| Fuel mixing LiFBeF2-ThF4-UF4 | Fuel Refinery | 50 min · 7680 EU/t | 25 min · 1920 EU/t |
| Fuel re-mixing LiFBeF2-ZrF4-UF4 | Fuel Refinery | 12 min · 7680 EU/t | 6 min · 1920 EU/t |
| Fuel re-mixing LiFBeF2-ThF4-UF4 | Fuel Refinery | 15 min · 7680 EU/t | 7 min 30 s · 1920 EU/t |
| Spent fuel fluorination LiF-ThF4 | Reactor Processing Unit | 10 min · 7680 EU/t | 5 min · 1920 EU/t |
| Spent fuel fluorination LiFBeF2-ThF4 | Reactor Processing Unit | 10 min · 7680 EU/t | 5 min · 1920 EU/t |
| Spent fuel fluorination, sparged LiF-ThF4 | Reactor Processing Unit | 5 min · 7680 EU/t | 2 min 30 s · 1920 EU/t |
| Spent fuel fluorination, sparged LiFBeF2-ThF4 | Reactor Processing Unit | 5 min · 7680 EU/t | 2 min 30 s · 1920 EU/t |
| Spent fuel fluorination LiFBeF2-UF4-FP | Reactor Processing Unit | 120 min · 480 EU/t | 60 min · 120 EU/t |
| Spent fuel fluorination, sparged UF4-FP | Reactor Processing Unit | 60 min · 480 EU/t | 30 min · 120 EU/t |
| Cold Trap UF6F2 | Cold Trap | 10 min · 480 EU/t | 5 min · 120 EU/t |
| Cold Trap UF6F2-FP | Cold Trap | 10 min · 1920 EU/t | 5 min · 480 EU/t |
| Distillation, impure molten salt base | Distillation Tower | 7 min 30 s · 7680 EU/t | 3 min 45 s · 1920 EU/t |
| Sparging LiFBeF2-UF4-FP | Sparge Tower | 25 s · 7680 EU/t | 12.5 s · 1920 EU/t |
| Sparging LiF-ThF4 | Sparge Tower | 25 s · 7680 EU/t | 12.5 s · 1920 EU/t |
| Sparging LiFBeF2-ThF4 | Sparge Tower | 25 s · 7680 EU/t | 12.5 s · 1920 EU/t |

## Config

One option in `config/lftrpricedrop.cfg`. Set it to `false` and nothing is touched.

```
B:enablePriceDrop=true
```

## Known issues

Targets are pinned to GT5U 5.09.54.265. After a GT++ update the ones that no longer match are skipped, the log records a miss, and the game loads normally.

Tested in the dev environment (66 mods) only, not in a full 243-mod pack.

## License

MIT, see [LICENSE](LICENSE).
