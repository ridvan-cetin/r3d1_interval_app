# R3D1 Interval Timer - Kivy Version

A cross-platform productivity timer app built with Python and Kivy.

## Features

### Interval Timer Profiles
- Pre-configured profiles (Meeting, Coding, Chit-chat, Quick Check)
- Custom profiles with configurable intervals, sessions, and breaks
- Color-coded profiles
- Break reminders after set intervals

### Pomodoro Timer
- Classic Pomodoro Technique (25/5/15 min)
- Customizable focus and break durations
- Daily tomato counter
- Task tracking
- Auto-start options for breaks and focus sessions
- Long break after 4 pomodoros (configurable)

### Quick Start
- Instant timer with custom minutes and intervals
- No profile setup required

### Statistics
- Weekly activity tracking
- Total minutes, sessions, and active days
- Session history

### Settings
- Sound alerts
- Vibration feedback
- Dark theme

## Installation

### Desktop (Windows/macOS/Linux)

1. Install Python 3.8+
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Run the app:
   ```bash
   python main.py
   ```

### Android

1. Install Buildozer:
   ```bash
   pip install buildozer
   ```
2. Initialize (first time only):
   ```bash
   buildozer init
   ```
3. Build APK:
   ```bash
   buildozer android debug
   ```
4. Install on device:
   ```bash
   buildozer android deploy run
   ```

### iOS (macOS only)

1. Install kivy-ios:
   ```bash
   pip install kivy-ios
   ```
2. Build:
   ```bash
   toolchain build kivy
   toolchain create R3D1Interval .
   ```

## Project Structure

```
kivy_app/
├── main.py              # Main application code
├── r3d1interval.kv      # Kivy UI layout file
├── r3d1_data.json       # Local data storage (auto-created)
├── requirements.txt     # Python dependencies
├── buildozer.spec       # Android build configuration
└── README.md            # This file
```

## Usage

### Profiles Tab
1. Tap a profile card to start the timer
2. Use the + button to create new profiles
3. Tap and hold a profile to edit or delete

### Pomodoro Tab
1. Enter your current task (optional)
2. Tap "Start Focus" to begin a 25-minute session
3. After completing a pomodoro, take a 5-minute break
4. After 4 pomodoros, take a 15-minute long break

### Quick Start
1. Set the interval duration (minutes)
2. Set the number of intervals
3. Tap GO to start

## Data Storage

All data is stored locally in `r3d1_data.json`:
- Profiles
- Session history
- Pomodoro history
- Settings

## License

MIT License - Feel free to use and modify!
