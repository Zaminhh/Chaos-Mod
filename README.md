# ChaosClient 1.0.0

Minecraft 26.2 client mod using Fabric Loader 0.19.3, Fabric API 0.161.0+26.2, Loom 1.17, and Java 25.

Run `./gradlew build` with a Java 25 `JAVA_HOME`. The mod JAR is produced in `build/libs/`.

Right Shift opens the ClickGUI. Left-click toggles modules; right-click expands settings. Panel position, category, expansion state, values, colors, modes, and keybinds persist to `config/chaosclient.json`.

Configuration changes are debounced for 30 seconds and serialized on a daemon writer. A world unload or GUI close requests an asynchronous flush.

## Hook map

- `LocalPlayerMixin`: reach, freecam movement-packet suppression, NoSlow, and SafeWalk.
- `EntityMixin`: local-player step height override.
- `ClientPacketListenerMixin`: Velocity cancels local knockback packets.
- `MinecraftMixin`: FastPlace removes the client use delay.
- `MultiPlayerGameModeMixin`: FastBreak accelerates destroy progress and clears delay.
- `AbstractContainerScreenMixin`: ChestStealer quick-moves container slots.
- 26.2 is unobfuscated; `net.fabricmc.fabric-loom` is used without a mappings dependency.

## Performance changes

- ESP is submitted through one reusable `SimpleGizmoCollector`; vanilla batches the collected geometry by render state.
- Xray scans 2,048 blocks per tick instead of traversing the entire 49³ volume in one tick.
- Killaura sends silent rotations only when its CPS gate fires and a target exists.
- Feature ticking switches only over the cached enabled-module set.
- Mixins use direct shadows and injectors; no reflection exists in a hot path.
