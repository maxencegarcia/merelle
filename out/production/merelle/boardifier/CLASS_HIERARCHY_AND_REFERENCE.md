# Boardifier - Class Hierarchy and Reference

## Class Hierarchy Trees

### Model Element Hierarchy
```
GameElement (abstract)
├── StaticElement (abstract)
│   ├── BackgroundElement (concrete)
│   ├── ContainerElement (concrete)
│   └── [Custom StaticElement subclasses]
├── SpriteElement (concrete)
│   ├── DiceElement (concrete)
│   └── [Custom SpriteElement subclasses]
├── TextElement (concrete)
└── [Custom GameElement subclasses]
```

### Animation Hierarchy
```
Animation (abstract)
├── LinearMoveAnimation (concrete)
├── MoveAnimation (concrete)
├── WaitAnimation (concrete)
├── FaceAnimation (concrete)
├── CyclicFaceAnimation (concrete)
└── [Custom Animation subclasses]
```

### Action Hierarchy
```
GameAction (abstract)
├── PutInContainerAction (concrete)
├── RemoveFromContainerAction (concrete)
├── MoveWithinContainerAction (concrete)
├── RemoveFromStageAction (concrete)
├── DrawDiceAction (concrete)
└── [Custom GameAction subclasses]
```

### View Element Hierarchy
```
ElementLook (abstract)
├── ContainerLook (concrete)
│   ├── GridLook (concrete)
│   ├── TableLook (concrete)
│   └── [Custom ContainerLook subclasses]
├── TextLook (concrete)
├── ClassicBoardLook (concrete)
└── [Custom ElementLook subclasses]
```

### Controller Hierarchy
```
Controller (abstract)
└── [Game-specific Controller subclass]
```

### Decider Hierarchy
```
Decider (abstract)
└── [Game-specific AI Decider subclasses]
```

### Stage Hierarchy
```
GameStageModel (abstract)
└── [Game-specific stage classes: Level1Model, Level2Model, etc.]

GameStageView (abstract)
└── [Game-specific view classes: Level1View, Level2View, etc.]
```

---

## Complete Class Directory

### MODEL PACKAGE (boardifier.model)
**Core Classes**
- `Model.java` - Global game state manager
- `GameElement.java` - Abstract base for all game objects
- `GameStageModel.java` - Abstract stage/level manager
- `Player.java` - Player representation

**Element Types**
- `StaticElement.java` - Base for non-moving elements
- `BackgroundElement.java` - Background graphics
- `SpriteElement.java` - Animated, moving entities
- `TextElement.java` - Dynamic text
- `DiceElement.java` - Dice/randomizer element
- `ContainerElement.java` - Grid-based element container

**Type System**
- `ElementTypes.java` - Extensible element type registry

**Events**
- `Event.java` - Immutable event envelope
- `EventQueue.java` - Per-element event queue
- `Coord2D.java` - 2D coordinate utility

**Exceptions**
- `GameException.java` - Game-specific exception

**Callbacks**
- `SelectionCallback.java` - Selection change callback (functional interface)
- `ContainerOpCallback.java` - Container operation callback (functional interface)

### MODEL.ACTION SUBPACKAGE (boardifier.model.action)
**Action Framework**
- `GameAction.java` - Abstract action base class
- `ActionList.java` - Action orchestration container
- `ActionCallback.java` - Action completion callback (functional interface)

**Concrete Actions**
- `PutInContainerAction.java` - Add element to container
- `RemoveFromContainerAction.java` - Remove from container
- `MoveWithinContainerAction.java` - Move within container
- `RemoveFromStageAction.java` - Remove from stage
- `DrawDiceAction.java` - Roll dice

### MODEL.ANIMATION SUBPACKAGE (boardifier.model.animation)
**Animation Framework**
- `Animation.java` - Abstract animation base class
- `AnimationState.java` - Animation lifecycle state
- `AnimationStep.java` - Single frame data
- `AnimationCallback.java` - Animation completion callback (functional interface)
- `AnimationTypes.java` - Extensible animation type registry

**Concrete Animations**
- `LinearMoveAnimation.java` - Linear movement
- `MoveAnimation.java` - Instant teleport movement
- `WaitAnimation.java` - Frame-based wait
- `FaceAnimation.java` - Single sprite face display
- `CyclicFaceAnimation.java` - Cycling sprite faces

### CONTROL PACKAGE (boardifier.control)
**Core Classes**
- `Controller.java` - Abstract game controller
- `ActionPlayer.java` - Action execution engine
- `Decider.java` - Abstract AI/decision maker

**Factories**
- `StageFactory.java` - Dynamic stage instantiation via reflection
- `ActionFactory.java` - Common action creation helpers

**Utilities**
- `Logger.java` - Configurable logging system

### VIEW PACKAGE (boardifier.view)
**Main View Classes**
- `View.java` - Top-level view manager
- `GameStageView.java` - Abstract stage-specific view
- `RootPane.java` - Console rendering engine

**Element Look Classes**
- `ElementLook.java` - Abstract visual representation base
- `ContainerLook.java` - Grid-based layout container
- `GridLook.java` - Grid with borders
- `TableLook.java` - Table-style layout
- `TextLook.java` - Text rendering
- `ClassicBoardLook.java` - Board-game rendering

**Utilities**
- `ConsoleColor.java` - Color constants

---

## Type Registries

### ElementTypes Registry
```
String → int mapping:
"basic" → 0
"static" → 1
"background" → 3
"container" → 10
"text" → 20
"sprite" → 30
"dice" → 31
```

**Usage Pattern:**
```java
ElementTypes.register("myCustomType", 100);
int type = ElementTypes.getType("myCustomType");
```

### AnimationTypes Registry
```
String → int mapping:
"none" → -1
"move/teleport" → 10
"move/linearcst" → 11
"move/linearprop" → 12
"look/simple" → 20
"look/sequence" → 21
"look/random" → 22
"wait/frames" → 30
```

**Naming Convention:** "category/action" (e.g., "move/linear", "look/sequence")

**Usage Pattern:**
```java
AnimationTypes.register("move/custom", 13);
int type = AnimationTypes.getType("move/custom");
```

---

## Key Interfaces and Functional Interfaces

### Functional Interfaces (Lambda-Compatible)
```java
// Action completion
ActionCallback {
    void execute()
}

// Animation completion
AnimationCallback {
    void execute()
}

// Selection events
SelectionCallback {
    void onSelectionChange()
}

// Container operations
ContainerOpCallback {
    void onContainerOp(GameElement element, ContainerElement container, int row, int col)
}
```

---

## Core Constants

### Model Game States (Model.java)
```java
STATE_INIT = 1      // Initialization/intro screen
STATE_PLAY = 2      // Active gameplay
STATE_PAUSED = 3    // Game paused
STATE_ENDSTAGE = 4  // Current stage completed
STATE_ENDGAME = 5   // Game ended
```

### SpriteElement States (SpriteElement.java)
```java
SPRITE_STATE_IDLE = 0
SPRITE_STATE_MOVING = 1
SPRITE_STATE_FALLING = 2
SPRITE_STATE_COLLIDE = 3
SPRITE_STATE_JUMPING = 4
```

### Direction Constants (SpriteElement.java)
```java
MOVE_NONE = -1
MOVE_RIGHT = 0
MOVE_UP = 1
MOVE_LEFT = 2
MOVE_DOWN = 3
```

### Player Types (Player.java)
```java
HUMAN = 1
COMPUTER = 2
```

### ElementLook Anchor Types (ElementLook.java)
```java
ANCHOR_CENTER = 0      // Center of look at (x,y)
ANCHOR_TOPLEFT = 1     // Top-left corner at (x,y)
```

### ContainerLook Alignment (ContainerLook.java)
```java
// Vertical
ALIGN_TOP = 0
ALIGN_MIDDLE = 1
ALIGN_BOTTOM = 2

// Horizontal
ALIGN_LEFT = 0
ALIGN_CENTER = 1
ALIGN_RIGHT = 2
```

### Logger Levels (Logger.java)
```java
LOGGER_NONE = 0     // Disabled
LOGGER_INFO = 1     // Information messages
LOGGER_DEBUG = 2    // Debug messages
LOGGER_TRACE = 3    // Framework trace
```

### Logger Verbosity (Logger.java)
```java
VERBOSE_NONE = 0    // Just message
VERBOSE_BASIC = 1   // Class + method names
VERBOSE_HIGH = 2    // Class + method + object reference
```

### Event Types (Event.java)
```java
EventType.LOCATION          // Position changed
EventType.VISIBILITY        // Visibility changed
EventType.SELECTION         // Selection changed
EventType.IN_CONTAINER      // Entered container
EventType.OUT_CONTAINER     // Left container
EventType.MOVE_CONTAINER    // Moved within container
EventType.FACE              // Sprite face changed
```

### Animation States (AnimationState.java)
```java
OFF                 // Not running
STARTED             // Running
PAUSED              // Paused
```

---

## Key Attributes Reference

### Model
- `state: int` - Current game state
- `frameGap: long` - Nanoseconds between frame updates
- `lastFrame: long` - Last frame timestamp
- `players: List<Player>` - All players
- `idPlayer: int` - Current player index
- `idWinner: int` - Winner ID (-1 if none)
- `gameStageModel: GameStageModel` - Active stage
- `lastClick: Coord2D` - Last mouse click position
- `captureMouseEvent: boolean` - Accept mouse input?
- `captureKeyEvent: boolean` - Accept keyboard input?
- `captureActionEvent: boolean` - Accept action events?

### GameElement
- `x, y: double` - Position
- `gameStageModel: GameStageModel` - Owner stage
- `visible: boolean` - Display flag
- `selected: boolean` - Selection state
- `clickable: boolean` - Interactable flag
- `type: int` - Element type
- `container: ContainerElement` - Parent container
- `animation: Animation` - Current animation
- `eventQueue: EventQueue` - Event queue
- `inContainerOp: boolean` - Operation in progress?

### ContainerElement
- `name: String` - Container identifier
- `nbRows, nbCols: int` - Grid size
- `grid: List<GameElement>[][]` - Cell contents
- `rowSpans, colSpans: int[][]` - Spanning cells
- `reachableCells: boolean[][]` - Accessible cells

### SpriteElement
- `nbFaces: int` - Face count
- `faceIndexes: List<Integer>` - Face sequence
- `currentIndex: int` - Current face index
- `state: int` - Sprite state
- `xSpeed, ySpeed: double` - Velocity
- `framesPerFaceChange: int` - Animation speed

### Animation
- `duration: int` - Milliseconds
- `frameGap: int` - Frame interval
- `state: AnimationState` - Animation state
- `animationStep: int` - Current step
- `steps: List<AnimationStep>` - Frame data
- `type: int` - Animation type

### ElementLook
- `element: GameElement` - Represented element
- `shape: String[][]` - Visual buffer
- `width, height: int` - Dimensions
- `depth: int` - Layer (negative = below)
- `anchorType: int` - Anchor position
- `parent: ElementLook` - Container look

### RootPane
- `viewPort: String[][]` - Render buffer
- `width, height: int` - Buffer dimensions
- `gameStageView: GameStageView` - Current view

---

## Method Signatures Quick Reference

### Model Essential Methods
```java
Model(long frameGap)
void startGame(GameStageModel stage)
void update()
void reset()
void setCaptureMouseEvent(boolean)
void setCaptureKeyEvent(boolean)
void setCaptureActionEvent(boolean)
GameStageModel getGameStageModel()
Player getCurrentPlayer()
List<Player> getPlayers()
ContainerElement getContainer(String name)
```

### GameElement Essential Methods
```java
void update()                                    // Abstract - override
void setLocation(double x, double y)
void setVisible(boolean)
void setSelected(boolean)
void setClickable(boolean)
void setAnimation(Animation)
GameStageModel getGameStageModel()
ContainerElement getContainer()
Animation getAnimation()
EventQueue getEventQueue()
```

### ContainerElement Essential Methods
```java
ContainerElement(String name, int x, int y, int nbRows, int nbCols, GameStageModel)
void addElement(GameElement, int row, int col)
void removeElement(GameElement)
GameElement getCellContent(int row, int col)
void moveWithin(GameElement, int rowDest, int colDest)
void setReachable(int row, int col, boolean)
boolean isReachable(int row, int col)
boolean setCellSpan(int row, int col, int rowSpan, int colSpan)
```

### GameAction Essential Methods
```java
GameAction(Model model, GameElement element, String animationName)
void execute()                                  // Abstract - override
void createAnimation()                          // Abstract - override
Animation setupAnimation()
void setAnimateBeforeExecute(boolean)
void onActionEnd(ActionCallback callback)
void onAnimationEnd(AnimationCallback callback)
```

### Animation Essential Methods
```java
Animation(Model model, int duration, int type)
void computeSteps()                            // Abstract - override
void start()
void pause()
void stop()
AnimationStep next()
void onEnd(AnimationCallback callback)
boolean isStarted()
boolean isPaused()
```

### Controller Essential Methods
```java
Controller(Model model, View view)
void startGame()                                // Entry point
void stageLoop()                                // Abstract - override
void setFirstStageName(String stageName)
protected void startStage(String stageName)
protected void processEvents()
```

### ActionPlayer Essential Methods
```java
ActionPlayer(Model model, Controller control, Decider decider, ActionList preActions)
ActionPlayer(Model model, Controller control, ActionList actions)
void start()
```

### Decider Essential Methods
```java
Decider(Model model, Controller control)
ActionList decide()                            // Abstract - override
```

### GameStageView Essential Methods
```java
GameStageView(String name, GameStageModel gameStageModel)
void createLooks()                             // Abstract - override
void addLook(ElementLook look)
ElementLook getElementLook(GameElement element)
List<ElementLook> getLooks()
void setWidth(int), setHeight(int)
```

### ElementLook Essential Methods
```java
ElementLook(GameElement element, int width, int height, int depth)
void update()                                   // Abstract - override
GameElement getElement()
int getWidth(), getHeight()
int getDepth()
void setAnchor(int anchorType)
void setRootPane(RootPane rootPane)
```

### RootPane Essential Methods
```java
RootPane(int width, int height)
void init(GameStageView gameStageView)
void update()
void print()
void clearViewPort()
```

### StageFactory Essential Methods
```java
static void registerModelAndView(String stageName, String modelClassName, String viewClassName)
static GameStageModel createStageModel(String stageName, Model model)
static GameStageView createStageView(String stageName, GameStageModel model)
```

### ActionFactory Essential Methods
```java
static ActionList generatePutInContainer(Model, GameElement, String nameContainerDest, int rowDest, int colDest)
static ActionList generateMoveWithinContainer(Model, GameElement, int rowDest, int colDest)
static ActionList generateRemoveFromContainer(Model, GameElement)
static ActionList generateRemoveFromStage(Model, GameElement)
static ActionList generateDrawDice(Model, GameElement)
```

---

## Typical Extension Points for Game Development

### Required Extensions
1. **Controller Subclass**
   ```java
   public class MyGameController extends Controller {
       @Override
       public void stageLoop() {
           // Game loop implementation
       }
   }
   ```

2. **GameStageModel Subclasses** (one per level)
   ```java
   public class Level1Model extends GameStageModel {
       public Level1Model(String name, Model model) {
           super(name, model);
       }
       
       @Override
       public void createElements(StageElementsFactory factory) {
           // Create and add game elements
       }
   }
   ```

3. **GameStageView Subclasses** (one per level)
   ```java
   public class Level1View extends GameStageView {
       public Level1View(String name, GameStageModel gameStageModel) {
           super(name, gameStageModel);
       }
       
       @Override
       public void createLooks() {
           // Create ElementLook for each element
       }
   }
   ```

### Optional Extensions
4. **Decider Subclass** (for AI)
   ```java
   public class MyAI extends Decider {
       @Override
       public ActionList decide() {
           // AI logic returns actions
       }
   }
   ```

5. **Custom GameElement Subclasses**
   ```java
   public class MyPiece extends SpriteElement {
       @Override
       public void update() {
           // Custom behavior
       }
   }
   ```

6. **Custom Action Subclasses**
   ```java
   public class MyAction extends GameAction {
       @Override
       public void execute() {
           // Custom action logic
       }
       
       @Override
       protected void createAnimation() {
           // Create associated animation
       }
   }
   ```

7. **Custom Animation Subclasses**
   ```java
   public class MyAnimation extends Animation {
       @Override
       public void computeSteps() {
           // Generate AnimationStep data
       }
   }
   ```

8. **Custom ElementLook Subclasses**
   ```java
   public class MyElementLook extends ElementLook {
       @Override
       public void update() {
           // Custom rendering logic
       }
   }
   ```

---

## Framework Loading Sequence (Detailed)

### 1. Framework Initialization
```java
// Create model
Model model = new Model();

// Create view with model reference
View view = new View(model);

// Create controller subclass with both
MyController controller = new MyController(model, view);
```

### 2. Stage Registration
```java
// Register all stages before starting game
StageFactory.registerModelAndView(
    "level1",
    "com.game.stages.Level1Model",
    "com.game.stages.Level1View"
);
StageFactory.registerModelAndView(
    "level2",
    "com.game.stages.Level2Model",
    "com.game.stages.Level2View"
);
```

### 3. Game Start
```java
controller.setFirstStageName("level1");
controller.startGame();
```

### 4. Stage Loading (Internal)
```
StageFactory.createStageModel("level1", model)
├── Class.forName("com.game.stages.Level1Model")
├── getDeclaredConstructor(String.class, Model.class)
└── newInstance("level1", model)

gameStageModel.createElements(factory)
└── Developer creates GameElement instances

StageFactory.createStageView("level1", gameStageModel)
├── Class.forName("com.game.stages.Level1View")
├── getDeclaredConstructor(String.class, GameStageModel.class)
└── newInstance("level1", gameStageModel)

gameStageView.createLooks()
└── Developer creates ElementLook instances
```

### 5. Game Loop (Internal)
```
controller.stageLoop()
├── Each iteration:
│   ├── Capture input
│   ├── Create actions (player input or Decider.decide())
│   ├── ActionPlayer.start()
│   │   ├── Execute preActions
│   │   ├── Execute main actions
│   │   └── Handle animations
│   ├── Controller.processEvents()
│   │   ├── Handle container events
│   │   ├── Update elements
│   │   └── Update looks
│   ├── Model.update()
│   │   └── Frame-based updates
│   └── View.update()
│       └── RootPane renders to console
└── Repeat until stage ends
```

---

*End of Class Hierarchy and Reference*
