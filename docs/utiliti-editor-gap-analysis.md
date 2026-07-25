# utiLITI Editor Gap Analysis for MCP Integration

## Overview

This document analyzes what the utiLITI editor already provides and what needs to be extended to support full AI scene building via MCP.

---

## 1. Currently Implemented Editor Controls

### 1.1 Entity Property Panels

| Panel | Status | Controls |
|-------|--------|----------|
| **PropPanel** | ✅ Complete | Sprite, material, rotation, shadow, flip H/V, scale |
| **CreaturePanel** | ✅ Complete | Sprite, direction, scale, start dead |
| **TriggerPanel** | ✅ Complete | Activation type, message, cooldown, one-time, activators, targets |
| **LightSourcePanel** | ✅ Complete | Shape (ellipse/rectangle), color, intensity, active |
| **SoundPanel** | ✅ Complete | Sound resource, volume, range, loop |
| **SpawnpointPanel** | ✅ Complete | Direction, pivot, offset X/Y, entity info |
| **EmitterPanel** | ✅ Complete | Full property tabs (emission, appearance, transform, physics) |
| **StaticShadowPanel** | ✅ Complete | Shadow type, offset |
| **CollisionBoxPanel** | ✅ Complete | Collision type, obstructing lights |

### 1.2 Common Property Panels

| Panel | Status | Controls |
|-------|--------|----------|
| **CollisionPanel** | ✅ Complete | Has collision, width, height, align, valign, type |
| **CombatPanel** | ✅ Complete | Hitpoints, team, indestructible |
| **MovementPanel** | ✅ Complete | Velocity, acceleration, deceleration, turn on move |
| **CustomPanel** | ✅ Complete | Arbitrary key-value properties |
| **TagPanel** | ✅ Complete | Entity tagging |

### 1.3 Inspector System

| Component | Status | Controls |
|-----------|--------|----------|
| **MapObjectInspector** | ✅ Complete | Name, ID, layer, render type, tags, implementation, transform (x, y, w, h) |
| **Expandable Cards** | ✅ Complete | Accordion-style property sections |

### 1.4 Map Editing Tools

| Tool | Status | Description |
|------|--------|-------------|
| **PointerTool** | ✅ Complete | Selection and transform |
| **StampBrushTool** | ✅ Complete | Tile painting |
| **BucketFillTool** | ✅ Complete | Flood fill |
| **EraserTool** | ✅ Complete | Tile erasing |
| **TerrainBrushTool** | ✅ Complete | Terrain painting |

---

## 2. Missing Editor Controls (Gaps)

### 2.1 Entity System Gaps

| Missing Panel | Engine Class | Priority | MCP Tools Affected |
|---------------|--------------|----------|-------------------|
| **CreatureBehaviorPanel** | `StateMachine`, `EntityNavigator` | High | `configure-creature-behavior`, `set-navigation` |
| **AbilityPanel** | `Ability`, `AbilityAttributes` | Medium | `create-ability`, `assign-ability` |
| **EffectPanel** | `Effect` | Medium | `configure-effect`, `configure-effect-chain` |
| **SpawnRulePanel** | `EntitySpawner` | Medium | `configure-spawning`, `set-spawn-rules` |

> **Note**: MapArea is handled by the general MapObjectInspector - it has no unique properties beyond name, position, size, and tags.

### 2.2 Physics System Gaps

| Missing Panel | Engine Class | Priority | MCP Tools Affected |
|---------------|--------------|----------|-------------------|
| **ForceFieldPanel** | `Force`, `GravityForce` | High | `add-force`, `configure-force-field` |
| **CollisionFilterPanel** | `ICollisionEntity` | Medium | `set-collision-filter`, `configure-physics-layer` |
| **PhysicsWorldPanel** | `PhysicsEngine` | Medium | `set-gravity`, `configure-physics-world` |

### 2.3 Graphics System Gaps

| Missing Panel | Engine Class | Priority | MCP Tools Affected |
|---------------|--------------|----------|-------------------|
| **CameraControlPanel** | `Camera`, `FreeFlightCamera` | Low | `set-camera`, `configure-viewport` |
| **RenderLayerPanel** | `RenderType` | Low | `set-render-layer`, `configure-z-order` |

> **Note**: AmbientLight is already configured in MapPropertyPanel via the Lighting card (ambient color, shadow color, preview).

### 2.4 Navigation & AI Gaps

| Missing Panel | Engine Class | Priority | MCP Tools Affected |
|---------------|--------------|----------|-------------------|
| **PathfindingPanel** | `AStarPathFinder`, `AStarGrid` | Medium | `configure-pathfinding`, `set-nav-grid` |
| **BehaviorTreePanel** | `StateMachine`, `State` | Medium | `create-behavior-tree`, `configure-ai` |

### 2.5 Environment Gaps

| Missing Panel | Engine Class | Priority | MCP Tools Affected |
|---------------|--------------|----------|-------------------|
| **GravityPanel** | `Environment.gravity` | Medium | `set-gravity`, `configure-physics-world` |
| **SpawnConfigPanel** | `EntitySpawner` | Medium | `configure-spawning`, `set-spawn-rules` |

---

## 3. Priority Matrix

### High Priority (Must Have for AI Scene Building)

1. **CreatureBehaviorPanel** - Configure AI state machines
2. **ForceFieldPanel** - Add physics forces

### Medium Priority (Should Have)

5. **AbilityPanel** - Define creature abilities
6. **EffectPanel** - Configure buffs/debuffs
7. **CollisionFilterPanel** - Fine-tune collision
8. **PhysicsWorldPanel** - World physics settings
9. **PathfindingPanel** - Navigation configuration
10. **SpawnRulePanel** - Entity spawning rules
11. **GravityPanel** - Per-map gravity

### Low Priority (Nice to Have)

12. **CameraControlPanel** - Viewport configuration
13. **RenderLayerPanel** - Render ordering
14. **BehaviorTreePanel** - Complex AI logic

---

## 4. Recommended Implementation Plan

### Phase 1: Core Missing Panels

```
ForceFieldPanel
├── Force type (gravity, wind, magnetic, sticky)
├── Strength
├── Size/radius
├── Affect allies/enemies
└── Cancel on collision/reached
```

### Phase 2: Entity Behavior Panels

```
CreatureBehaviorPanel
├── State machine editor
│   ├── States (idle, patrol, chase, attack)
│   ├── Transitions
│   └── Conditions
├── Pathfinding config
│   ├── Speed
│   ├── Avoid collision
│   └── Navigation grid
└── AI parameters

AbilityPanel
├── Ability name/description
├── Cooldown
├── Cast type (instant, channel, charge)
├── Impact area (shape, size, angle)
├── Effects list
│   ├── Effect type
│   ├── Duration
│   ├── Magnitude
│   └── Target attribute
└── Requirements
```

### Phase 3: Physics & Environment

```
PhysicsWorldPanel
├── Global gravity X/Y
├── World bounds
├── Collision layers
└── Debug visualization

SpawnRulePanel
├── Entity type
├── Spawn count
├── Spawn interval
├── Spawn conditions
├── Wave configuration
└── Spawn points
```

---

## 5. MCP Tool Mapping

### Tools That Can Be Implemented Now (Using Existing Panels)

| MCP Tool | Existing Panel | Notes |
|----------|----------------|-------|
| `get-project-info` | Editor | Direct API access |
| `list-maps` | MapList | Direct API access |
| `list-assets` | AssetTree | Direct API access |
| `get-map-info` | MapComponent | Direct API access |
| `create-map` | NewMapDialog | Dialog exists |
| `add-entity` | PointerTool | Tool exists |
| `remove-entity` | PointerTool | Tool exists |
| `import-spritesheet` | SpritesheetImportPanel | Panel exists |
| `import-sound` | SoundPanel | Panel exists |

### Tools Requiring New Panels

| MCP Tool | New Panel Required |
|----------|-------------------|
| `create-map-area` | MapAreaPanel |
| `configure-creature-behavior` | CreatureBehaviorPanel |
| `create-force-field` | ForceFieldPanel |
| `set-ambient-light` | AmbientLightPanel |
| `create-ability` | AbilityPanel |
| `configure-pathfinding` | PathfindingPanel |
| `set-gravity` | GravityPanel |
| `configure-spawning` | SpawnRulePanel |

---

## 6. Integration Strategy

### For MCP Server Implementation

1. **Use Existing Panels**: Many MCP tools can leverage existing editor functionality
2. **Extend PropertyPanel System**: New panels should follow the existing `PropertyPanel` pattern
3. **MapObjectProperty Extension**: Add new properties for missing features
4. **MapObjectType Extension**: Register new entity types if needed

### For Editor UI Extension

1. **Follow Existing Patterns**: Use `PropertyPanel`, `ExpandableCard`, `MapObjectPropertyActionListener`
2. **Integrate with Inspector**: Add new panels to `MapObjectInspector`
3. **Maintain Undo Support**: Use `UndoManager` for all changes
4. **Add Preview Support**: Visual feedback for new features

---

## 7. Summary

### What's Already Good
- Entity property panels are comprehensive
- Inspector system is well-architected
- Tool system is extensible
- Custom properties provide flexibility
- Map-level settings (gravity, ambient light, shadows) are in MapPropertyPanel

### What Needs Work
- 12 new panels needed for full AI scene building
- Physics forces UI is completely missing
- AI behavior configuration is missing
- Ambient light is already in MapPropertyPanel (not a gap)

### Estimated Effort
- **High Priority Panels**: ~2-3 weeks
- **Medium Priority Panels**: ~4-6 weeks
- **Low Priority Panels**: ~2-3 weeks
- **Total**: ~8-12 weeks for full MCP support
