# NoVoidX

NoVoidX is a lightweight Minecraft plugin for Paper that prevents players from falling into the void by teleporting them automatically. The plugin is simple, efficient, and fully configurable.

## 🧩 Features

- Automatically teleports players when falling into the void.
- Customizable teleportation location.
- Multi-world support.

## 🛠 Installation

1. Download the `NoVoidX.jar` plugin file.
2. Place it in your server's `plugins/` folder.
3. Restart your server.

## ⚙️ Configuration

After the plugin is run for the first time, a configuration file will be generated at `plugins/NoVoidX/config.yml`. You can customize it as follows:

```yaml
# NoVoidX Configuration
enabled: true

# Check update on startup
check-update: true

worlds:
  world:
    enabled: true # Set to false to disable NoVoidX for this world
    void-threshold: -128 # Y-coordinate threshold for void in the Overworld
    target-world: world_nether
    target-y: 512
    ration: 8 # Ratio of teleportation to Nether world
  world_the_end:
    enabled: true
    void-threshold: -64
    target-world: world
    target-y: 512
    ration: 1 # Ratio of teleportation to Overworld

# Duration of blindness effect in seconds after teleportation
blindness-duration: 5

# Cooldown in seconds before a player can be teleported again
teleport-cooldown: 3

# logging settings
log: true # Enable or disable logging
```

📄 License
This project is licensed under the Apache License 2.0.