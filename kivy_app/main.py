"""
R3D1 Interval Timer - Kivy Version
A productivity timer app with profiles and Pomodoro support
"""

import json
import os
from datetime import datetime, timedelta
from functools import partial

from kivy.app import App
from kivy.clock import Clock
from kivy.core.window import Window
from kivy.lang import Builder
from kivy.properties import (
    StringProperty, NumericProperty, BooleanProperty,
    ListProperty, DictProperty, ObjectProperty
)
from kivy.uix.screenmanager import ScreenManager, Screen, SlideTransition
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.popup import Popup
from kivy.uix.textinput import TextInput
from kivy.utils import get_color_from_hex
from kivy.metrics import dp

# Set window size for desktop testing
Window.size = (400, 700)

# Load KV file
kv_path = os.path.join(os.path.dirname(__file__), 'r3d1interval.kv')
if os.path.exists(kv_path):
    Builder.load_file(kv_path)

# Default profiles
DEFAULT_PROFILES = [
    {
        'id': 1,
        'name': 'Meeting',
        'interval_minutes': 15,
        'session_minutes': 60,
        'break_after_intervals': 0,
        'break_duration_minutes': 5,
        'color': '#2196F3',
        'icon': 'timer'
    },
    {
        'id': 2,
        'name': 'Coding',
        'interval_minutes': 25,
        'session_minutes': 120,
        'break_after_intervals': 4,
        'break_duration_minutes': 5,
        'color': '#4CAF50',
        'icon': 'code'
    },
    {
        'id': 3,
        'name': 'Chit-chat',
        'interval_minutes': 10,
        'session_minutes': 30,
        'break_after_intervals': 0,
        'break_duration_minutes': 5,
        'color': '#FF9800',
        'icon': 'chat'
    },
    {
        'id': 4,
        'name': 'Quick Check',
        'interval_minutes': 1,
        'session_minutes': 5,
        'break_after_intervals': 0,
        'break_duration_minutes': 5,
        'color': '#E91E63',
        'icon': 'bolt'
    }
]

# Color palette
COLORS = [
    '#2196F3', '#4CAF50', '#FF9800', '#E91E63',
    '#9C27B0', '#00BCD4', '#F44336', '#FFEB3B',
    '#795548', '#607D8B', '#3F51B5', '#009688'
]

# Pomodoro colors
POMODORO_COLORS = {
    'focus': '#E53935',
    'short_break': '#43A047',
    'long_break': '#1E88E5'
}


class DataManager:
    """Handles data persistence"""

    def __init__(self):
        self.data_dir = os.path.dirname(os.path.abspath(__file__))
        self.data_file = os.path.join(self.data_dir, 'r3d1_data.json')
        self.data = self.load_data()

    def load_data(self):
        """Load data from JSON file"""
        default_data = {
            'profiles': DEFAULT_PROFILES.copy(),
            'sessions': [],
            'pomodoro_history': [],
            'settings': {
                'sound': True,
                'vibration': True,
                'dark_theme': True,
                'pomodoro_focus_minutes': 25,
                'pomodoro_short_break_minutes': 5,
                'pomodoro_long_break_minutes': 15,
                'pomodoro_sessions_before_long_break': 4,
                'pomodoro_auto_start_break': True,
                'pomodoro_auto_start_focus': False
            }
        }

        if os.path.exists(self.data_file):
            try:
                with open(self.data_file, 'r') as f:
                    loaded = json.load(f)
                    # Merge with defaults
                    for key in default_data:
                        if key not in loaded:
                            loaded[key] = default_data[key]
                    return loaded
            except (json.JSONDecodeError, IOError):
                return default_data
        return default_data

    def save_data(self):
        """Save data to JSON file"""
        try:
            with open(self.data_file, 'w') as f:
                json.dump(self.data, f, indent=2)
        except IOError as e:
            print(f"Error saving data: {e}")

    def get_profiles(self):
        return self.data['profiles']

    def save_profile(self, profile):
        profiles = self.data['profiles']
        existing = next((p for p in profiles if p['id'] == profile['id']), None)
        if existing:
            profiles[profiles.index(existing)] = profile
        else:
            profile['id'] = max([p['id'] for p in profiles], default=0) + 1
            profiles.append(profile)
        self.save_data()

    def delete_profile(self, profile_id):
        self.data['profiles'] = [p for p in self.data['profiles'] if p['id'] != profile_id]
        self.save_data()

    def add_session(self, session):
        self.data['sessions'].append(session)
        self.save_data()

    def add_pomodoro(self, pomodoro):
        self.data['pomodoro_history'].append(pomodoro)
        self.save_data()

    def get_settings(self):
        return self.data['settings']

    def save_settings(self, settings):
        self.data['settings'] = settings
        self.save_data()

    def get_today_pomodoros(self):
        today = datetime.now().strftime('%Y-%m-%d')
        return sum(1 for p in self.data['pomodoro_history']
                   if p.get('date', '').startswith(today))

    def clear_pomodoro_history(self):
        self.data['pomodoro_history'] = []
        self.save_data()

    def get_weekly_stats(self):
        """Get statistics for the last 7 days"""
        now = datetime.now()
        week_ago = now - timedelta(days=7)

        recent_sessions = [
            s for s in self.data['sessions']
            if datetime.fromisoformat(s['date']) >= week_ago
        ]

        total_minutes = sum(s['duration_seconds'] for s in recent_sessions) // 60
        completed = len([s for s in recent_sessions if s.get('completed', False)])
        active_days = len(set(s['date'][:10] for s in recent_sessions))

        return {
            'total_minutes': total_minutes,
            'completed_sessions': completed,
            'active_days': active_days
        }


class HomeScreen(Screen):
    """Main home screen with profiles and pomodoro tabs"""
    current_tab = StringProperty('profiles')

    def on_enter(self):
        self.refresh_profiles()
        self.update_pomodoro_display()

    def switch_tab(self, tab):
        self.current_tab = tab
        if tab == 'pomodoro':
            self.update_pomodoro_display()

    def refresh_profiles(self):
        """Refresh the profiles grid"""
        grid = self.ids.profiles_grid
        grid.clear_widgets()

        app = App.get_running_app()
        profiles = app.data_manager.get_profiles()

        for profile in profiles:
            card = ProfileCard(profile=profile)
            card.bind(on_press=lambda x, p=profile: self.start_timer(p))
            grid.add_widget(card)

    def start_timer(self, profile):
        """Start timer with selected profile"""
        app = App.get_running_app()
        app.root.get_screen('timer').setup_timer(profile)
        app.root.current = 'timer'

    def open_profile_editor(self, profile=None):
        """Open profile editor screen"""
        app = App.get_running_app()
        app.root.get_screen('editor').setup_editor(profile)
        app.root.current = 'editor'

    def update_pomodoro_display(self):
        """Update pomodoro display"""
        app = App.get_running_app()
        today_count = app.data_manager.get_today_pomodoros()
        settings = app.data_manager.get_settings()

        self.ids.pomodoro_count.text = str(today_count)
        self.ids.tomato_icons.text = self.get_tomato_text(today_count)
        self.ids.pomo_focus_display.text = f"{settings['pomodoro_focus_minutes']} min"
        self.ids.pomo_break_display.text = f"{settings['pomodoro_short_break_minutes']} min"
        self.ids.pomo_long_display.text = f"after {settings['pomodoro_sessions_before_long_break']}"

    def get_tomato_text(self, count):
        """Generate tomato emoji text"""
        if count == 0:
            return ""
        visible = min(count, 12)
        text = "🍅 " * visible
        if count > 12:
            text += f"+{count - 12}"
        return text

    def start_quick_timer(self, minutes, intervals):
        """Start a quick timer"""
        profile = {
            'id': None,
            'name': f'Quick {minutes}min x {intervals}',
            'interval_minutes': minutes,
            'session_minutes': minutes * intervals,
            'break_after_intervals': 0,
            'break_duration_minutes': 5,
            'color': '#4CAF50',
            'icon': 'timer'
        }
        self.start_timer(profile)


class ProfileCard(Button):
    """Custom profile card widget"""
    profile = DictProperty()

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.background_normal = ''
        self.background_color = get_color_from_hex(self.profile.get('color', '#2196F3') + '33')


class TimerScreen(Screen):
    """Timer screen for running intervals"""
    profile_name = StringProperty('')
    timer_display = StringProperty('00:00')
    status_text = StringProperty('READY')
    session_info = StringProperty('Session: 00:00')
    intervals_info = StringProperty('Intervals: 0')
    progress = NumericProperty(0)
    timer_color = StringProperty('#2196F3')

    # Timer state
    status = StringProperty('idle')  # idle, running, paused, break, completed
    remaining_interval = NumericProperty(0)
    remaining_session = NumericProperty(0)
    interval_seconds = NumericProperty(0)
    session_seconds = NumericProperty(0)
    intervals_completed = NumericProperty(0)
    break_remaining = NumericProperty(0)
    break_after = NumericProperty(0)
    break_duration = NumericProperty(0)

    clock_event = ObjectProperty(None, allownone=True)
    start_time = ObjectProperty(None, allownone=True)
    current_profile = DictProperty(None, allownone=True)

    def setup_timer(self, profile):
        """Setup timer with profile settings"""
        self.current_profile = profile
        self.profile_name = profile['name']
        self.timer_color = profile['color']

        self.interval_seconds = profile['interval_minutes'] * 60
        self.session_seconds = profile['session_minutes'] * 60
        self.break_after = profile.get('break_after_intervals', 0)
        self.break_duration = profile.get('break_duration_minutes', 5) * 60

        self.remaining_interval = self.interval_seconds
        self.remaining_session = self.session_seconds
        self.intervals_completed = 0
        self.break_remaining = 0
        self.status = 'idle'
        self.start_time = None

        self.update_display()

    def start_timer(self):
        """Start or resume the timer"""
        if self.status == 'idle':
            self.start_time = datetime.now()

        self.status = 'running'
        self.clock_event = Clock.schedule_interval(self.tick, 1)
        self.update_display()

    def pause_timer(self):
        """Pause the timer"""
        self.status = 'paused'
        if self.clock_event:
            self.clock_event.cancel()
        self.update_display()

    def resume_timer(self):
        """Resume the timer"""
        self.status = 'break' if self.break_remaining > 0 else 'running'
        self.clock_event = Clock.schedule_interval(self.tick, 1)
        self.update_display()

    def stop_timer(self):
        """Stop the timer and save session"""
        if self.clock_event:
            self.clock_event.cancel()

        # Save session
        if self.start_time:
            duration = self.session_seconds - self.remaining_session
            app = App.get_running_app()
            app.data_manager.add_session({
                'profile_name': self.profile_name,
                'date': datetime.now().isoformat(),
                'duration_seconds': duration,
                'intervals_completed': self.intervals_completed,
                'completed': self.remaining_session <= 0
            })

        self.status = 'idle'
        app = App.get_running_app()
        app.root.current = 'home'

    def skip_break(self):
        """Skip the current break"""
        self.break_remaining = 0
        self.status = 'running'
        self.update_display()

    def tick(self, dt):
        """Timer tick - called every second"""
        if self.status == 'break':
            self.break_remaining -= 1
            self.remaining_session -= 1

            if self.break_remaining <= 0:
                self.play_alert()
                self.status = 'running'

        elif self.status == 'running':
            self.remaining_interval -= 1
            self.remaining_session -= 1

            # Session completed
            if self.remaining_session <= 0:
                self.complete_session()
                return

            # Interval completed
            if self.remaining_interval <= 0:
                self.intervals_completed += 1
                self.play_alert()

                # Check for break
                if self.break_after > 0 and self.intervals_completed % self.break_after == 0:
                    self.status = 'break'
                    self.break_remaining = self.break_duration

                # Reset interval
                self.remaining_interval = self.interval_seconds

        self.update_display()

    def complete_session(self):
        """Handle session completion"""
        if self.clock_event:
            self.clock_event.cancel()

        self.status = 'completed'
        self.play_alert()

        # Save session
        app = App.get_running_app()
        app.data_manager.add_session({
            'profile_name': self.profile_name,
            'date': datetime.now().isoformat(),
            'duration_seconds': self.session_seconds,
            'intervals_completed': self.intervals_completed,
            'completed': True
        })

        self.update_display()

    def update_display(self):
        """Update all display elements"""
        is_break = self.status == 'break'
        seconds = self.break_remaining if is_break else self.remaining_interval

        mins = seconds // 60
        secs = seconds % 60
        self.timer_display = f"{mins:02d}:{secs:02d}"

        session_mins = self.remaining_session // 60
        session_secs = self.remaining_session % 60
        self.session_info = f"Session: {session_mins:02d}:{session_secs:02d}"
        self.intervals_info = f"Intervals: {self.intervals_completed}"

        # Progress
        total = self.break_duration if is_break else self.interval_seconds
        if total > 0:
            self.progress = 1 - (seconds / total)

        # Status text
        status_labels = {
            'idle': 'READY',
            'running': 'RUNNING',
            'paused': 'PAUSED',
            'break': 'BREAK TIME',
            'completed': 'COMPLETED'
        }
        self.status_text = status_labels.get(self.status, 'READY')

    def play_alert(self):
        """Play alert sound"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()
        if settings.get('sound', True):
            # In a real app, you'd load and play a sound file
            print("BEEP!")  # Placeholder


class PomodoroScreen(Screen):
    """Dedicated Pomodoro timer screen"""
    timer_display = StringProperty('25:00')
    status_text = StringProperty('Ready to Focus')
    today_count = NumericProperty(0)
    progress = NumericProperty(0)
    timer_color = StringProperty('#E53935')

    status = StringProperty('idle')  # idle, focus, short_break, long_break, paused
    remaining_seconds = NumericProperty(0)
    total_seconds = NumericProperty(0)
    completed_pomodoros = NumericProperty(0)
    current_task = StringProperty('')
    paused_from = StringProperty('')

    clock_event = ObjectProperty(None, allownone=True)

    def on_enter(self):
        self.update_display()
        app = App.get_running_app()
        self.today_count = app.data_manager.get_today_pomodoros()

    def start_pomodoro(self):
        """Start a pomodoro focus session"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        self.current_task = self.ids.task_input.text.strip()
        self.status = 'focus'
        self.total_seconds = settings['pomodoro_focus_minutes'] * 60
        self.remaining_seconds = self.total_seconds
        self.timer_color = POMODORO_COLORS['focus']

        self.clock_event = Clock.schedule_interval(self.tick, 1)
        self.update_display()

    def pause_pomodoro(self):
        """Pause the pomodoro"""
        self.paused_from = self.status
        self.status = 'paused'
        if self.clock_event:
            self.clock_event.cancel()
        self.update_display()

    def resume_pomodoro(self):
        """Resume the pomodoro"""
        self.status = self.paused_from or 'focus'
        self.paused_from = ''
        self.clock_event = Clock.schedule_interval(self.tick, 1)
        self.update_display()

    def stop_pomodoro(self):
        """Stop the pomodoro"""
        if self.clock_event:
            self.clock_event.cancel()

        self.status = 'idle'
        self.remaining_seconds = 0
        self.total_seconds = 0
        self.timer_color = POMODORO_COLORS['focus']
        self.update_display()

    def tick(self, dt):
        """Timer tick"""
        self.remaining_seconds -= 1

        if self.remaining_seconds <= 0:
            self.complete_phase()

        self.update_display()

    def complete_phase(self):
        """Complete current phase"""
        if self.clock_event:
            self.clock_event.cancel()

        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        if self.status == 'focus':
            # Completed a pomodoro
            self.completed_pomodoros += 1
            self.today_count += 1

            # Save to history
            app.data_manager.add_pomodoro({
                'date': datetime.now().isoformat(),
                'task': self.current_task,
                'duration': settings['pomodoro_focus_minutes']
            })

            self.play_alert()

            # Determine break type
            is_long_break = self.completed_pomodoros % settings['pomodoro_sessions_before_long_break'] == 0

            if settings['pomodoro_auto_start_break']:
                if is_long_break:
                    self.start_long_break()
                else:
                    self.start_short_break()
            else:
                self.status = 'idle'
                self.show_break_prompt(is_long_break)

        elif self.status in ['short_break', 'long_break']:
            self.play_alert()
            if settings['pomodoro_auto_start_focus']:
                self.start_pomodoro()
            else:
                self.status = 'idle'

        self.update_display()

    def start_short_break(self):
        """Start short break"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        self.status = 'short_break'
        self.total_seconds = settings['pomodoro_short_break_minutes'] * 60
        self.remaining_seconds = self.total_seconds
        self.timer_color = POMODORO_COLORS['short_break']

        self.clock_event = Clock.schedule_interval(self.tick, 1)
        self.update_display()

    def start_long_break(self):
        """Start long break"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        self.status = 'long_break'
        self.total_seconds = settings['pomodoro_long_break_minutes'] * 60
        self.remaining_seconds = self.total_seconds
        self.timer_color = POMODORO_COLORS['long_break']

        self.clock_event = Clock.schedule_interval(self.tick, 1)
        self.update_display()

    def show_break_prompt(self, is_long):
        """Show break prompt popup"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        break_type = 'long' if is_long else 'short'
        minutes = settings['pomodoro_long_break_minutes'] if is_long else settings['pomodoro_short_break_minutes']

        content = BoxLayout(orientation='vertical', padding=20, spacing=10)
        content.add_widget(Label(
            text=f"Time for a {break_type} break!\n({minutes} min)",
            halign='center'
        ))

        buttons = BoxLayout(size_hint_y=None, height=dp(50), spacing=10)
        skip_btn = Button(text='Skip')
        start_btn = Button(text='Start Break')
        buttons.add_widget(skip_btn)
        buttons.add_widget(start_btn)
        content.add_widget(buttons)

        popup = Popup(
            title='Break Time',
            content=content,
            size_hint=(0.8, 0.4),
            auto_dismiss=False
        )

        skip_btn.bind(on_press=popup.dismiss)
        start_btn.bind(on_press=lambda x: self.on_break_accepted(popup, is_long))
        popup.open()

    def on_break_accepted(self, popup, is_long):
        popup.dismiss()
        if is_long:
            self.start_long_break()
        else:
            self.start_short_break()

    def update_display(self):
        """Update display elements"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        if self.status == 'idle':
            display_seconds = settings['pomodoro_focus_minutes'] * 60
        else:
            display_seconds = self.remaining_seconds

        mins = display_seconds // 60
        secs = display_seconds % 60
        self.timer_display = f"{mins:02d}:{secs:02d}"

        # Status text
        status_labels = {
            'idle': 'Ready to Focus',
            'focus': 'Focus Time',
            'short_break': 'Short Break',
            'long_break': 'Long Break',
            'paused': 'Paused'
        }
        self.status_text = status_labels.get(self.status, 'Ready')

        # Progress
        if self.total_seconds > 0 and self.status != 'idle':
            self.progress = 1 - (self.remaining_seconds / self.total_seconds)
        else:
            self.progress = 0

        # Color
        if self.status == 'short_break':
            self.timer_color = POMODORO_COLORS['short_break']
        elif self.status == 'long_break':
            self.timer_color = POMODORO_COLORS['long_break']
        else:
            self.timer_color = POMODORO_COLORS['focus']

    def play_alert(self):
        """Play alert"""
        print("POMODORO ALERT!")


class EditorScreen(Screen):
    """Profile editor screen"""
    editing_profile = DictProperty(None, allownone=True)
    selected_color = StringProperty('#2196F3')

    def setup_editor(self, profile=None):
        """Setup editor with profile data"""
        self.editing_profile = profile

        if profile:
            self.ids.profile_name.text = profile['name']
            self.ids.interval_slider.value = profile['interval_minutes']
            self.ids.session_slider.value = profile['session_minutes']
            self.ids.break_toggle.active = profile.get('break_after_intervals', 0) > 0
            self.ids.break_interval_slider.value = profile.get('break_after_intervals', 4) or 4
            self.ids.break_duration_slider.value = profile.get('break_duration_minutes', 5)
            self.selected_color = profile['color']
        else:
            self.ids.profile_name.text = ''
            self.ids.interval_slider.value = 25
            self.ids.session_slider.value = 60
            self.ids.break_toggle.active = False
            self.ids.break_interval_slider.value = 4
            self.ids.break_duration_slider.value = 5
            self.selected_color = '#2196F3'

    def save_profile(self):
        """Save the profile"""
        name = self.ids.profile_name.text.strip()
        if not name:
            return

        profile = {
            'id': self.editing_profile['id'] if self.editing_profile else None,
            'name': name,
            'interval_minutes': int(self.ids.interval_slider.value),
            'session_minutes': int(self.ids.session_slider.value),
            'break_after_intervals': int(self.ids.break_interval_slider.value) if self.ids.break_toggle.active else 0,
            'break_duration_minutes': int(self.ids.break_duration_slider.value),
            'color': self.selected_color,
            'icon': 'timer'
        }

        app = App.get_running_app()
        app.data_manager.save_profile(profile)
        app.root.current = 'home'

    def delete_profile(self):
        """Delete the profile"""
        if self.editing_profile:
            app = App.get_running_app()
            app.data_manager.delete_profile(self.editing_profile['id'])
            app.root.current = 'home'

    def select_color(self, color):
        """Select a color"""
        self.selected_color = color


class SettingsScreen(Screen):
    """Settings screen"""

    def on_enter(self):
        self.load_settings()

    def load_settings(self):
        """Load current settings"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        self.ids.sound_toggle.active = settings.get('sound', True)
        self.ids.vibration_toggle.active = settings.get('vibration', True)
        self.ids.dark_toggle.active = settings.get('dark_theme', True)

    def save_setting(self, key, value):
        """Save a single setting"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()
        settings[key] = value
        app.data_manager.save_settings(settings)


class PomodoroSettingsScreen(Screen):
    """Pomodoro settings screen"""

    def on_enter(self):
        self.load_settings()

    def load_settings(self):
        """Load pomodoro settings"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        self.ids.focus_slider.value = settings['pomodoro_focus_minutes']
        self.ids.short_break_slider.value = settings['pomodoro_short_break_minutes']
        self.ids.long_break_slider.value = settings['pomodoro_long_break_minutes']
        self.ids.sessions_slider.value = settings['pomodoro_sessions_before_long_break']
        self.ids.auto_break_toggle.active = settings['pomodoro_auto_start_break']
        self.ids.auto_focus_toggle.active = settings['pomodoro_auto_start_focus']

    def save_settings(self):
        """Save pomodoro settings"""
        app = App.get_running_app()
        settings = app.data_manager.get_settings()

        settings['pomodoro_focus_minutes'] = int(self.ids.focus_slider.value)
        settings['pomodoro_short_break_minutes'] = int(self.ids.short_break_slider.value)
        settings['pomodoro_long_break_minutes'] = int(self.ids.long_break_slider.value)
        settings['pomodoro_sessions_before_long_break'] = int(self.ids.sessions_slider.value)
        settings['pomodoro_auto_start_break'] = self.ids.auto_break_toggle.active
        settings['pomodoro_auto_start_focus'] = self.ids.auto_focus_toggle.active

        app.data_manager.save_settings(settings)
        app.root.current = 'home'
        app.root.get_screen('home').switch_tab('pomodoro')


class StatsScreen(Screen):
    """Statistics screen"""
    total_minutes = StringProperty('0')
    completed_sessions = StringProperty('0')
    active_days = StringProperty('0')

    def on_enter(self):
        self.load_stats()

    def load_stats(self):
        """Load statistics"""
        app = App.get_running_app()
        stats = app.data_manager.get_weekly_stats()

        self.total_minutes = str(stats['total_minutes'])
        self.completed_sessions = str(stats['completed_sessions'])
        self.active_days = str(stats['active_days'])


class R3D1IntervalApp(App):
    """Main application class"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.data_manager = DataManager()

    def build(self):
        # Create screen manager
        sm = ScreenManager(transition=SlideTransition())

        # Add screens
        sm.add_widget(HomeScreen(name='home'))
        sm.add_widget(TimerScreen(name='timer'))
        sm.add_widget(PomodoroScreen(name='pomodoro'))
        sm.add_widget(EditorScreen(name='editor'))
        sm.add_widget(SettingsScreen(name='settings'))
        sm.add_widget(PomodoroSettingsScreen(name='pomo_settings'))
        sm.add_widget(StatsScreen(name='stats'))

        return sm

    def on_stop(self):
        """Save data on app close"""
        self.data_manager.save_data()


if __name__ == '__main__':
    R3D1IntervalApp().run()
