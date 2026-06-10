# Boardifier Framework - Quick Developer Guide

## What is Boardifier?

Boardifier is a **Java framework for building board-based and sprite-based games**. It's a library, not a complete game—you build your game on top of it.

**Key Characteristics:**
- Clean MVC architecture (Model-View-Controller)
- Event-driven state management
- Built-in animation system
- Action/command orchestration
- Reflection-based dynamic stage loading
- Console-based character rendering
- AI-ready (pluggable decision makers)

---

## At a Glance

| Concept | Purpose | When You Use It |
|---------|---------|-----------------|
| **Model** | Game state container | Central hub for all game data |
| **GameElement** | Any object in game | Sprites, pieces, dice, text, backgrounds |
| **GameStageModel** | Level/stage data | Define what's in level 1, level 2, etc. |
| **ContainerElement** | Grid for elements | Board squares, inventories, grid layouts |
| **GameAction** | Game operation | Move piece, roll dice, change state |
| **Animation** | Visual effect | Movement, sprite face changes |
| **Event** | State change notification | Used internally for reactive updates |
| **Controller** | Game loop | Orchestrates everything each frame |
| **GameStageView** | Stage visuals | Create ElementLook for each element |
| **ElementLook** | Visual representation | How element appears on screen |
| **Decider** | AI logic | What should AI player do next? |

---

## Starting a New Game Project

### Step 1: Create Project Structure
```
MyGame/
├── src/
│   ├── MyGameController.java
│   ├── MyGame.java (main entry point)
│   └── stages/
│       ├── Level1Model.java
│       ├── Level1View.java
│       ├── Level2Model.java
│       └── Level2View.java
└── lib/
    └── boardifier.jar
```

### Step 2: Create Main Entry Point
```java
// MyGame.java
import boardifier.model.*;
import boardifier.view.*;
import boardifier.control.*;

public class MyGame {
    public static void main(String[] args) throws GameException {
        // Create core instances
        Model model = new Model();
        View view = new View(model);
        MyGameController controller = new MyGameController(model, view);
        
        // Register all stages
        StageFactory.registerModelAndView(
            "level1",
            "stages.Level1Model",
            "stages.Level1View"
        );
        
        // Start game
        controller.setFirstStageName("level1");
        controller.startGame();
    }
}
```

### Step 3: Create Controller
```java
// MyGameController.java
import boardifier.control.*;
import boardifier.model.*;
import boardifier.view.*;

public class MyGameController extends Controller {
    
    public MyGameController(Model model, View view) {
        super(model, view);
    }
    
    @Override
    public void stageLoop() {
        while (model.isStageStarted()) {
            // 1. Handle user input
            // 2. Create actions from input
            // 3. Execute actions
            // 4. Let framework process events/updates/render
            
            // Simple placeholder:
            try {
                Thread.sleep(50); // 20 FPS
            } catch (InterruptedException e) {}
        }
    }
}
```

### Step 4: Create Stage Model
```java
// stages/Level1Model.java
import boardifier.model.*;

public class Level1Model extends GameStageModel {
    
    public Level1Model(String name, Model model) {
        super(name, model);
    }
    
    @Override
    public void createElements(StageElementsFactory factory) {
        // Create game board
        ContainerElement board = new ContainerElement(
            "board",      // name
            0, 0,         // x, y position
            8, 8,         // rows, cols
            this          // stage reference
        );
        addElement(board);
        
        // Create background
        BackgroundElement bg = new BackgroundElement(0, 0, this);
        addElement(bg);
        
        // Create sprite
        SpriteElement piece = new SpriteElement(1, this);
        piece.setLocation(10, 10);
        addElement(piece);
    }
}
```

### Step 5: Create Stage View
```java
// stages/Level1View.java
import boardifier.model.*;
import boardifier.view.*;

public class Level1View extends GameStageView {
    
    public Level1View(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
        setWidth(80);
        setHeight(24);
    }
    
    @Override
    public void createLooks() {
        // Create visual representations
        for (GameElement element : gameStageModel.getElements()) {
            ElementLook look = createLookForElement(element);
            if (look != null) {
                addLook(look);
            }
        }
    }
    
    private ElementLook createLookForElement(GameElement element) {
        if (element.getType() == ElementTypes.getType("background")) {
            // Draw background
            ElementLook look = new ElementLook(element, 80, 24, -1) {
                @Override
                public void update() {
                    // Fill with space
                    for (int i = 0; i < getHeight(); i++) {
                        for (int j = 0; j < getWidth(); j++) {
                            shape[i][j] = " ";
                        }
                    }
                }
            };
            addLook(look);
            return look;
        } else if (element instanceof ContainerElement) {
            // Grid board
            ContainerElement container = (ContainerElement) element;
            GridLook look = new GridLook(3, 3, container, 0, 1);
            addLook(look);
            return look;
        } else if (element instanceof SpriteElement) {
            // Sprite piece
            ElementLook look = new ElementLook(element, 1, 1, 1) {
                @Override
                public void update() {
                    shape[0][0] = "●";
                }
            };
            addLook(look);
            return look;
        }
        return null;
    }
}
```

---

## Common Development Tasks

### Task 1: Add a Game Element
```java
// In GameStageModel.createElements():

// Create a sprite
SpriteElement piece = new SpriteElement(3, this); // 3 faces
piece.setLocation(100, 200);
piece.setVisible(true);
piece.setClickable(true);
addElement(piece);

// Create text
TextElement score = new TextElement("Score: 0", this);
score.setLocation(50, 50);
addElement(score);

// Create dice
DiceElement dice = new DiceElement(6, this);
addElement(dice);
```

### Task 2: Create and Execute an Action
```java
// In controller loop:

GameElement piece = model.getGameStageModel().getElement(0);
ContainerElement board = (ContainerElement) 
    model.getGameStageModel().getElement(1);

// Create action list
ActionList actions = ActionFactory.generatePutInContainer(
    model,
    piece,
    "board",  // container name
    3,        // target row
    4         // target column
);

// Execute
ActionPlayer player = new ActionPlayer(model, this, actions);
player.start();
```

### Task 3: Handle User Input
```java
// In controller loop:

GameStageModel stage = model.getGameStageModel();

// Check keyboard
Player currentPlayer = model.getCurrentPlayer();
if (currentPlayer.isKeyPressed("ArrowUp")) {
    // Move piece up
}

// Check mouse click
Coord2D click = model.getLastClick();
if (click.getX() >= 0) {
    // Handle click at position
}
```

### Task 4: Implement AI
```java
// MyAIDecider.java
import boardifier.control.*;
import boardifier.model.*;
import boardifier.model.action.*;

public class MyAIDecider extends Decider {
    
    @Override
    public ActionList decide() {
        GameStageModel stage = model.getGameStageModel();
        GameElement piece = stage.getElement(0);
        
        // Simple AI: move to random position
        ActionList actions = new ActionList();
        int randomRow = (int)(Math.random() * 8);
        int randomCol = (int)(Math.random() * 8);
        
        actions = ActionFactory.generatePutInContainer(
            model, piece, "board", randomRow, randomCol
        );
        
        return actions;
    }
}
```

### Task 5: Add Animation
```java
// When creating action with animation:

GameAction action = new PutInContainerAction(
    model,
    piece,
    "board",
    3, 4,
    "move/linearcst",  // animation type
    150, 200,          // destination coordinates
    100                // speed factor
);
```

### Task 6: Handle Events
```java
// In GameStageModel or GameStageView:

// Register selection callback
gameStageModel.onSelectionChange(() -> {
    System.out.println("Selection changed!");
});

// Register container callbacks
gameStageModel.onPutInContainer((element, container, row, col) -> {
    System.out.println(element + " added to " + container 
        + " at " + row + "," + col);
});
```

### Task 7: Create Custom Element Type
```java
// Register custom element type
ElementTypes.register("myType", 50);

// Use it
public class MyCustomElement extends SpriteElement {
    public MyCustomElement(GameStageModel stage) {
        super(1, stage);
        type = ElementTypes.getType("myType");
    }
}
```

### Task 8: Create Custom Animation
```java
import boardifier.model.animation.*;

public class MyAnimation extends Animation {
    private int steps;
    
    public MyAnimation(Model model, int duration) {
        super(model, duration, AnimationTypes.getType("move/linearcst"));
        steps = duration / frameGap;
    }
    
    @Override
    public void computeSteps() {
        // Generate frame data
        for (int i = 0; i < steps; i++) {
            AnimationStep step = new AnimationStep();
            step.addInt(i * 10);  // x increment
            step.addInt(i * 10);  // y increment
            this.steps.add(step);
        }
    }
}
```

---

## Debugging Tips

### Enable Logging
```java
// At game start:
Logger.setLevel(Logger.LOGGER_TRACE);     // Most verbose
Logger.setVerbosity(Logger.VERBOSE_HIGH);  // Include object refs

// Later in development:
Logger.setLevel(Logger.LOGGER_INFO);      // Less verbose
Logger.setVerbosity(Logger.VERBOSE_BASIC); // Simpler output
```

### Inspect Game State
```java
// In controller or elsewhere:
Model model = ...;
GameStageModel stage = model.getGameStageModel();

// List all elements
for (GameElement element : stage.getElements()) {
    System.out.println(element + " at " + element.getX() + "," + element.getY());
}

// Check current player
System.out.println("Current player: " + model.getCurrentPlayer().getName());

// Check game state
System.out.println("Game state: " + model.getState());
```

### Watch Animation Progress
```java
GameElement element = ...;
Animation anim = element.getAnimation();

if (anim != null) {
    System.out.println("Animation: " + anim.getName());
    System.out.println("State: " + (anim.isStarted() ? "started" : "off"));
}
```

---

## Architecture Decision: Where Does Code Go?

| Task | Location | Why |
|------|----------|-----|
| Game data/state | GameElement subclasses, GameStageModel | Model layer owns state |
| What's in a stage | GameStageModel.createElements() | Define stage content here |
| How stage looks | GameStageView.createLooks() | Visual representation |
| Game loop logic | Controller.stageLoop() | Orchestration happens here |
| AI decisions | Decider.decide() | Plugin point for AI |
| User input handling | Controller.stageLoop() | Capture and respond to input |
| Game operations | GameAction subclasses | Commands are here |
| Visual effects | Animation subclasses | Effects are temporal |
| Element appearance | ElementLook subclasses | Visual render code |

---

## Performance Considerations

### Frame Rate Control
```java
// In constructor:
Model model = new Model(10000000); // 10ms = 100 FPS

// Or default:
Model model = new Model(); // 10ms default

// Adjust in loop:
long frameStart = System.nanoTime();
// ... game logic ...
long elapsed = System.nanoTime() - frameStart;
long sleepTime = model.getFrameGap() - elapsed;
if (sleepTime > 0) {
    Thread.sleep(sleepTime / 1000000); // Convert to ms
}
```

### Viewport Optimization
```java
// In GameStageView:
// If stage size known, set explicit dimensions
setWidth(80);
setHeight(24);

// Otherwise, auto-sizing (slower):
// setWidth(-1);
// setHeight(-1);
```

---

## Common Pitfalls

### Pitfall 1: Forgetting to call createElements()
```java
// WRONG - StageFactory calls this, you don't manually call it
// gameStageModel.createElements(...);

// RIGHT - Override and let StageFactory call it
@Override
public void createElements(StageElementsFactory factory) {
    // Your code here
}
```

### Pitfall 2: Not adding elements to stage
```java
// WRONG
SpriteElement piece = new SpriteElement(1, gameStageModel);
// Piece never appears!

// RIGHT
SpriteElement piece = new SpriteElement(1, gameStageModel);
gameStageModel.addElement(piece);  // Add to stage!
```

### Pitfall 3: Not creating looks for elements
```java
// WRONG - Element exists but has no visual
gameStageModel.addElement(piece);
// In createLooks, nothing references piece

// RIGHT
gameStageModel.addElement(piece);
// ...
@Override public void createLooks() {
    ElementLook look = new ElementLook(piece, 1, 1, 0) { ... };
    addLook(look);
}
```

### Pitfall 4: Forgetting animation is optional
```java
// Animation is NOT automatic
// You must create it in GameAction.createAnimation()
@Override
protected void createAnimation() {
    // If you want animation:
    animation = new LinearMoveAnimation(...);
    // If no animation needed:
    animation = null;
}
```

### Pitfall 5: Using wrong factory class name format
```java
// WRONG - won't find class
StageFactory.registerModelAndView("level1", 
    "Level1Model",  // Missing package!
    "Level1View");

// RIGHT - fully qualified
StageFactory.registerModelAndView("level1",
    "com.game.stages.Level1Model",
    "com.game.stages.Level1View");
```

---

## Testing Your Game

### Test Checklist
- [ ] All elements appear in console
- [ ] Elements can be selected/clicked
- [ ] Keyboard input works
- [ ] Actions execute correctly
- [ ] Animations play smoothly
- [ ] Frame rate is acceptable
- [ ] Game transitions to next stage
- [ ] Game ends properly
- [ ] No crashes or exceptions

### Simple Test Harness
```java
public class GameTest {
    public static void main(String[] args) throws GameException {
        Model model = new Model();
        View view = new View(model);
        TestController controller = new TestController(model, view);
        
        StageFactory.registerModelAndView(
            "test",
            "stages.TestModel",
            "stages.TestView"
        );
        
        controller.setFirstStageName("test");
        controller.startGame();
        
        // Run for 5 seconds then exit
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        System.exit(0);
    }
}
```

---

## Next Steps

1. **Read**: STRUCTURAL_ANALYSIS.md for detailed architecture
2. **Browse**: CLASS_HIERARCHY_AND_REFERENCE.md for API details
3. **Start**: Create your first Level1Model and Level1View
4. **Iterate**: Build out your game stage by stage
5. **Debug**: Use Logger and state inspection to troubleshoot
6. **Optimize**: Profile frame rate, memory usage as needed

---

*This framework is designed for extensibility. The framework handles MVC coordination, animation, events, and rendering. You focus on game logic, content, and gameplay mechanics.*
