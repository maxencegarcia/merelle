# Boardifier Framework - Comprehensive Structural Analysis

## Executive Summary

**Boardifier** is an extensible Java framework for building board-based and sprite-based games using a classic **MVC (Model-View-Controller)** architecture enhanced with:
- Event-driven state management
- Action/command system for orchestrated gameplay
- Frame-based animation engine
- Reflection-based factory pattern for dynamic stage loading
- Console-based character rendering

The framework provides the infrastructure; developers create game-specific implementations by extending abstract classes and registering custom types.

---

## Project Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     BOARDIFIER FRAMEWORK                    │
├─────────────────────────────────────────────────────────────┤
│  Model Layer          │  Control Layer      │  View Layer    │
│  (Game State)         │  (Coordination)     │  (Rendering)   │
├───────────────────────┼─────────────────────┼────────────────┤
│ Model                 │ Controller          │ View           │
│ GameStageModel        │ StageFactory        │ GameStageView  │
│ GameElement           │ ActionFactory       │ RootPane       │
│ Player                │ ActionPlayer        │ ElementLook    │
│ Event/EventQueue      │ Decider             │ ContainerLook  │
│ ContainerElement      │ Logger              │ GridLook       │
│ SpriteElement         │                     │ TableLook      │
│ TextElement           │                     │ TextLook       │
│ BackgroundElement     │                     │                │
│ DiceElement           │                     │                │
│ Animation System      │                     │                │
│ Action System         │                     │                │
└───────────────────────┴─────────────────────┴────────────────┘
```

---

## 1. MODEL LAYER (Game State & Data Structures)

### 1.1 Core Model Management

#### **Model.java** (Framework Base Class)
**Purpose**: Central game state manager
```
Responsibilities:
- Global game state management (INIT, PLAY, PAUSED, ENDSTAGE, ENDGAME)
- Frame-based timing (FPS control via frameGap in nanoseconds)
- Player management (list, current player tracking)
- Current stage reference
- Event capture toggles (mouse, keyboard, action events)
- Last click coordinates

Key Attributes:
- state: int - current game phase
- frameGap: long - nanoseconds between frame updates (default: 10ms)
- lastFrame: long - timestamp for FPS regulation
- players: List<Player>
- idPlayer: int - current player index
- idWinner: int - winner identification (-1 if none)
- gameStageModel: GameStageModel - active stage
- captureMouseEvent, captureKeyEvent, captureActionEvent: boolean

Key Methods:
- startGame(GameStageModel) - initialize stage
- update() - called each frame
- reset() - reset to initial state
```

**Design Pattern**: Singleton-like access point for game state

---

#### **GameStageModel.java** (Framework Abstract Base Class)
**Purpose**: Stage/level-specific game model container
```
Responsibilities:
- Manage all elements in a stage
- Track selected elements
- Manage container elements (for board games)
- Invoke callbacks for container operations
- Stage-specific state management

Key Attributes:
- name: String - unique stage identifier (must match StageFactory registration)
- model: Model - parent game model
- state: int - local stage state
- elements: List<GameElement> - all game objects in stage
- containers: List<ContainerElement> - grid-based containers
- selected: List<GameElement> - currently selected elements
- Callbacks:
  - onSelectionChangeCallback
  - onPutInContainerCallback
  - onMoveInContainerCallback
  - onRemoveFromContainerCallback

Key Methods:
- createElements(StageElementsFactory) - stage setup (abstract, must override)
- getElements(), getContainers()
- addElement(GameElement), removeElement(GameElement)
- Selection management methods
```

**Design Pattern**: Template Method (createElements is abstract)

---

#### **Player.java** (Framework Concrete Class)
**Purpose**: Player representation in game
```
Attributes:
- type: int - HUMAN or COMPUTER constant
- name: String - player name
- keyPressed: List<String> - currently pressed keys

Factory Methods:
- createHumanPlayer(String name) - static factory
- createComputerPlayer(String name) - static factory

Key Methods:
- getType(), getName()
- Key input tracking: addKeyPressed(), removeKeyPressed(), isKeyPressed()
- reset() - clear state between turns
```

**Design Pattern**: Factory Method (static creation)

---

### 1.2 Game Element Hierarchy

#### **GameElement.java** (Framework Abstract Base Class)
**Purpose**: All game objects inherit from this
```
Responsibilities:
- Position and visibility management
- Container relationship tracking
- Animation hosting
- Event queue management
- Selection and clickability flags

Key Attributes:
- x, y: double - location in space
- gameStageModel: GameStageModel - owning stage
- visible: boolean - render flag
- selected: boolean - selection state
- clickable: boolean - interactable flag
- type: int - registered element type
- container: ContainerElement - parent container (null if in stage)
- animation: Animation - current animation
- eventQueue: EventQueue - state change events

Key Abstract Methods:
- update() - called each frame
- setLocation(double x, double y)

Key Concrete Methods:
- isVisible(), setVisible()
- isSelected(), setSelected()
- getContainer(), setContainer()
- Event queue accessors
- Animation management
```

**Design Pattern**: Composite (elements can be in containers)

---

#### **StaticElement.java** (Framework Abstract Class)
**Purpose**: Non-moving game objects
```
Characteristics:
- Extends GameElement
- Overrides setLocation() as no-op (cannot move)

Subclasses:
- BackgroundElement - background graphics
- ContainerElement - grid containers
```

---

#### **SpriteElement.java** (Framework Concrete Class)
**Purpose**: Animated, moving game entities
```
Attributes:
- nbFaces: int - number of animation frames
- faceIndexes: List<Integer> - sequence of face indices to display
- currentIndex: int - current position in faceIndexes
- state: int - sprite state (IDLE, MOVING, FALLING, COLLIDING, JUMPING)
- xSpeed, ySpeed: double - velocity
- framesPerFaceChange: int - animation speed

States:
- SPRITE_STATE_IDLE = 0
- SPRITE_STATE_MOVING = 1
- SPRITE_STATE_FALLING = 2
- SPRITE_STATE_COLLIDE = 3
- SPRITE_STATE_JUMPING = 4

Movement Constants:
- MOVE_NONE = -1, MOVE_RIGHT = 0, MOVE_UP = 1, MOVE_LEFT = 2, MOVE_DOWN = 3

Key Methods:
- update() - override for sprite-specific behavior
- Face/animation management
- Speed control (setXSpeed, setYSpeed, stopMoving)

Use Cases:
- Game pieces on a board
- Moving sprites/characters
- Falling/physics-based objects
```

---

#### **TextElement.java** (Framework Concrete Class)
**Purpose**: Dynamic text display
```
Attributes:
- text: String - displayed text

Key Methods:
- getText(), setText()
- update() - animates text position if animation present
```

---

#### **DiceElement.java** (Framework Concrete Class)
**Purpose**: Dice/randomizer elements
```
Extends: SpriteElement

Attributes:
- nbSides: int - number of sides

Key Methods:
- getNbSides()
- setCurrentValue(int) - show specific face (1 to nbSides)
- getCurrentValue() - get current face value
```

---

#### **BackgroundElement.java** (Framework Concrete Class)
**Purpose**: Static background graphics
```
Extends: StaticElement
- Type: ElementTypes.getType("background")
```

---

#### **ContainerElement.java** (Framework Concrete Class)
**Purpose**: Grid-based element storage (for board games)
```
Extends: StaticElement

Attributes:
- name: String - container identifier
- nbRows, nbCols: int - grid dimensions
- grid: List<GameElement>[][] - 2D cell array
- rowSpans, colSpans: int[][] - cell spanning for merged cells
- reachableCells: boolean[][] - cell accessibility flags
- Alignment/padding arrays for layout control

Key Methods:
- addElement(GameElement, row, col)
- removeElement(GameElement)
- getCellContent(row, col)
- Move element within grid
- Spanning cell support: setCellSpan()
- Reachability control: setReachable(), isReachable()

Callbacks:
- onPutInContainerCallback
- onMoveInContainerCallback
- onRemoveFromContainerCallback
```

---

### 1.3 Type System

#### **ElementTypes.java** (Framework Registry)
**Purpose**: Extensible type system for game elements
```
Predefined Types:
- "basic" (0) - basic element
- "static" (1) - non-moving element
- "background" (3) - background layer
- "container" (10) - grid container
- "text" (20) - text element
- "sprite" (30) - animated sprite
- "dice" (31) - dice element

Key Methods:
- getType(String name) - lookup type number
- register(String name, int typeNumber) - add custom type
- isValid(int typeNumber), isValid(String name) - validation

Design Pattern**: Registry (extensible, validated)
```

---

### 1.4 Event System

#### **Event.java** (Framework Immutable Class)
**Purpose**: Game state change notifications
```
Event Types:
- LOCATION - position changed
- VISIBILITY - visibility toggled
- SELECTION - selection changed
- IN_CONTAINER - element entered container
- OUT_CONTAINER - element left container
- MOVE_CONTAINER - element moved within container
- FACE - sprite face/image changed

Structure:
- type: EventType
- params: Object[5] - up to 5 parameters
- paramCount: int

Key Methods:
- addParameter(Object)
- getParameter(int index)
- Type-specific query methods (isLocationEvent, isInContainerEvent, etc.)

Design Pattern**: Type-safe event envelope
```

---

#### **EventQueue.java** (Framework Concrete Class)
**Purpose**: Queue of pending events per element
```
Attributes:
- queue: Event[1000] - fixed-size event array
- size: int - current queue length

Key Methods:
- addChangeLocationEvent()
- addChangeVisibilityEvent()
- addChangeSelectionEvent()
- addChangeFaceEvent()
- addPutInContainerEvent(ContainerElement, row, col)
- addRemoveFromContainerEvent(ContainerElement, row, col)
- addMoveInContainerEvent(rowSrc, colSrc, rowDest, colDest)
- getEvent(index), removeEvent(index)
- clear()

Design Pattern**: FIFO Queue, Notification (events posted, processed later)
```

---

### 1.5 Action System

#### **GameAction.java** (Framework Abstract Base Class)
**Purpose**: Orchestrated game operations (Command Pattern)
```
Responsibilities:
- Execute game state changes
- Coordinate animations
- Manage callbacks on completion

Key Attributes:
- model: Model
- element: GameElement - target element
- animationName: String - animation type name
- animationType: int - animation type constant
- animation: Animation - created animation
- animateBeforeExecute: boolean - play animation before action?
- onEndCallback: ActionCallback

Key Abstract Methods:
- execute() - perform game logic changes
- createAnimation() - create associated animation (abstract)

Key Methods:
- setupAnimation() - prepare animation for playback
- getAnimation(), getElement()
- onActionEnd(ActionCallback) - register completion handler
- onAnimationEnd(AnimationCallback)

Subclasses:
- PutInContainerAction - add element to container
- RemoveFromContainerAction - remove from container
- MoveWithinContainerAction - move within container
- RemoveFromStageAction - remove from stage
- DrawDiceAction - roll dice

Design Pattern**: Command (encapsulates request), Template Method (execute is abstract)
```

---

#### **ActionList.java** (Framework Concrete Class)
**Purpose**: Organize actions for orchestrated execution
```
Attributes:
- actions: List<List<GameAction>> - action "packs"
- currentPack: List<GameAction> - current pack being built
- doEndOfTurn: boolean - trigger turn change after execution?

Key Methods:
- addActionPack() - start new parallel action group
- addSingleAction(GameAction) - add single action
- addPackAction(GameAction) - add to current pack
- addAll(ActionList) - merge lists
- getActions() - retrieve all action packs
- setDoEndOfTurn(boolean)

Execution Model:
- Actions in same pack execute in parallel
- Packs execute sequentially
- Enables: move A, then move B (sequential) vs move A & move B (parallel)

Design Pattern**: Composite (groups of commands), Builder (fluent construction)
```

---

#### **ActionFactory.java** (Framework Concrete Class)
**Purpose**: Factory for common game actions
```
Static Factory Methods:
- generatePutInContainer() - add element to container (with remove if needed)
- generateMoveWithinContainer() - reposition in container
- generateRemoveFromContainer() - remove from container
- generateRemoveFromStage() - delete element
- generateDrawDice() - simulate dice roll

Returns ActionList (can be modified before execution)

Design Pattern**: Factory Method (static convenience constructors)
```

---

#### **ActionCallback.java** (Framework Interface)
**Purpose**: Action completion notification
```
@FunctionalInterface
void execute()

Allows: action.onActionEnd(() -> { /* handle completion */ })
```

---

### 1.6 Animation System

#### **Animation.java** (Framework Abstract Base Class)
**Purpose**: Time-based visual changes
```
Responsibilities:
- Manage animation lifecycle
- Generate animation steps (frame data)
- State management

Key Attributes:
- duration: int - milliseconds
- frameGap: int - milliseconds between frames
- state: AnimationState - current animation state
- animationStep: int - current step index
- steps: List<AnimationStep> - computed step data
- type: int - animation type
- onEndCallback: AnimationCallback

States:
- OFF, STARTED, PAUSED

Key Methods:
- start(), pause(), stop()
- computeSteps() - generate frame data (abstract)
- next() - get next step (returns null when complete)
- getType(), getName()
- onEnd(AnimationCallback) - register completion handler

Subclasses:
- LinearMoveAnimation - smooth movement
- MoveAnimation - teleport/instant move
- WaitAnimation - wait N frames
- FaceAnimation - single sprite face
- CyclicFaceAnimation - cycle through faces

Design Pattern**: State Machine (animation states), Template Method (computeSteps)
```

---

#### **AnimationTypes.java** (Framework Registry)
**Purpose**: Extensible animation type system
```
Predefined Types:
- none (-1)
- move/teleport (10) - instant move
- move/linearcst (11) - constant speed movement
- move/linearprop (12) - proportional (frame-based) movement
- look/simple (20) - single frame display
- look/sequence (21) - cycle through frames
- look/random (22) - random frame selection
- wait/frames (30) - wait N frames

Key Methods:
- getType(String name), getName(int type)
- register(String name, int typeNumber)
- isValid(int type), isValid(String name)

Naming Convention**: "category/action" (e.g., "move/linear")

Design Pattern**: Type Registry
```

---

#### **AnimationState.java**, **AnimationStep.java**, **AnimationCallback.java**
**Purpose**: Support classes for animation lifecycle
```
- AnimationState: Tracks ON/OFF, STARTED, PAUSED
- AnimationStep: Single frame data (contains Object[] for parameters)
- AnimationCallback: @FunctionalInterface for completion handlers
```

---

### 1.7 Utilities

#### **Coord2D.java** (Framework Concrete Class)
**Purpose**: 2D coordinate with arithmetic
```
Attributes:
- x, y: double

Operations:
- add(dx, dy) - return new Coord2D
- subtract(dx, dy) - return new Coord2D
- get/set X and Y
```

---

#### **GameException.java** (Framework Concrete Class)
**Purpose**: Game-specific exception
```
Extends Exception
Optionally includes problematic GameElement for context
```

---

#### **Callbacks** (Framework Interfaces)
```
- SelectionCallback.execute() - selection changed
- ContainerOpCallback.execute(element, container, row, col) - container operation
```

---

## 2. CONTROL LAYER (Game Logic Coordination)

### 2.1 Game Controller

#### **Controller.java** (Framework Abstract Base Class)
**Purpose**: Main game loop coordinator and event processor
```
Responsibilities:
- Game/stage lifecycle (start, stop, pause)
- Event processing from model
- Element interaction handling
- Animation update coordination
- View synchronization

Key Attributes:
- model: Model
- view: View
- firstStageName: String
- mapElementLook: Map<GameElement, ElementLook>
- frameNumber: long

Key Abstract Methods:
- stageLoop() - custom game loop implementation (abstract)

Key Concrete Methods:
- startGame() - game entry point
- startStage(stageName) - load and initialize stage
  - Creates GameStageModel via StageFactory
  - Creates GameStageView via StageFactory
  - Calls gameStageModel.createElements()
  - Calls gameStageView.createLooks()
  - Processes initial events
- stopStage()
- processEvents() - main event pipeline
  - processContainerEvents() - handle element moves
  - updateElements() - call element.update()
  - processLookEvents() - handle visual updates

Lifecycle:
1. Controller created with Model and View
2. setFirstStageName(stageName) configured
3. startGame() called → loads first stage
4. stageLoop() executed (subclass implements)
5. Within loop: actions executed, events processed, views updated
6. endStage() → next stage or game end

Design Pattern**: Abstract Factory (via StageFactory), Template Method (stageLoop)
```

---

### 2.2 Factory Classes

#### **StageFactory.java** (Framework Concrete Class)
**Purpose**: Dynamic stage instantiation via reflection
```
Static Maps:
- stageModelNames: Map<String, ClassName>
- stageViewNames: Map<String, ClassName>

Key Static Methods:
- registerModelAndView(stageName, modelClassName, viewClassName)
  - Called during game initialization
  - Maps stage name to fully-qualified class names
  
- createStageModel(stageName, Model) - reflection instantiation
  - Looks up class name
  - Uses getDeclaredConstructor(String.class, Model.class)
  - Returns new instance of GameStageModel subclass
  
- createStageView(stageName, GameStageModel) - reflection instantiation
  - Looks up class name
  - Uses getDeclaredConstructor(String.class, GameStageModel.class)
  - Returns new instance of GameStageView subclass

Example Usage:
```java
StageFactory.registerModelAndView("level1", "com.game.Level1Model", "com.game.Level1View");
GameStageModel stage = StageFactory.createStageModel("level1", model);
```

Design Pattern**: Factory (uses reflection for dynamic instantiation)
```

---

#### **ActionFactory.java** (Already covered in Model section)

---

### 2.3 Action Execution

#### **ActionPlayer.java** (Framework Concrete Class)
**Purpose**: Execute queued actions and coordinate with AI
```
Attributes:
- control: Controller
- model: Model
- decider: Decider - AI decision maker
- actions: ActionList - current action queue
- preActions: ActionList - actions before decision

Constructor Options:
1. ActionPlayer(model, control, decider, preActions)
   - Decider-based: for AI players
2. ActionPlayer(model, control, actions)
   - Direct actions: for user input

Key Method:
- start()
  1. Disable event capture (during action)
  2. Execute preActions if present
  3. If decider present: call decide() to get actions
  4. Execute all action packs
  5. Enable event capture (resume input)

Implementation:
- Loops through action packs
- For each pack, executes all GameAction.execute()
- Respects action ordering (packs sequential, actions within pack sequential)

Design Pattern**: Strategy (pluggable Decider), Command (executes ActionLists)
```

---

### 2.4 AI Decision Making

#### **Decider.java** (Framework Abstract Base Class)
**Purpose**: Pluggable AI/decision strategy
```
Attributes:
- model: Model
- control: Controller

Key Abstract Method:
- ActionList decide() - compute AI moves

Subclasses Implement:
- Game-specific AI logic
- Returns ActionList for ActionPlayer to execute

Design Pattern**: Strategy (pluggable decision algorithm)
```

---

### 2.5 Utilities

#### **Logger.java** (Framework Concrete Class)
**Purpose**: Configurable logging system
```
Log Levels:
- LOGGER_NONE (0) - disabled
- LOGGER_INFO (1) - informational messages
- LOGGER_DEBUG (2) - debug messages (game logic)
- LOGGER_TRACE (3) - framework trace messages

Verbosity Levels:
- VERBOSE_NONE - just message
- VERBOSE_BASIC - method/class name + message
- VERBOSE_HIGH - class/method/object reference + message

Static Methods:
- setLevel(int), setVerbosity(int)
- info(String, Object caller)
- debug(String, Object caller)
- trace(String, Object caller)

Design Pattern**: Singleton (static logger)
```

---

## 3. VIEW LAYER (Presentation & Rendering)

### 3.1 Main View Container

#### **View.java** (Framework Concrete Class)
**Purpose**: Top-level view manager
```
Attributes:
- model: Model - reference to model
- gameStageView: GameStageView - current stage view
- rootPane: RootPane - rendering engine

Key Methods:
- setView(GameStageView) - swap stage view
- getGameStageView()
- update() - call rootPane.update() and print()
- Trampoline methods:
  - getElementLook(GameElement)
  - getElementContainerLook(GameElement)

Responsibilities:
- Hold current stage view
- Coordinate rendering

Design Pattern**: Wrapper/Facade (wraps RootPane and GameStageView)
```

---

#### **RootPane.java** (Framework Concrete Class)
**Purpose**: Console-based rendering engine
```
Attributes:
- viewPort: String[][] - character buffer
- width, height: int
- gameStageView: GameStageView

Key Methods:
- init(GameStageView) - setup stage
  - Sort looks by depth (layering)
  - Attach rootPane reference to each look
  
- update() - render frame
  1. Calculate viewport dimensions
  2. Clear buffer
  3. Sort looks by depth
  4. Render each visible element's look
  5. Update viewport size if needed
  
- print() - output to console
- clearViewPort()

Rendering Model:
- Character-based console display
- 2D String array represents pixels
- Depth sorting for layering (negative depth = lower layer)
- Automatic viewport sizing based on element bounds

Design Pattern**: Renderer (encapsulates rendering logic)
```

---

### 3.2 Stage View

#### **GameStageView.java** (Framework Abstract Base Class)
**Purpose**: Stage-specific visual presentation
```
Attributes:
- name: String - stage identifier (matches GameStageModel name)
- gameStageModel: GameStageModel - associated model
- looks: List<ElementLook> - visual representations
- width, height: int - viewport dimensions (-1 = auto-size)

Key Methods:
- createLooks() - abstract, must override to create ElementLook instances
- addLook(ElementLook)
- getElementLook(GameElement) - find look for element
- getLooks()
- update() - optional stage-specific rendering updates
- setGameStageView() - attach to RootPane

Responsibilities:
- Manage ElementLook instances (one per GameElement)
- Define viewport dimensions
- Create and configure looks in createLooks()

Design Pattern**: Abstract Factory (looks are created by subclass)
```

---

### 3.3 Element Looks (Visual Representations)

#### **ElementLook.java** (Framework Abstract Base Class)
**Purpose**: Visual representation of a GameElement
```
Attributes:
- element: GameElement - represented element
- shape: String[][] - 2D character buffer
- width, height: int - visual dimensions
- depth: int - layering (negative = below 0 = above)
- anchorType: int - ANCHOR_CENTER or ANCHOR_TOPLEFT
- parent: ElementLook - container look (if nested)
- rootPane: RootPane - reference for event handling

Anchor Types:
- ANCHOR_CENTER - center of look at (x, y)
- ANCHOR_TOPLEFT - top-left corner at (x, y)

Key Methods:
- update() - compute visual representation (abstract)
- draw() - render into viewport
- getElement(), getWidth(), getHeight()
- getDepth() - layering order
- hasParent(), setParent()
- setAnchor(), setRootPane()

Responsibilities:
- Convert GameElement data to visual (shape)
- Support depth-based layering
- Handle positioning

Design Pattern**: Adapter (converts GameElement to renderable format)
```

---

#### **ContainerLook.java** (Framework Concrete Class)
**Purpose**: Grid-based layout for container elements
```
Extends: ElementLook

Attributes:
- nbRows, nbCols: int - grid dimensions
- grid: List<ElementLook>[][] - nested looks
- rowHeight, colWidth: int - fixed or flexible cell size
- rowsHeight[], colsWidth[] - computed dimensions
- Alignment arrays: verticalAlignment[][], horizontalAlignment[][]
- Padding arrays: paddingTop[][], paddingBottom[][], etc.
- innersTop, innersLeft: int - offset of grid cells

Alignment Constants:
- ALIGN_TOP, ALIGN_MIDDLE, ALIGN_BOTTOM
- ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT

Key Methods:
- setVerticalAlignment(row, col, alignment)
- setHorizontalAlignment(row, col, alignment)
- setPaddingTop/Bottom/Left/Right()
- addInnerLook(ElementLook, row, col)
- computeDimensions() - calculate layout

Features:
- Cell spanning support (from ContainerElement)
- Flexible or fixed cell sizing
- Per-cell alignment and padding
- Nested look management

Design Pattern**: Composite (contains nested looks)
```

---

#### **GridLook.java** (Framework Concrete Class)
**Purpose**: Grid with border rendering
```
Extends: ContainerLook

Attributes:
- borderWidth: int - border thickness

Key Features:
- Draws grid borders
- Forces minimum cell size (1x1)
- Validates no spanning cells used (grids require regular cells)

Subclass Purpose**: Specialized ContainerLook for true grids
```

---

#### **TableLook.java** (Framework Concrete Class)
**Purpose**: Table-style grid with optional borders
```
Extends: ContainerLook

Attributes:
- borderWidth: int

Differences from GridLook:
- Allows spanning cells
- More flexible layout options

Subclass Purpose**: ContainerLook variant for table-like layouts
```

---

#### **TextLook.java** (Framework Concrete Class)
**Purpose**: Text element rendering
```
Responsibilities:
- Render TextElement text content
- Support text animation

Not detailed in explored files - extends ElementLook
```

---

#### **ClassicBoardLook.java** (Framework Concrete Class)
**Purpose**: Board-game-specific rendering
```
Not detailed in explored files - likely extends GameStageView or ElementLook

Purpose**: Custom rendering for classic board game style
```

---

### 3.4 Color Support

#### **ConsoleColor.java** (Framework Utility)
**Purpose**: Console color constants for colored text output
```
Provides: ANSI color codes for console rendering
Not detailed in exploration - provides color palette
```

---

## Design Patterns Used

1. **MVC (Model-View-Controller)**
   - Strict separation: Model (state) → View (render) ← Control (coordination)

2. **Abstract Factory (via Reflection)**
   - StageFactory creates GameStageModel and GameStageView subclasses dynamically

3. **Factory Method**
   - ActionFactory.generate*() methods
   - Player.createHumanPlayer(), createComputerPlayer()

4. **Template Method**
   - GameElement.update() (abstract, overridden)
   - GameAction.execute() and createAnimation() (abstract)
   - GameStageModel.createElements() (abstract)
   - GameStageView.createLooks() (abstract)
   - Controller.stageLoop() (abstract)
   - Animation.computeSteps() (abstract)
   - ElementLook.update() (abstract)

5. **Strategy Pattern**
   - Decider subclasses provide pluggable AI strategies
   - Different Animation subclasses for different animation types

6. **Command Pattern**
   - GameAction encapsulates game operations
   - ActionList orchestrates commands

7. **Observer/Event Pattern**
   - EventQueue notifies of state changes (LOCATION, VISIBILITY, SELECTION, etc.)
   - Callbacks (ActionCallback, AnimationCallback, etc.) react to events

8. **State Pattern**
   - Model has game states (INIT, PLAY, PAUSED, ENDSTAGE, ENDGAME)
   - SpriteElement has sprite states (IDLE, MOVING, FALLING, etc.)
   - Animation has animation states (OFF, STARTED, PAUSED)
   - AnimationState manages animation lifecycle

9. **Composite Pattern**
   - GameElement hierarchy with containers
   - ElementLook hierarchy with ContainerLook

10. **Type Registry Pattern**
    - ElementTypes registry for game element types
    - AnimationTypes registry for animation types
    - Extensible type system

11. **Adapter Pattern**
    - ElementLook adapts GameElement to renderable format

12. **Callback Pattern**
    - Pervasive use of functional interfaces for event handling
    - onAnimationEnd, onActionEnd, onSelectionChange callbacks

---

## Framework vs. Project-Specific Components

### Framework (Provided/Reusable)
```
MODEL:
- Model, GameElement, GameStageModel, Player
- SpriteElement, StaticElement, TextElement, DiceElement, BackgroundElement
- ContainerElement
- Event, EventQueue, Coord2D, GameException
- ElementTypes, AnimationTypes
- Animation and subclasses (LinearMoveAnimation, MoveAnimation, WaitAnimation, etc.)
- GameAction base class and standard subclasses
- ActionList, ActionCallback

VIEW:
- View, GameStageView, RootPane
- ElementLook and hierarchy (ContainerLook, GridLook, TableLook, TextLook)
- ConsoleColor

CONTROL:
- Controller (abstract base)
- StageFactory, ActionFactory
- ActionPlayer
- Decider (abstract base)
- Logger
```

### Project-Specific (Must Implement)
```
MODEL:
- Concrete GameStageModel subclasses for each stage/level
- Custom GameElement subclasses if needed (extends SpriteElement, StaticElement)
- Custom StageElementsFactory subclasses for element creation
- Custom animation types (extend Animation)
- Custom action types (extend GameAction)

CONTROL:
- Concrete Controller subclass implementing stageLoop()
- Concrete Decider subclass for AI logic
- Stage name registration in StageFactory
- Game initialization code

VIEW:
- Concrete GameStageView subclasses for each stage
- Custom ElementLook subclasses for special rendering
- Look creation and configuration
```

---

## Main Dependencies Graph

```
Model (Game State)
├── requires: Player, GameStageModel, Coord2D, GameException
├── contains: List<Player>, List<GameElement> (via GameStageModel)
└── interacts with: Animation, Event, EventQueue

GameStageModel
├── contains: List<GameElement>, List<ContainerElement>
├── requires: GameElement, ContainerElement, Event callbacks
└── interacts with: Model

GameElement
├── requires: GameStageModel, Animation, EventQueue, ContainerElement
├── base for: SpriteElement, StaticElement
└── contained by: GameStageModel, ContainerElement

ContainerElement (extends StaticElement)
├── contains: List<GameElement>[][] (grid)
└── interacts with: ContainerOpCallback

Animation
├── uses: AnimationTypes, AnimationState, AnimationStep
├── base for: LinearMoveAnimation, MoveAnimation, WaitAnimation, FaceAnimation
└── contains: List<AnimationStep>

GameAction
├── requires: Model, GameElement, Animation
├── base for: PutInContainerAction, RemoveFromContainerAction, etc.
└── contains: Animation

ActionList
├── contains: List<List<GameAction>>
└── used by: ActionPlayer, Decider

Controller
├── coordinates: Model, View
├── uses: StageFactory, ActionPlayer, Decider
├── manages: GameStageModel, GameStageView
└── calls: createElements(), createLooks()

View (Presentation)
├── contains: GameStageView, RootPane
├── renders: ElementLook instances
└── syncs with: Model state

GameStageView
├── contains: List<ElementLook>
├── manages: GameStageModel's visual representation
└── creates: ElementLook instances in createLooks()

RootPane (Renderer)
├── contains: String[][] viewport
├── renders: ElementLook instances (sorted by depth)
└── outputs: Console display

ElementLook
├── represents: GameElement
├── base for: ContainerLook, TextLook
└── parent/child: ContainerLook contains List<ElementLook>[][]

ContainerLook
├── extends: ElementLook
├── contains: List<ElementLook>[][] (nested looks)
└── base for: GridLook, TableLook
```

---

## Workflow: Game Execution

### Initialization Phase
```
1. Game developer:
   - Creates Model instance
   - Creates View instance (with Model)
   - Creates Controller subclass instance (with Model, View)
   - Registers stages: StageFactory.registerModelAndView()

2. Controller.setFirstStageName(stageName)

3. Controller.startGame()
```

### Stage Loading Phase
```
4. Controller.startStage(stageName)
   ├── StageFactory.createStageModel(stageName, model)
   │   └── Reflection: Class.forName() → getDeclaredConstructor() → newInstance()
   │
   ├── gameStageModel.createElements(factory) [SUBCLASS IMPLEMENTS]
   │   └── Creates GameElement instances, adds to stage
   │
   ├── StageFactory.createStageView(stageName, gameStageModel)
   │   └── Reflection: Class.forName() → getDeclaredConstructor() → newInstance()
   │
   ├── gameStageView.createLooks() [SUBCLASS IMPLEMENTS]
   │   └── Creates ElementLook instances for each GameElement
   │
   ├── Controller.processEvents()
   │   ├── processContainerEvents() - handle element insertions
   │   ├── updateElements() - call element.update()
   │   └── processLookEvents() - update visual state
   │
   └── model.startGame(gameStageModel)
       └── Sets stage as active
```

### Game Loop Phase
```
5. Controller.stageLoop() [SUBCLASS IMPLEMENTS]
   └── Each iteration:
       ├── Capture input (mouse, keyboard)
       ├── Create actions (from input or Decider)
       ├── ActionPlayer.start()
       │   ├── Execute preActions
       │   ├── If Decider: call decide() → get ActionList
       │   └── Execute all GameAction instances
       │       └── Each action:
       │           ├── setupAnimation() - prepare animation
       │           ├── execute() - modify game state
       │           └── animation.computeSteps() & play
       │
       ├── Controller.processEvents() - handle state change events
       │   ├── processContainerEvents() - element moves
       │   ├── updateElements() - call element.update()
       │   └── processLookEvents() - visual updates
       │
       ├── Model.update() - frame-based updates
       │   └── Call element.update() for all elements
       │       └── Update animations, sprite states, etc.
       │
       └── View.update() → RootPane.update() → print()
           └── Render to console
```

### Stage End Phase
```
6. When stage completes:
   ├── Controller.endStage()
   │   └── May call startStage(nextStageName)
   │
   └── Or if last stage:
       └── Game ends
```

---

## Key Features Summary

| Feature | Implementation | Purpose |
|---------|-----------------|---------|
| **Game State** | Model class with state constants | Track game phases (INIT, PLAY, PAUSED, ENDSTAGE, ENDGAME) |
| **Elements** | GameElement hierarchy | Represent all game objects |
| **Containers** | ContainerElement with grid | Store elements in grid (board games) |
| **Actions** | GameAction + ActionList | Orchestrate game operations |
| **Animations** | Animation + subclasses | Visual feedback for actions |
| **Events** | Event + EventQueue | Reactive notifications of state changes |
| **AI** | Decider abstract class | Pluggable AI strategies |
| **Views** | ElementLook + GameStageView | Visual representation |
| **Rendering** | RootPane + String[][] | Character-based console output |
| **Stages** | GameStageModel + GameStageView | Level/scene management |
| **Types** | ElementTypes + AnimationTypes | Extensible type system |
| **Callbacks** | @FunctionalInterface callbacks | Event-driven response |
| **Factories** | StageFactory, ActionFactory | Dynamic instantiation |
| **Logging** | Logger | Debug and tracing |

---

## No Entry Point (Framework Library)

**Boardifier is a framework, not a complete game.**

To use it, developer must:
1. Create GameStageModel subclasses (one per stage)
2. Create GameStageView subclasses (one per stage)
3. Create Controller subclass
4. Optionally create Decider subclass for AI
5. Create main() that initializes and calls controller.startGame()

Example (pseudocode):
```java
public class MyGame {
    public static void main(String[] args) throws GameException {
        Model model = new Model();
        View view = new View(model);
        MyController controller = new MyController(model, view);
        
        StageFactory.registerModelAndView("level1", "MyLevel1Model", "MyLevel1View");
        controller.setFirstStageName("level1");
        controller.startGame();
    }
}

public class MyController extends Controller {
    @Override
    public void stageLoop() {
        while (model.isStageStarted()) {
            // Game loop implementation
        }
    }
}
```

---

## Summary: Architecture Strengths

1. **Clear Separation of Concerns** - MVC makes responsibilities obvious
2. **Highly Extensible** - Type registries, factory patterns, abstract bases
3. **Event-Driven** - Reactive system without tight coupling
4. **Animation Support** - First-class animation system with state machines
5. **Action Orchestration** - Complex game sequences via ActionList/GameAction
6. **AI Ready** - Decider strategy pattern for pluggable AI
7. **Console Rendering** - Character-based, depth-sorted layering
8. **Multi-Player** - Built-in player management
9. **Type-Safe Events** - Enum-based event types
10. **Reflection-Based Loading** - Dynamic stage instantiation
11. **Callback-Heavy** - Java 8 lambda-friendly functional interfaces
12. **Board-Game Focused** - Grid containers with cell spanning

---

*End of Structural Analysis*
