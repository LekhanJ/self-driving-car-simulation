# 🚗 Self-Driving Car Simulation

> A fully autonomous neural network-powered vehicle simulation built from scratch in Java — no ML libraries, no shortcuts.

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-5C5C5C?style=for-the-badge)
![Gson](https://img.shields.io/badge/Gson-2.13.2-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge)

---

## 📌 Overview

This project implements a **fully self-driving car simulation** using a custom-built feedforward neural network and a genetic-mutation learning algorithm — all written in pure Java with zero machine learning frameworks. The car learns to navigate a multi-lane road, detect obstacles, and make real-time steering decisions entirely on its own.

The simulation includes:
- A custom **3-layer neural network** (5 → 12 → 4) implemented from scratch
- **5-ray proximity sensors** that feed distance data into the network
- **Mutation-based training** to evolve smarter driving behavior over generations
- A **live neural network visualizer** that displays real-time neuron activations
- **Brain persistence** — save the best-performing network to JSON and reload it across sessions

---

## 🎬 Demo

```
┌──────────────────────────────┬──────────────────────────────────────┐
│        Road / Simulation     │    Neural Network Visualizer         │
│                              │                                      │
│   [🚙] AI Car (full alpha)   │   ○──○──○──○──○  (sensor inputs)     │
│   [🚙] Clones (35% alpha)    │        ↕  weights                    │
│   [🚛] Traffic (dummy cars)  │   ○──○──○──○──○──○──○──○──○──○──○──○ │
│                              │        ↕  weights                    │
│   ══╔══════════╗══           │   [↑] [←] [→] [↓] (outputs)          │
│      ║  ROAD   ║             │                                      │
│   ══╚══════════╝══           │   Live glow = active connections     │
└──────────────────────────────┴──────────────────────────────────────┘
```

---

## ✨ Key Features

| Feature | Details |
|---|---|
| **Neural Network from Scratch** | No TensorFlow, PyTorch, or ML libraries — pure Java math |
| **Real-Time Sensor System** | 5 ray-casted sensors, 250px range, 60° spread |
| **Live NN Visualizer** | Animated neuron activations with glow effects and color-coded weights |
| **Mutation-Based Learning** | Evolve smarter generations using Gaussian-style weight perturbation |
| **Brain Persistence** | Serialize/deserialize best network weights to `best_brain.json` via Gson |
| **Polygon Collision Detection** | Precise SAT-based intersection testing for road borders and traffic |
| **Multi-Car Simulation** | Run N parallel AI cars; highlight the best-performing one |
| **Manual + AI Control** | Toggle between keyboard-controlled and AI-driven modes |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          ENTRY POINT                                │
│                          Main.java                                  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ creates
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                        MainScreen.java                             │
│           (JFrame — Layout, Buttons, Panel Management)             │
│                                                                    │
│   ┌─────────────────────────┐   ┌──────────────────────────────┐   │
│   │       Road.java         │   │  NeuralNetworkVisualization  │   │
│   │  (JPanel — Game Loop)   │   │       .java (JPanel)         │   │
│   │                         │   │                              │   │
│   │  - Timer @ ~60 FPS      │   │  - Renders live network      │   │
│   │  - Traffic management   │   │  - Updated each frame with   │   │
│   │  - Camera follow        │   │    the best car's brain      │   │
│   │  - Best car tracking    │   └──────────┬───────────────────┘   │
│   └───────────┬─────────────┘              │ reads                 │
│               │ manages                    │                       │
└───────────────┼────────────────────────────┼───────────────────────┘
                │                            │
                ▼                            ▼
┌──────────────────────────┐     ┌───────────────────────────────────┐
│        Car.java          │     │          Visualizer.java          │
│                          │     │                                   │
│  - Position / Physics    │     │  - drawNetwork() renders layers   │
│  - Polygon hitbox        │     │  - Color-coded weights (blue/org) │
│  - Damage detection      │     │  - Smoothed activation glow       │
│  - AI ↔ Manual toggle    │     │  - Arrow labels on output nodes   │
│                          │     └───────────────────────────────────┘
│   ┌──────────────────┐   │
│   │   Sensor.java    │   │
│   │                  │   │
│   │  5 ray-casted    │   │
│   │  proximity rays  │   │
│   │  → float[]       │   │
│   │  distance values │   │
│   └────────┬─────────┘   │
│            │ feeds       │
│   ┌────────▼─────────┐   │
│   │ NeuralNetwork    │   │
│   │   .java          │   │
│   │                  │   │
│   │  [5]→[12]→[4]    │   │
│   │  feedForward()   │   │
│   │  mutate()        │   │
│   └────────┬─────────┘   │
│            │ outputs     │
│   ┌────────▼─────────┐   │
│   │  Controls.java   │   │
│   │                  │   │
│   │  ↑  ←  →  ↓      │   │
│   │  KeyAdapter or   │   │
│   │  AI-driven       │   │
│   └──────────────────┘   │
└──────────────────────────┘
         │
         │ serialized via Gson
         ▼
┌────────────────────────────┐
│    BrainStorage.java       │
│                            │
│  save() → best_brain.json  │
│  load() ← best_brain.json  │
│  discard() → delete file   │
└────────────────────────────┘
```

---

## 🧠 How the Neural Network Works

The brain is a **custom feedforward neural network** with binary threshold activation:

```
Sensor Inputs (5 rays)
        │
        ▼
┌───────────────┐
│  Input Layer  │  5 neurons — normalized ray distances (0.0 → 1.0)
│               │  0.0 = obstacle very close, 1.0 = clear path
└──────┬────────┘
       │  weighted connections (random init, ±1.0)
       ▼
┌───────────────┐
│ Hidden Layer  │  12 neurons — learns abstract obstacle patterns
│               │  Activation: output = 1 if (Σ w·x) > bias, else 0
└──────┬────────┘
       │  weighted connections
       ▼
┌───────────────┐
│ Output Layer  │  4 neurons → [Forward, Left, Right, Reverse]
│               │  Threshold: fire if output > 0.5
└───────────────┘
```

### Training Loop (Mutation-Based Evolution)

Since there is no labeled training data or gradient descent, the car learns via **genetic-style mutation**:

1. **Initialize** — spawn N cars with randomly weighted brains
2. **Simulate** — all cars drive simultaneously on the same road with identical traffic
3. **Evaluate** — the car that travels furthest (lowest Y coordinate) is the "best"
4. **Save** — the best brain's weights are serialized to `best_brain.json`
5. **Next Generation** — reload the saved brain; apply small random mutations (`amount = 0.1`) to all clones using linear interpolation:

```java
weight = lerp(currentWeight, randomValue(-1, 1), mutationAmount);
```

6. Repeat until the car successfully navigates the full track

---

## 📡 Sensor System

```
          Car
           │
     ┌─────┼─────┐
  ray│  ray│  ray│ray   ray
  ╲  │  ╲  │  ╱  │  ╱    │
   ╲ │   ╲ │ ╱   │ ╱     ↓ 250px max range
    ╲│    ╲│╱    │╱    60° total spread
```

Each of the **5 sensors** casts a ray outward. If a ray intersects a road border or traffic car polygon, the normalized distance to the nearest intersection point is calculated:

```
offset = distance_to_hit / max_ray_length   (0.0 = touching, 1.0 = clear)
input  = 1.0 - offset                       (inverted: 1.0 = danger ahead)
```

These 5 values are passed directly as inputs to the neural network on every frame.

---

## 💥 Collision Detection

Collision uses **polygon intersection testing** (Separating Axis Theorem-inspired):

1. Each car's hitbox is computed as a **4-point convex polygon** based on its bounding diagonal and current rotation angle
2. Every edge pair between two polygons is checked using parametric line-segment intersection
3. If any intersection is found, the car is marked `damaged = true` and stops responding to controls

```java
// Core intersection math (Utils.java)
t = tTop / bottom;   // parametric position on segment AB
u = uTop / bottom;   // parametric position on segment CD
if (t ∈ [0,1] && u ∈ [0,1]) → intersection found
```

---

## 🗂️ Project Structure

```
self-driving-car-simulation/
│
├── src/
│   ├── Main.java                    # Entry point
│   ├── MainScreen.java              # JFrame, layout, save/delete buttons
│   ├── Road.java                    # Game loop (Timer), traffic, rendering
│   ├── Car.java                     # Physics, AI brain, damage, drawing
│   ├── Sensor.java                  # 5-ray proximity sensor system
│   ├── NeuralNetwork.java           # Custom NN: feedForward, mutate
│   ├── Level.java                   # Single NN layer: weights, biases, activation
│   ├── Controls.java                # KeyAdapter + AI control interface
│   ├── Visualizer.java              # Live neural network renderer
│   ├── NeuralNetworkVisualization.java  # JPanel wrapper for Visualizer
│   ├── BrainStorage.java            # Gson-based save/load of NN weights
│   └── Utils.java                   # lerp(), getIntersection(), polysIntersect()
│
├── images/
│   ├── car.png                      # AI car sprite
│   └── enemy-car.png                # Traffic car sprite
│
├── best_brain.json                  # Persisted best neural network weights
├── gson-2.13.2.jar                  # Gson dependency (JSON serialization)
├── SelfDrivingCar.iml               # IntelliJ module file
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- IntelliJ IDEA (recommended) or any Java IDE

### Running the Project

**Option 1 — IntelliJ IDEA (Recommended)**

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/self-driving-car-simulation.git
   cd self-driving-car-simulation
   ```

2. Open the project in IntelliJ IDEA

3. Add `gson-2.13.2.jar` to the project classpath:
   - Go to **File → Project Structure → Libraries**
   - Click **+** → **Java** → select `gson-2.13.2.jar`

4. Run `Main.java`

**Option 2 — Command Line**

```bash
# Compile
javac -cp .:gson-2.13.2.jar src/*.java -d out/

# Run
java -cp out:gson-2.13.2.jar Main
```

> **Windows users:** replace `:` with `;` in the classpath

---

## 🎮 Controls

| Input | Action |
|---|---|
| `↑` Arrow | Accelerate forward |
| `↓` Arrow | Reverse |
| `←` Arrow | Steer left |
| `→` Arrow | Steer right |
| `💾` Save button | Save the current best brain to `best_brain.json` |
| `🗑️` Delete button | Delete the saved brain and restart from random weights |

> In AI mode, the arrow keys are overridden by the neural network's output decisions.

---

## 🔄 Training Workflow

```
1. Launch the simulation
   └─ Cars spawn with random neural network weights

2. Watch the simulation run
   └─ The "best" car (furthest ahead) is highlighted at full opacity
   └─ Clones render at 35% transparency

3. When a promising run appears:
   └─ Click 💾 to save the best car's brain

4. Click 🗑️ then immediately re-run (or restart)
   └─ The saved brain is loaded + lightly mutated for all clones

5. Repeat steps 2–4
   └─ Each generation slightly improves on the last
   └─ Within a few generations, the car navigates the full track
```

---

## 🔍 Technical Highlights

**For resume/interview discussions:**

- **Zero ML frameworks** — the entire neural network (initialization, forward pass, mutation, serialization) is built from first principles in ~100 lines of Java
- **Real-time rendering at ~60 FPS** — Java Swing `Timer` drives a custom game loop with `Graphics2D` transforms for smooth camera follow
- **Temporal activation smoothing** — the visualizer uses a low-pass filter (`SMOOTHING = 0.2`) on connection activations to produce fluid, non-flickering glow animations
- **Efficient polygon math** — rotating hitbox polygons are recomputed each frame using `Math.hypot` + `Math.atan2`; intersection testing is O(n·m) on polygon edges
- **Generational learning without backprop** — inspired by NEAT / evolutionary strategies; mutation via `lerp(w, rand(-1,1), amount)` is equivalent to additive Gaussian noise at small `amount` values

---

## 🛠️ Dependencies

| Library | Version | Purpose |
|---|---|---|
| [Google Gson](https://github.com/google/gson) | 2.13.2 | JSON serialization of neural network weights |
| Java Swing | Built-in | GUI, rendering, event handling |

---

## 🧩 Concepts Demonstrated

- **Machine Learning** — custom feedforward neural network, weight initialization, threshold activation
- **Evolutionary Algorithms** — mutation-based training without labeled data or gradients
- **Computer Graphics** — affine transforms, alpha compositing, antialiasing, custom rendering pipeline
- **Physics Simulation** — velocity, acceleration, friction, angle-based steering
- **Geometric Algorithms** — ray casting, parametric line intersection, convex polygon collision
- **Design Patterns** — Observer (game loop → visualizer), Strategy (AI vs manual controls), Serialization

---

## 📈 Potential Extensions

- [ ] Increase population size for faster evolution (currently N=1, easily scalable)
- [ ] Add a fitness scoring system beyond just Y-position (e.g., penalize near-misses)
- [ ] Implement crossover between two top-performing brains (true genetic algorithm)
- [ ] Add curved roads and intersections
- [ ] Export training history to CSV and plot learning curves
- [ ] Port the neural network to use a canvas-based web frontend (JavaScript)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<p align="center">
  Built with ☕ Java and curiosity about how intelligence emerges from math.
</p>
