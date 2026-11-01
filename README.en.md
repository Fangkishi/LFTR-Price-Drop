# LFTR: Price Drop

An optional addon. It does not touch GT++'s source — at load complete it rewrites a set of recipes on the GT++ **LFTR (Liquid Fluoride Thorium Reactor)** production line: build costs move from single-use alloys to mainline materials, the fuel chain gains a path that needs no Industrial Strength Hydrofluoric Acid, and fuel and blanket processing get faster.

> ⚠️ **Not Stargate-compliant.** Stargate eligibility requires a run without balance changes; this mod deliberately alters build costs and processing speed. Do not install it if you are running for Stargate or plan to.
>
> Not affiliated with GTNH, GT++, or GT5U. The jar contains none of their code or assets; it only calls their recipe API at runtime.

**中文:** [README.md](README.md)

## Install

- GTNH 1.7.10, Forge 10.13.4.1614
- Requires GT5-Unofficial 5.09.54.265 (GT++ ships **inside** GT5U as modid `miscutils` — do not go looking for a separate GT++ jar)
- Drop `lftrpricedrop-<version>.jar` into `mods/` and start the game once
- Every load prints `[LFTR]` lines: one per change (36), plus three group summaries

## What it changes (36 entries)

### A · Build costs, 13 entries

Each variant copies the original recipe slot for slot and swaps only GT++'s single-use alloys for mainline materials of the same form and count. Control circuits, computer cubes, IC2 reactor plating, cables and hulls stay exactly as they were; if the original had two forms, the variant has the same two.

| Block | Form | Swapped | Kept |
|---|---|---|---|
| LFTR Controller | Crafting | Hastelloy-N double plate → Titanium double plate | LFTR control circuit, 12x Naquadah cable, GT Computer Cube, Thorium plate, IV hull |
| Reactor Casing I | Crafting | Hastelloy-N double plate → Titanium double plate; Hastelloy-C276 frame → Stainless Steel frame | IC2 Heat Capacity Reactor Plating ×4 |
| Reactor Casing II (Reactor Shield) | Crafting + Assembler, both | Hastelloy-C276 double plate → Stainless Steel double plate; Talonite gear → Tungstensteel gear | LV Field Generator |
| Reactor Processing Unit I | Crafting | Inconel 625 plate → Titanium plate; Hastelloy-N double plate → Titanium double plate | HV Field Generator, IV Robot Arm, IV Chemical Reactor |
| Reactor Processing Unit II | Crafting | Pikyonium plate → Tungstensteel plate; HS188A double plate → Tungstensteel double plate | IV Field Generator, ZPM Robot Arm, Processing Unit I |
| Cold Trap I | Crafting | Inconel 625 plate → Titanium plate; Hastelloy-X double plate → Tungstensteel double plate | IV hull, IV Robot Arm, Frost Proof Casing |
| Cold Trap II | Crafting | Pikyonium plate → Tungstensteel plate; HS188A double plate → Tungstensteel double plate | ZPM Robot Arm, Cold Trap I, Frost Proof Casing |
| Sparge Tower Casing | Assembler | HS188A plate → Tungstensteel plate; Hastelloy-N ring and screw → Titanium ring and screw | HV hull, circuit #8, molten Stainless Steel |
| Sparge Tower Controller | Assembly Line | HS188A gear and plate → Tungstensteel; Hastelloy-N double plate and screw → Titanium | Elite circuit, machines, wire, foil |
| Fuel Refinery | Crafting | Stellite gear → Tungstensteel gear | Elite circuit, Dense Tungstensteel plate, GT Computer Cube, IV hull |
| Fuel Refinery Internal Casing | Crafting | Incoloy-DS plate and gear → Stainless Steel; Staballoy giant pipe → Stainless Steel giant pipe | IV Super Tank |
| Fuel Refinery Casing (Hastelloy-N Sealant) | Crafting + Assembler, both | Incoloy-MA956 plate → Stainless Steel plate; Hastelloy-N plate → Titanium plate; Hastelloy-C276 frame → Stainless Steel frame | — |
| Fuel Refinery Structural Casing | Crafting | Inconel 792 ring → Stainless Steel ring; Hastelloy-X gear → Tungstensteel gear; C276 frame → Stainless Steel frame | EV hull, Steel plate |

### B · Hydrofluoric acid path, 6 entries

3 added, 3 replaced. Uses GT++'s existing ratio: **1 mB Industrial Strength Hydrofluoric Acid = 2 mB Hydrofluoric Acid**.

| Recipe | Action | Change |
|---|---|---|
| Lithium Carbonate → Lithium Fluoride (Chemical Bath) | Add | Industrial HF 500 → HF 1000 |
| Lithium Hydroxide → Lithium Fluoride (Chemical Reactor + Large Chemical Reactor) | Add | Industrial 500 → HF 1000 |
| U-233 + U-235 → Uranium Tetrafluoride (Mixer) | Add | Industrial 10000 → HF 20000 |
| Ammonium Tetrafluoroberyllate → Beryllium Fluoride (Large Chemical Reactor) | Replace | byproduct Industrial 1000 → HF 2000 |
| Fuel base (Industrial Blast Furnace) | Replace | inputs +2 empty cells; byproduct 2 Industrial HF cells → 4 HF cells |
| Fuel base (Chemical Plant) | Replace | byproduct Industrial 2000 → HF 4000 |

The Dehydrator's Beryllium Fluoride recipe is untouched and still yields one Industrial HF cell.

### C · Duration and voltage, 17 entries

Duration halved, EU/t divided by 4, inputs and outputs unchanged. The three LFTR burn recipes are out of scope — power output is untouched.

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

`config/lftrpricedrop.cfg`:

```
B:enablePriceDrop=true
```

Set it to `false` and nothing is touched; the log shows a single "config disabled" line.

## Why some entries add and others replace

GT matches recipes on **inputs only**, tolerates extra inputs in the machine, and takes the first match. So:

- A variant with **different inputs** can only be added — the two are told apart by the fluid, and the old line keeps working.
- A variant that differs **only in byproducts** has identical inputs; the later one is unreachable, so it must replace the original. The cost: the two fuel-base Industrial HF recipes disappear.

## Known limitations

- Targets are pinned to GT5U 5.09.54.265. If GT++ changes a recipe, the log prints a "miss" line, that one change is skipped, and **the original recipe stays untouched — the game still loads**. Nothing crashes, nothing is deleted by mistake.
- Rare exception: when one entry covers two recipe maps (B group's Lithium Hydroxide to Lithium Fluoride) or a block has two forms (Reactor Shield, Refinery Casing), a failure in the second half can leave the first half applied while the log still says miss. Known gap in 1.0.0, see AGENTS.md.
- Verified in the dev loop (66 mods) only, not in the full 243-mod pack.
- Multiblock structures are not modified; casing types and counts are unchanged.

## License

MIT, see [LICENSE](LICENSE).
