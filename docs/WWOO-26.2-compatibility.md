# WWOO compatibility on Minecraft 26.2

## Status

William Wythers' Overhauled Overworld (WWOO) 2.7.0 is an official Fabric/Quilt release for Minecraft 26.1.2. The project is licensed CC-BY-NC-ND-4.0 and does not publish a source repository, so this project must not fork, alter, or redistribute a modified WWOO artifact.

The official 2.7.0 artifact can pass Fabric 0.19.3 dependency resolution on Minecraft 26.2 with a local dependency override, but its data pack cannot load on 26.2. Registry loading stops at `wythers:terrain/feature/tepui_cavern_dripstone_spikes` because it references the removed `minecraft:dripstone_cluster` feature type. Dynamic Trees integration therefore remains gated on an official WWOO 26.2 release.

## Dynamic Trees compatibility

WWOO 2.7.0 rewrites vanilla biome definitions rather than registering its own biome IDs. Its official jar contains 344 configured tree features under the `wythers` namespace:

- 80 use `minecraft:tree` directly.
- 20 use `minecraft:random_selector`.
- 244 use `minecraft:simple_random_selector`.

Dynamic Trees already selects the affected `minecraft` overworld biomes. `TreeFeatureCanceller` now follows `FeatureConfiguration.getSubFeatures()` recursively, allowing it to find tree configurations nested under both selector types. No WWOO-specific tree pack or derivative WWOO data is required.

## Primary sources

- [WWOO Modrinth project](https://modrinth.com/mod/wwoo) — project license, supported environments, and compatibility description.
- [WWOO 2.7.0 release](https://modrinth.com/mod/wwoo/version/2.7.0) — Minecraft 26.1.2 target, Fabric/Quilt loaders, and dependencies.
- [Official WWOO 2.7.0 artifact](https://cdn.modrinth.com/data/II7t6llZ/versions/svF64mZQ/wwoo-fabric-26.1.2-2.7.0.jar) — inspected for Fabric metadata and aggregate configured-feature types; no content is redistributed.
- [Fabric dependency overrides](https://docs.fabricmc.net/players/troubleshooting/dependency-overrides) — loader-supported method used to test the untouched 26.1.2 artifact on 26.2.
