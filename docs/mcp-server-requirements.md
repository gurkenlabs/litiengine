# MCP Server Requirements for utiLITI

## Overview

This document outlines the requirements for implementing a Model Context Protocol (MCP) server within utiLITI, the map editor for the LITIENGINE game engine. The MCP server will enable AI assistants and LLM-powered tools to interact with utiLITI's editing capabilities, game assets, and project data.

## Protocol Version

Target MCP Specification: `2026-07-28` (stateless HTTP transport)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        AI Host                              │
│  (IDE, Chat Interface, Custom Workflow)                     │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ MCP Protocol (JSON-RPC 2.0)
                            │ Streamable HTTP Transport
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    utiLITI MCP Server                        │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐  │
│  │  Resources   │   Prompts   │    Tools    │   Logging   │  │
│  └─────────────┴─────────────┴─────────────┴─────────────┘  │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              LITIENGINE Integration Layer                │ │
│  │  (Editor, MapComponent, ResourceBundle, Spritesheets)   │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Requirements

### 1. Server Capabilities Declaration

The MCP server MUST declare the following capabilities:

```json
{
  "capabilities": {
    "resources": { "subscribe": true, "listChanged": true },
    "prompts": { "listChanged": true },
    "tools": { "listChanged": true },
    "logging": {}
  }
}
```

### 2. Resources

Resources expose utiLITI's game data and editor state to AI models.

#### 2.1 Project Resources

| Resource URI | Description | MIME Type |
|--------------|-------------|-----------|
| `uti://project/info` | Game project metadata (name, version, description) | `application/json` |
| `uti://project/maps` | List of all maps in the project | `application/json` |
| `uti://project/spritesheets` | All registered spritesheets | `application/json` |
| `uti://project/sounds` | All registered sound resources | `application/json` |
| `uti://project/emitters` | All emitter definitions | `application/json` |
| `uti://project/blueprints` | All blueprint templates | `application/json` |
| `uti://project/tilesets` | All tileset definitions | `application/json` |

#### 2.2 Map Resources

| Resource URI | Description | MIME Type |
|--------------|-------------|-----------|
| `uti://map/{name}` | Complete map data (TMX) | `application/xml` |
| `uti://map/{name}/layers` | Map layer hierarchy | `application/json` |
| `uti://map/{name}/entities` | All entities on the map | `application/json` |
| `uti://map/{name}/spawnpoints` | Spawn point definitions | `application/json` |
| `uti://map/{name}/collisions` | Collision data | `application/json` |
| `uti://map/{name}/terrain` | Terrain information | `application/json` |

#### 2.3 Asset Resources

| Resource URI | Description | MIME Type |
|--------------|-------------|-----------|
| `uti://asset/spritesheet/{name}` | Spritesheet metadata and frame data | `application/json` |
| `uti://asset/sound/{name}` | Sound resource metadata | `application/json` |
| `uti://asset/emitter/{name}` | Emitter configuration | `application/xml` |
| `uti://asset/blueprint/{name}` | Blueprint template data | `application/xml` |

#### 2.4 Editor State Resources

| Resource URI | Description | MIME Type |
|--------------|-------------|-----------|
| `uti://editor/state` | Current editor state (active map, selection, etc.) | `application/json` |
| `uti://editor/history` | Undo/redo history | `application/json` |
| `uti://editor/preferences` | User preferences | `application/json` |

### 3. Prompts

Prompts provide structured workflows for common game development tasks.

#### 3.1 Map Creation Prompts

| Prompt Name | Description | Arguments |
|-------------|-------------|-----------|
| `create-map` | Step-by-step map creation workflow | `width`, `height`, `tileSize` |
| `import-tileset` | Guide for importing and configuring tilesets | `tilesetPath` |
| `setup-map-layers` | Helper for setting up layer hierarchy | `layerTypes` |

#### 3.2 Asset Management Prompts

| Prompt Name | Description | Arguments |
|-------------|-------------|-----------|
| `import-spritesheet` | Guide for importing and configuring spritesheets | `imagePath`, `frameWidth`, `frameHeight` |
| `import-sounds` | Guide for importing sound assets | `soundPaths` |
| `configure-emitter` | Helper for setting up particle emitters | `emitterType` |

#### 3.3 Entity and Gameplay Prompts

| Prompt Name | Description | Arguments |
|-------------|-------------|-----------|
| `place-entity` | Guide for placing and configuring entities | `entityType` |
| `setup-spawnpoints` | Helper for configuring spawn points | `entityType`, `count` |
| `configure-collision` | Guide for setting up collision maps | `collisionTypes` |

### 4. Tools

Tools enable AI models to perform actions within utiLITI.

#### 4.1 Project Management Tools

| Tool Name | Description | Input Schema | Annotations |
|-----------|-------------|--------------|-------------|
| `get-project-info` | Get current project metadata | `{}` | `readOnlyHint: true` |
| `list-maps` | List all maps in the project | `{}` | `readOnlyHint: true` |
| `list-assets` | List all assets by type | `{ type: "spritesheet" \| "sound" \| "emitter" \| "blueprint" \| "tileset" }` | `readOnlyHint: true` |

#### 4.2 Map Editing Tools

| Tool Name | Description | Input Schema | Annotations |
|-----------|-------------|--------------|-------------|
| `get-map-info` | Get detailed map information | `{ mapName: string }` | `readOnlyHint: true` |
| `get-map-layer` | Get layer data | `{ mapName: string, layerName: string }` | `readOnlyHint: true` |
| `get-map-entities` | Get all entities on a map | `{ mapName: string }` | `readOnlyHint: true` |
| `create-map` | Create a new map | `{ name: string, width: int, height: int, tileWidth: int, tileHeight: int }` | `readOnlyHint: false, destructiveHint: false` |
| `add-layer` | Add a new layer to a map | `{ mapName: string, layerType: "tile" \| "object" \| "image", layerName: string }` | `readOnlyHint: false, destructiveHint: false` |
| `remove-layer` | Remove a layer from a map | `{ mapName: string, layerName: string }` | `readOnlyHint: false, destructiveHint: true` |
| `set-tile-data` | Set tile data for a tile layer | `{ mapName: string, layerName: string, x: int, y: int, tileId: int }` | `readOnlyHint: false` |
| `add-entity` | Add an entity to the map | `{ mapName: string, entityType: string, x: float, y: float, properties: object }` | `readOnlyHint: false, destructiveHint: false` |
| `remove-entity` | Remove an entity from the map | `{ mapName: string, entityId: string }` | `readOnlyHint: false, destructiveHint: true` |

#### 4.3 Asset Management Tools

| Tool Name | Description | Input Schema | Annotations |
|-----------|-------------|--------------|-------------|
| `import-spritesheet` | Import a spritesheet from file | `{ filePath: string, name?: string, frameWidth?: int, frameHeight?: int }` | `readOnlyHint: false, destructiveHint: false` |
| `import-sound` | Import a sound file | `{ filePath: string, name?: string }` | `readOnlyHint: false, destructiveHint: false` |
| `import-emitter` | Import an emitter definition | `{ filePath: string }` | `readOnlyHint: false, destructiveHint: false` |
| `import-blueprint` | Import a blueprint template | `{ filePath: string }` | `readOnlyHint: false, destructiveHint: false` |
| `import-tileset` | Import a tileset definition | `{ filePath: string }` | `readOnlyHint: false, destructiveHint: false` |
| `get-spritesheet-info` | Get spritesheet metadata | `{ name: string }` | `readOnlyHint: true` |

#### 4.4 Project File Tools

| Tool Name | Description | Input Schema | Annotations |
|-----------|-------------|--------------|-------------|
| `save-project` | Save the current project | `{ path?: string }` | `readOnlyHint: false, destructiveHint: false` |
| `load-project` | Load a project file | `{ path: string }` | `readOnlyHint: false, destructiveHint: false` |
| `export-sprites` | Export spritesheet definitions | `{ outputPath: string, includeResources: bool }` | `readOnlyHint: false, destructiveHint: false` |

### 5. Transport Configuration

#### 5.1 Streamable HTTP Transport

- **Endpoint**: `http://localhost:{port}/mcp`
- **Default Port**: `8080` (configurable via utiLITI preferences)
- **Required Headers**:
  - `Mcp-Method`: Indicates the operation type
  - `Mcp-Name`: Names the target tool/resource/prompt
  - `MCP-Protocol-Version`: Protocol version identifier

#### 5.2 Server Discovery

Implement `server/discover` method to advertise supported versions and capabilities:

```json
{
  "versions": ["2026-07-28"],
  "capabilities": {
    "resources": { "subscribe": true, "listChanged": true },
    "prompts": { "listChanged": true },
    "tools": { "listChanged": true }
  }
}
```

### 6. Security Requirements

#### 6.1 User Consent

- All tool executions MUST require explicit user consent
- Resource reads MUST be logged and visible to the user
- Destructive operations MUST show confirmation dialogs

#### 6.2 Access Control

- MCP server MUST only be accessible on localhost by default
- Remote access MUST require explicit configuration and authentication
- Sensitive project files MUST NOT be exposed without user consent

#### 6.3 Data Privacy

- Project data MUST NOT be transmitted outside the local machine without explicit user consent
- Tool execution logs MUST be stored locally only

### 7. Error Handling

#### 7.1 Error Codes

| Error Code | Description |
|------------|-------------|
| `-32602` | Invalid parameters (missing resource) |
| `-32600` | Invalid request |
| `-32601` | Method not found |
| `-32603` | Internal error |

#### 7.2 Error Response Format

```json
{
  "jsonrpc": "2.0",
  "id": "request-id",
  "error": {
    "code": -32602,
    "message": "Map not found: mapName",
    "data": {
      "mapName": "invalid-map",
      "availableMaps": ["map1", "map2"]
    }
  }
}
```

### 8. Caching Strategy

#### 8.1 List Caching

- `tools/list`, `resources/list`, `prompts/list` responses MUST include `ttlMs` and `cacheScope`
- Default TTL: 60 seconds for static data, 10 seconds for dynamic state
- Cache scope: `private` (per-user) for editor state, `public` for static assets

#### 8.2 Resource Caching

- Static resources (spritesheets, tilesets): `ttlMs: 300000` (5 minutes)
- Dynamic resources (editor state): `ttlMs: 5000` (5 seconds)
- Change notifications MUST invalidate relevant caches

### 9. Logging

- Server events SHOULD be logged to stderr (per MCP 2026-07-28 deprecation of logging capability)
- Log levels: `debug`, `info`, `warning`, `error`
- Tool execution logs SHOULD be captured for debugging

### 10. Configuration

#### 10.1 Server Configuration File

Location: `{projectPath}/.utiliti/mcp.json`

```json
{
  "enabled": true,
  "port": 8080,
  "host": "localhost",
  "requireConsent": true,
  "allowedOrigins": ["http://localhost:*"],
  "logging": {
    "level": "info",
    "file": "mcp-server.log"
  },
  "tools": {
    "enabled": ["*"],
    "disabled": []
  },
  "resources": {
    "enabled": ["*"],
    "disabled": []
  }
}
```

### 11. Implementation Phases

#### Phase 1: Core Infrastructure
- [ ] MCP server skeleton with HTTP transport
- [ ] `server/discover` implementation
- [ ] Basic tool registration and execution
- [ ] User consent dialogs

#### Phase 2: Resources
- [ ] Project metadata resources
- [ ] Map data resources
- [ ] Asset metadata resources
- [ ] Editor state resources

#### Phase 3: Tools
- [ ] Project management tools
- [ ] Map editing tools
- [ ] Asset import tools
- [ ] File management tools

#### Phase 4: Prompts
- [ ] Map creation workflows
- [ ] Asset management guides
- [ ] Entity configuration helpers

#### Phase 5: Polish
- [ ] Caching implementation
- [ ] Error handling refinement
- [ ] Documentation and testing
- [ ] Performance optimization

### 12. Testing Requirements

#### 12.1 Unit Tests
- Tool execution with mocked editor state
- Resource serialization/deserialization
- Error handling and validation

#### 12.2 Integration Tests
- Full MCP protocol handshake
- Tool execution with real editor state
- Resource updates and notifications

#### 12.3 Protocol Compliance
- Validate against MCP 2026-07-28 specification
- Test with official MCP client SDK
- Verify header requirements (`Mcp-Method`, `Mcp-Name`)

### 13. Engine Capabilities for AI Scene Building

These are LITIENGINE capabilities NOT currently exposed in utiLITI but essential for full AI-driven scene building.

#### 13.1 Entity System Extensions

| Capability | Description | MCP Exposure |
|------------|-------------|--------------|
| **Creature Behavior** | State machines, pathfinding, navigation | Tools: `set-creature-behavior`, `configure-navigation` |
| **Trigger Conditions** | Custom activation predicates, cooldowns | Tools: `set-trigger-condition`, `configure-trigger-logic` |
| **MapArea Definition** | Named regions for gameplay zones | Tools: `create-map-area`, `define-zone-properties` |
| **SoundSource Placement** | Ambient audio with range/volume | Tools: `add-sound-source`, `configure-audio-zone` |
| **LightSource Configuration** | Dynamic lighting with intensity/color | Tools: `add-light-source`, `configure-lighting` |

#### 13.2 Physics System Extensions

| Capability | Description | MCP Exposure |
|------------|-------------|--------------|
| **Forces** | Gravity, wind, magnetic, sticky forces | Tools: `add-force`, `configure-force-field` |
| **Collision Filtering** | Per-entity collision masks | Tools: `set-collision-filter`, `configure-physics-layer` |
| **Raycasting** | Line-of-sight, proximity queries | Tools: `query-physics`, `test-collision` |
| **Movement Controllers** | Custom movement behaviors | Tools: `set-movement-controller`, `configure-movement` |

#### 13.3 Combat System Extensions

| Capability | Description | MCP Exposure |
|------------|-------------|--------------|
| **Ability Definitions** | Skills with effects, cooldowns, ranges | Tools: `create-ability`, `configure-ability-effects` |
| **Effect System** | Buffs, debuffs, damage-over-time | Tools: `add-effect`, `configure-effect-chain` |
| **Team Configuration** | Faction/alliance setup | Tools: `set-team`, `configure-factions` |
| **Combat Attributes** | Hitpoints, armor, damage modifiers | Tools: `set-combat-stats`, `configure-attributes` |

#### 13.4 Graphics System Extensions

| Capability | Description | MCP Exposure |
|------------|-------------|--------------|
| **AmbientLight Control** | Global lighting color/intensity | Tools: `set-ambient-light`, `configure-atmosphere` |
| **StaticShadow Configuration** | Shadow casting from geometry | Tools: `configure-shadows`, `set-shadow-type` |
| **Emitter Particles** | Particle system spawn rules | Tools: `configure-particle-system`, `set-emitter-rules` |
| **Camera Control** | Viewport, zoom, follow targets | Tools: `set-camera`, `configure-viewport` |
| **Render Layers** | Entity render ordering | Tools: `set-render-layer`, `configure-z-order` |

#### 13.5 Navigation & AI Extensions

| Capability | Description | MCP Exposure |
|------------|-------------|--------------|
| **A* Pathfinding** | Grid-based navigation | Tools: `configure-pathfinding`, `set-nav-grid` |
| **Entity Spawning** | Spawn rules, waves, conditions | Tools: `configure-spawning`, `set-spawn-rules` |
| **Behavior Trees** | Complex entity AI | Tools: `create-behavior-tree`, `configure-ai` |

#### 13.6 Environment Extensions

| Capability | Description | MCP Exposure |
|------------|-------------|--------------|
| **Gravity Configuration** | Per-map gravity settings | Tools: `set-gravity`, `configure-physics-world` |
| **Entity Tagging** | Tag-based entity groups | Tools: `tag-entity`, `query-by-tag` |
| **Custom Properties** | Arbitrary key-value entity data | Tools: `set-custom-property`, `get-entity-metadata` |

### 14. Extended Resources for AI Scene Building

| Resource URI | Description | MIME Type |
|--------------|-------------|-----------|
| `uti://engine/creatures` | All creature types with behaviors | `application/json` |
| `uti://engine/abilities` | Ability definitions and effects | `application/json` |
| `uti://engine/forces` | Force field configurations | `application/json` |
| `uti://engine/lights` | Light source presets | `application/json` |
| `uti://engine/sounds` | Sound source configurations | `application/json` |
| `uti://engine/navigation` | Pathfinding grid data | `application/json` |
| `uti://engine/physics` | Physics world settings | `application/json` |
| `uti://engine/rendering` | Render layer configuration | `application/json` |

### 15. Extended Tools for AI Scene Building

#### 15.1 Creature & Behavior Tools

| Tool Name | Description | Input Schema |
|-----------|-------------|--------------|
| `create-creature` | Create a creature with full configuration | `{ mapName, x, y, spritesheet, velocity, hitpoints, team, behavior }` |
| `configure-creature-behavior` | Set AI state machine | `{ entityId, states, transitions, initialState }` |
| `set-navigation` | Configure pathfinding for entity | `{ entityId, pathfinderType, speed, avoidCollision }` |

#### 15.2 Lighting & Atmosphere Tools

| Tool Name | Description | Input Schema |
|-----------|-------------|--------------|
| `create-light-source` | Add dynamic light | `{ mapName, x, y, width, height, intensity, color, shape, active }` |
| `set-ambient-light` | Configure global lighting | `{ mapName, color, intensity }` |
| `configure-shadow-casting` | Set shadow properties | `{ mapName, shadowType, opacity }` |

#### 15.3 Physics & Forces Tools

| Tool Name | Description | Input Schema |
|-----------|-------------|--------------|
| `create-force-field` | Add physics force | `{ mapName, type, x, y, strength, size, affectEnemies }` |
| `set-gravity` | Configure map gravity | `{ mapName, gravityX, gravityY }` |
| `configure-collision-filter` | Set collision rules | `{ entityId, canCollideWith, collisionType }` |

#### 15.4 Combat & Abilities Tools

| Tool Name | Description | Input Schema |
|-----------|-------------|--------------|
| `create-ability` | Define a new ability | `{ name, cooldown, castType, impact, impactAngle, effects }` |
| `assign-ability` | Give ability to creature | `{ creatureId, abilityName }` |
| `configure-effect` | Set effect properties | `{ effectType, duration, magnitude, targetAttribute }` |

#### 15.5 Trigger & Gameplay Tools

| Tool Name | Description | Input Schema |
|-----------|-------------|--------------|
| `create-trigger` | Add trigger with logic | `{ mapName, x, y, activation, message, targets, cooldown, conditions }` |
| `set-trigger-condition` | Add activation predicate | `{ triggerId, conditionType, parameters }` |
| `create-map-area` | Define gameplay zone | `{ mapName, x, y, width, height, name, tags }` |

#### 15.6 Audio & Atmosphere Tools

| Tool Name | Description | Input Schema |
|-----------|-------------|--------------|
| `create-sound-source` | Add ambient audio | `{ mapName, x, y, sound, volume, range, loop }` |
| `configure-audio-zone` | Set audio properties | `{ zoneId, reverb, occlusion, priority }` |

### 16. Extended Prompts for AI Scene Building

| Prompt Name | Description | Arguments |
|-------------|-------------|-----------|
| `design-level` | Complete level design workflow | `theme, difficulty, size, enemyTypes` |
| `create-combat-arena` | Battle area setup | `enemyCount, waveCount, powerups` |
| `setup-lighting-scene` | Atmospheric lighting | `mood, timeOfDay, lightSources` |
| `configure-puzzle-area` | Puzzle mechanics | `puzzleType, triggers, rewards` |
| `create-boss-arena` | Boss fight setup | `bossType, phases, mechanics` |
| `setup-ambient-audio` | Soundscape design | `mood, ambientSounds, music` |

### 17. Implementation Phases (Updated)

#### Phase 1: Core Infrastructure
- [ ] MCP server skeleton with HTTP transport
- [ ] `server/discover` implementation
- [ ] Basic tool registration and execution
- [ ] User consent dialogs

#### Phase 2: Basic Resources & Tools
- [ ] Project metadata resources
- [ ] Map data resources
- [ ] Asset metadata resources
- [ ] Editor state resources
- [ ] Project management tools
- [ ] Basic map editing tools

#### Phase 3: Entity System
- [ ] Creature creation and configuration
- [ ] Prop and trigger placement
- [ ] Spawnpoint setup
- [ ] MapArea definition

#### Phase 4: Lighting & Atmosphere
- [ ] LightSource tools
- [ ] AmbientLight control
- [ ] StaticShadow configuration
- [ ] SoundSource placement

#### Phase 5: Physics & Combat
- [ ] Force field tools
- [ ] Collision configuration
- [ ] Ability system integration
- [ ] Effect chain setup

#### Phase 6: AI & Behavior
- [ ] Creature behavior trees
- [ ] Navigation/pathfinding config
- [ ] Trigger logic systems
- [ ] Entity spawning rules

#### Phase 7: Advanced Features
- [ ] Emitter particle systems
- [ ] Camera control
- [ ] Render layer management
- [ ] Custom property system

#### Phase 8: Polish
- [ ] Caching implementation
- [ ] Error handling refinement
- [ ] Documentation and testing
- [ ] Performance optimization

### 18. Future Considerations

- **MCP Apps**: Interactive UI elements for complex configurations
- **Tasks Extension**: Long-running operations (batch imports, map generation)
- **Multi-user Support**: Collaborative editing via MCP
- **Plugin System**: Third-party tool and resource extensions
- **Procedural Generation**: AI-driven map/level generation workflows
- **Runtime Integration**: Live game editing via MCP
- **Version Control**: Map/entity versioning and diff support
