# AGENTS.md

## Project overview

Gradle multi-project build producing four Minecraft plugin JARs:

| Subproject | Target platform | Artifact |
|---|---|---|
| `ProxyMessagesVelocity` | Velocity proxy (3.4.0-SNAPSHOT) | fat JAR via Shadow |
| `ProxyMessagesPaper` | Paper backend servers (API 1.20) | thin JAR |
| `ProxyMessagesFabric` | Fabric servers (MC 1.20.1, Loader 0.15.x) | thin JAR via Loom |
| `ProxyMessagesNeoForge` | NeoForge servers (MC 1.20.1, NeoForge 47.x) | thin JAR via NeoGradle |

`ProxyMessagesVelocity` and `ProxyMessagesPaper` share build conventions defined in `buildSrc/src/main/kotlin/buildlogic.java-*.gradle.kts`. The modded subprojects use their own build plugins (`fabric-loom` and `net.neoforged.gradle.userdev`) and do **not** apply the buildSrc conventions. Java 21 toolchain is required for all subprojects.

## Build commands

```bash
# Build all subprojects (produces JARs under each subproject's build/libs/)
./gradlew build

# Build a single subproject
./gradlew :ProxyMessagesVelocity:build
./gradlew :ProxyMessagesPaper:build
./gradlew :ProxyMessagesFabric:build
./gradlew :ProxyMessagesNeoForge:build

# Run tests (JUnit Jupiter, configured in buildSrc)
./gradlew test
```

The Velocity artifact is assembled with the Shadow plugin (`shadowJar` task, uses `minimize()`) and produces `ProxyMessagesVelocity-4.0.0.jar`. The Paper artifact is a plain `jar`. The modded artifacts are produced by their respective mod toolchains (Loom / NeoGradle) and are also plain JARs. The first build of a modded subproject downloads and deobfuscates Minecraft, which takes several minutes.

## Key architecture facts

- **Communication channels**: Two plugin messaging channels:
  - `proxymessages:main` — chat/event messages. Paper captures chat events, serializes UUID + message, sends to Velocity; Velocity re-broadcasts to all servers.
  - `proxymessages:papi` — PlaceholderAPI resolution. Velocity sends a placeholder string to Paper, Paper resolves it via PAPI and replies.
- **Paper plugin is optional** unless `enable-papi: true` in the Velocity config. When PAPI is enabled it is required on all backend servers.
- **Modded companion mods** (`ProxyMessagesFabric`, `ProxyMessagesNeoForge`) replicate Paper's role on Fabric and NeoForge backends: suppress vanilla join/leave messages and forward chat over `proxymessages:main`. PAPI is not available on either platform — `enable-papi` must be `false` on any network with modded backends.
- **NeoForge join/leave suppression requires Mixins** — NeoForge 47.x has no API hook equivalent to Paper's `event.joinMessage(null)`. `PlayerListMixin` uses `@Redirect` to suppress the `broadcastSystemMessage` calls in `placeNewPlayer` and `remove`.
- **NeoForge plugin message ingestion requires a Mixin** — `ServerGamePacketHandlerMixin` injects into `handleCustomPayload` to receive proxy→backend messages without triggering NeoForge's channel version-negotiation.
- **Config & data directory**: Velocity side only. Config lives at `plugins/proxymessages/config.yml` at runtime. The `database.txt` resource ships a single default color entry (`default #5e5e5e`) and is used to persist per-player name colors.
- **No config on Paper or the modded mods**: all behavior is driven by the Velocity side.
- **Reload**: `pmReload` / `reloadPM` commands call `configUtil.saveData()` then re-run `initialize()` in-place — no server restart needed.
- **Discord integration**: Uses JDA 5.6.1, shaded into the Velocity JAR. Enabled/disabled at runtime via config.
- **Configurate fork**: Uses `org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT` (GeyserMC snapshot, not the mainline 4.x release) for config parsing. The package is relocated to `dev.ogblackdiamond.libs.configurate` in the shadow JAR to avoid conflicts.

## Versioning

- Velocity plugin version: `4.0.0` (set in `@Plugin` annotation in `ProxyMessages.java` and `shadowJar.archiveVersion` in `build.gradle.kts`)
- Paper plugin version: `4.0.0` (set in `plugin.yml` and `tasks.jar.archiveVersion`)
- Fabric mod version: `4.0.0` (set in `build.gradle.kts` `version` and expanded into `fabric.mod.json` at build time)
- NeoForge mod version: `4.0.0` (set in `build.gradle.kts` `version` and expanded into `META-INF/mods.toml` at build time)
- These are **not kept in sync** — update them independently when releasing.

## Style / formatting

- `formatting.xml` is a Google Java Style Checkstyle config used as an **IDE formatter** (Eclipse). It is **not wired into the Gradle build** — no Checkstyle plugin is applied in any `.gradle.kts` file. Style violations will not fail the build.
- Conventions to follow: 4-space indentation, 100-character line limit, no star imports, Javadoc on public methods with ≥2 lines.

## Dependency notes

- `velocity-api` and `paper-api` are `compileOnly` — do not shade them.
- `commons-text:1.11.0` is pinned via a constraint in `buildSrc/buildlogic.java-common-conventions.gradle.kts` and is included at runtime via Shadow in the Velocity JAR.
- The GeyserMC snapshot repo (`https://repo.opencollab.dev/maven-snapshots`) is required for the custom Configurate build.
- The Shadow JAR uses `minimize()` — adding new dependencies requires verifying that needed classes are not inadvertently stripped.
- `settings.gradle.kts` has a `pluginManagement {}` block (must remain first) that adds the Fabric Maven (`https://maven.fabricmc.net/`) and NeoForge Maven (`https://maven.neoforged.net/releases`) repos so the loom and NeoGradle plugins resolve without manual local installs.
