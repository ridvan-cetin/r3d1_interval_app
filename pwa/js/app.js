// R3D1 Interval Timer - PWA
// ========================

// Default profiles
const DEFAULT_PROFILES = [
    {
        id: 1,
        name: 'Meeting',
        intervalMinutes: 15,
        sessionMinutes: 60,
        breakAfterIntervals: 0,
        breakDurationMinutes: 5,
        color: '#2196F3',
        isDefault: true
    },
    {
        id: 2,
        name: 'Coding',
        intervalMinutes: 25,
        sessionMinutes: 120,
        breakAfterIntervals: 4,
        breakDurationMinutes: 5,
        color: '#4CAF50',
        isDefault: true
    },
    {
        id: 3,
        name: 'Chit-chat',
        intervalMinutes: 10,
        sessionMinutes: 30,
        breakAfterIntervals: 0,
        breakDurationMinutes: 5,
        color: '#FF9800',
        isDefault: true
    },
    {
        id: 4,
        name: 'Quick Check',
        intervalMinutes: 1,
        sessionMinutes: 5,
        breakAfterIntervals: 0,
        breakDurationMinutes: 5,
        color: '#E91E63',
        isDefault: true
    }
];

// App State
let state = {
    profiles: [],
    sessions: [],
    settings: {
        sound: true,
        vibration: true,
        wakelock: false
    },
    timer: {
        status: 'idle', // idle, running, paused, break, completed
        profileId: null,
        intervalSeconds: 0,
        remainingIntervalSeconds: 0,
        sessionSeconds: 0,
        remainingSessionSeconds: 0,
        intervalsCompleted: 0,
        breakRemainingSeconds: 0,
        startTime: null
    },
    currentScreen: 'home',
    editingProfileId: null
};

let timerInterval = null;
let wakeLock = null;
let audioContext = null;

// Initialize app
function init() {
    loadState();
    renderProfiles();
    setupEventListeners();
    applySettings();
    registerServiceWorker();
}

// Local Storage
function loadState() {
    const savedProfiles = localStorage.getItem('r3d1_profiles');
    const savedSessions = localStorage.getItem('r3d1_sessions');
    const savedSettings = localStorage.getItem('r3d1_settings');

    state.profiles = savedProfiles ? JSON.parse(savedProfiles) : [...DEFAULT_PROFILES];
    state.sessions = savedSessions ? JSON.parse(savedSessions) : [];
    state.settings = savedSettings ? JSON.parse(savedSettings) : state.settings;
}

function saveProfiles() {
    localStorage.setItem('r3d1_profiles', JSON.stringify(state.profiles));
}

function saveSessions() {
    localStorage.setItem('r3d1_sessions', JSON.stringify(state.sessions));
}

function saveSettings() {
    localStorage.setItem('r3d1_settings', JSON.stringify(state.settings));
}

// Screen Navigation
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(`${screenId}-screen`).classList.add('active');
    state.currentScreen = screenId;
}

// Render Profiles
function renderProfiles() {
    const grid = document.getElementById('profiles-grid');
    grid.innerHTML = state.profiles.map(profile => `
        <div class="profile-card" data-id="${profile.id}" style="--profile-color: ${profile.color}">
            <div class="profile-icon" style="background: ${profile.color}33">
                <svg viewBox="0 0 24 24" style="fill: ${profile.color}">
                    <path d="M15 1H9v2h6V1zm-4 13h2V8h-2v6zm8.03-6.61l1.42-1.42c-.43-.51-.9-.99-1.41-1.41l-1.42 1.42A8.962 8.962 0 0012 4c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-2.12-.74-4.07-1.97-5.61zM12 20c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"/>
                </svg>
            </div>
            <div>
                <div class="profile-name">${profile.name}</div>
                <div class="profile-info">
                    ${profile.intervalMinutes} min intervals<br>
                    ${profile.sessionMinutes} min total
                    ${profile.breakAfterIntervals > 0 ? `<br><span style="color:${profile.color}">Break every ${profile.breakAfterIntervals}</span>` : ''}
                </div>
            </div>
            <button class="profile-edit-btn" data-edit="${profile.id}">
                <svg viewBox="0 0 24 24"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
            </button>
        </div>
    `).join('');

    // Add click handlers
    grid.querySelectorAll('.profile-card').forEach(card => {
        card.addEventListener('click', (e) => {
            if (!e.target.closest('.profile-edit-btn')) {
                startTimerWithProfile(parseInt(card.dataset.id));
            }
        });
    });

    grid.querySelectorAll('.profile-edit-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            openProfileEditor(parseInt(btn.dataset.edit));
        });
    });
}

// Timer Functions
function startTimerWithProfile(profileId) {
    const profile = state.profiles.find(p => p.id === profileId);
    if (!profile) return;

    state.timer = {
        status: 'idle',
        profileId: profileId,
        intervalSeconds: profile.intervalMinutes * 60,
        remainingIntervalSeconds: profile.intervalMinutes * 60,
        sessionSeconds: profile.sessionMinutes * 60,
        remainingSessionSeconds: profile.sessionMinutes * 60,
        intervalsCompleted: 0,
        breakRemainingSeconds: 0,
        breakAfterIntervals: profile.breakAfterIntervals,
        breakDurationSeconds: profile.breakDurationMinutes * 60,
        startTime: null
    };

    document.getElementById('timer-profile-name').textContent = profile.name;
    document.getElementById('progress-circle').style.stroke = profile.color;

    updateTimerDisplay();
    showScreen('timer');
    showTimerControls('idle');
}

function startTimer() {
    if (state.timer.status === 'idle') {
        state.timer.startTime = Date.now();
    }
    state.timer.status = 'running';
    updateStatusBadge();
    showTimerControls('running');

    requestWakeLock();

    timerInterval = setInterval(timerTick, 1000);
}

function pauseTimer() {
    state.timer.status = 'paused';
    clearInterval(timerInterval);
    updateStatusBadge();
    showTimerControls('paused');
}

function resumeTimer() {
    state.timer.status = state.timer.breakRemainingSeconds > 0 ? 'break' : 'running';
    updateStatusBadge();
    showTimerControls(state.timer.status);
    timerInterval = setInterval(timerTick, 1000);
}

function stopTimer() {
    clearInterval(timerInterval);
    releaseWakeLock();

    // Save session
    if (state.timer.startTime) {
        const profile = state.profiles.find(p => p.id === state.timer.profileId);
        const duration = state.timer.sessionSeconds - state.timer.remainingSessionSeconds;

        state.sessions.push({
            profileName: profile ? profile.name : 'Unknown',
            date: new Date().toISOString(),
            durationSeconds: duration,
            intervalsCompleted: state.timer.intervalsCompleted,
            completed: state.timer.remainingSessionSeconds <= 0
        });
        saveSessions();
    }

    showScreen('home');
    state.timer.status = 'idle';
}

function skipBreak() {
    state.timer.breakRemainingSeconds = 0;
    state.timer.status = 'running';
    updateStatusBadge();
    showTimerControls('running');
}

function timerTick() {
    if (state.timer.status === 'break') {
        state.timer.breakRemainingSeconds--;
        state.timer.remainingSessionSeconds--;

        if (state.timer.breakRemainingSeconds <= 0) {
            playAlert();
            state.timer.status = 'running';
            updateStatusBadge();
            showTimerControls('running');
        }
    } else if (state.timer.status === 'running') {
        state.timer.remainingIntervalSeconds--;
        state.timer.remainingSessionSeconds--;

        // Session completed
        if (state.timer.remainingSessionSeconds <= 0) {
            completeSession();
            return;
        }

        // Interval completed
        if (state.timer.remainingIntervalSeconds <= 0) {
            state.timer.intervalsCompleted++;
            playAlert();

            // Check for break
            if (state.timer.breakAfterIntervals > 0 &&
                state.timer.intervalsCompleted % state.timer.breakAfterIntervals === 0) {
                state.timer.status = 'break';
                state.timer.breakRemainingSeconds = state.timer.breakDurationSeconds;
                updateStatusBadge();
                showTimerControls('break');
            }

            // Reset interval
            state.timer.remainingIntervalSeconds = state.timer.intervalSeconds;
        }
    }

    updateTimerDisplay();
}

function completeSession() {
    clearInterval(timerInterval);
    releaseWakeLock();

    state.timer.status = 'completed';
    playAlert();
    updateStatusBadge();
    showTimerControls('completed');

    // Save session
    const profile = state.profiles.find(p => p.id === state.timer.profileId);
    state.sessions.push({
        profileName: profile ? profile.name : 'Unknown',
        date: new Date().toISOString(),
        durationSeconds: state.timer.sessionSeconds,
        intervalsCompleted: state.timer.intervalsCompleted,
        completed: true
    });
    saveSessions();
}

function updateTimerDisplay() {
    const isBreak = state.timer.status === 'break';
    const seconds = isBreak ? state.timer.breakRemainingSeconds : state.timer.remainingIntervalSeconds;

    document.getElementById('timer-label').textContent = isBreak ? 'Break' : 'Interval';
    document.getElementById('timer-time').textContent = formatTime(seconds);
    document.getElementById('intervals-count').textContent = `Intervals: ${state.timer.intervalsCompleted}`;
    document.getElementById('session-time').textContent = `Session: ${formatTime(state.timer.remainingSessionSeconds)}`;

    // Update progress ring
    const totalSeconds = isBreak ? state.timer.breakDurationSeconds : state.timer.intervalSeconds;
    const progress = 1 - (seconds / totalSeconds);
    const circumference = 2 * Math.PI * 90; // r=90
    const offset = circumference - (progress * circumference);
    document.getElementById('progress-circle').style.strokeDashoffset = offset;
}

function updateStatusBadge() {
    const badge = document.getElementById('timer-status');
    badge.className = 'status-badge ' + state.timer.status;

    const labels = {
        idle: 'READY',
        running: 'RUNNING',
        paused: 'PAUSED',
        break: 'BREAK TIME',
        completed: 'COMPLETED'
    };
    badge.textContent = labels[state.timer.status];
}

function showTimerControls(status) {
    const controls = document.getElementById('timer-controls');
    const profile = state.profiles.find(p => p.id === state.timer.profileId);
    const color = profile ? profile.color : '#2196F3';

    switch(status) {
        case 'idle':
            controls.innerHTML = `
                <button id="start-btn" class="control-btn primary" style="background:${color}">
                    <svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
                </button>
            `;
            break;
        case 'running':
            controls.innerHTML = `
                <button id="stop-btn" class="control-btn">
                    <svg viewBox="0 0 24 24"><path d="M6 6h12v12H6z"/></svg>
                </button>
                <button id="pause-btn" class="control-btn primary" style="background:${color}">
                    <svg viewBox="0 0 24 24"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>
                </button>
            `;
            break;
        case 'paused':
            controls.innerHTML = `
                <button id="stop-btn" class="control-btn">
                    <svg viewBox="0 0 24 24"><path d="M6 6h12v12H6z"/></svg>
                </button>
                <button id="resume-btn" class="control-btn primary" style="background:${color}">
                    <svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
                </button>
            `;
            break;
        case 'break':
            controls.innerHTML = `
                <button id="stop-btn" class="control-btn">
                    <svg viewBox="0 0 24 24"><path d="M6 6h12v12H6z"/></svg>
                </button>
                <button id="skip-btn" class="control-btn primary" style="background:#FF9800">
                    <svg viewBox="0 0 24 24"><path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/></svg>
                </button>
            `;
            break;
        case 'completed':
            controls.innerHTML = `
                <button id="done-btn" class="control-btn primary" style="background:#4CAF50">
                    <svg viewBox="0 0 24 24"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
                </button>
            `;
            break;
    }

    // Attach event listeners
    const startBtn = document.getElementById('start-btn');
    const pauseBtn = document.getElementById('pause-btn');
    const resumeBtn = document.getElementById('resume-btn');
    const stopBtn = document.getElementById('stop-btn');
    const skipBtn = document.getElementById('skip-btn');
    const doneBtn = document.getElementById('done-btn');

    if (startBtn) startBtn.addEventListener('click', startTimer);
    if (pauseBtn) pauseBtn.addEventListener('click', pauseTimer);
    if (resumeBtn) resumeBtn.addEventListener('click', resumeTimer);
    if (stopBtn) stopBtn.addEventListener('click', stopTimer);
    if (skipBtn) skipBtn.addEventListener('click', skipBreak);
    if (doneBtn) doneBtn.addEventListener('click', () => showScreen('home'));
}

// Profile Editor
function openProfileEditor(profileId = null) {
    state.editingProfileId = profileId;
    const profile = profileId ? state.profiles.find(p => p.id === profileId) : null;

    document.getElementById('editor-title').textContent = profile ? 'Edit Profile' : 'New Profile';
    document.getElementById('profile-name').value = profile ? profile.name : '';
    document.getElementById('interval-slider').value = profile ? profile.intervalMinutes : 25;
    document.getElementById('interval-value').textContent = profile ? profile.intervalMinutes : 25;
    document.getElementById('session-slider').value = profile ? profile.sessionMinutes : 60;
    document.getElementById('session-value').textContent = profile ? profile.sessionMinutes : 60;
    document.getElementById('break-toggle').checked = profile ? profile.breakAfterIntervals > 0 : false;
    document.getElementById('break-interval-slider').value = profile ? profile.breakAfterIntervals || 4 : 4;
    document.getElementById('break-interval-value').textContent = profile ? profile.breakAfterIntervals || 4 : 4;
    document.getElementById('break-duration-slider').value = profile ? profile.breakDurationMinutes : 5;
    document.getElementById('break-duration-value').textContent = profile ? profile.breakDurationMinutes : 5;

    // Update break settings visibility
    document.getElementById('break-settings').classList.toggle('hidden', !(profile && profile.breakAfterIntervals > 0));

    // Color selection
    const selectedColor = profile ? profile.color : '#2196F3';
    document.querySelectorAll('.color-btn').forEach(btn => {
        btn.classList.toggle('selected', btn.dataset.color === selectedColor);
    });

    // Show/hide delete button
    document.getElementById('delete-profile-btn').classList.toggle('hidden', !profile || profile.isDefault);

    showScreen('editor');
}

function saveProfile() {
    const name = document.getElementById('profile-name').value.trim();
    if (!name) {
        alert('Please enter a profile name');
        return;
    }

    const selectedColor = document.querySelector('.color-btn.selected');
    const breakEnabled = document.getElementById('break-toggle').checked;

    const profileData = {
        name: name,
        intervalMinutes: parseInt(document.getElementById('interval-slider').value),
        sessionMinutes: parseInt(document.getElementById('session-slider').value),
        breakAfterIntervals: breakEnabled ? parseInt(document.getElementById('break-interval-slider').value) : 0,
        breakDurationMinutes: parseInt(document.getElementById('break-duration-slider').value),
        color: selectedColor ? selectedColor.dataset.color : '#2196F3',
        isDefault: false
    };

    if (state.editingProfileId) {
        const index = state.profiles.findIndex(p => p.id === state.editingProfileId);
        if (index !== -1) {
            state.profiles[index] = { ...state.profiles[index], ...profileData };
        }
    } else {
        profileData.id = Date.now();
        state.profiles.push(profileData);
    }

    saveProfiles();
    renderProfiles();
    showScreen('home');
}

function deleteProfile() {
    if (!state.editingProfileId) return;

    if (confirm('Delete this profile?')) {
        state.profiles = state.profiles.filter(p => p.id !== state.editingProfileId);
        saveProfiles();
        renderProfiles();
        showScreen('home');
    }
}

// Statistics
function renderStatistics() {
    const now = new Date();
    const weekAgo = new Date(now - 7 * 24 * 60 * 60 * 1000);

    const recentSessions = state.sessions.filter(s => new Date(s.date) >= weekAgo);

    const totalMinutes = Math.round(recentSessions.reduce((sum, s) => sum + s.durationSeconds, 0) / 60);
    const completedSessions = recentSessions.filter(s => s.completed).length;
    const activeDays = new Set(recentSessions.map(s => new Date(s.date).toDateString())).size;
    const streak = calculateStreak();

    document.getElementById('stat-minutes').textContent = totalMinutes;
    document.getElementById('stat-sessions').textContent = completedSessions;
    document.getElementById('stat-days').textContent = activeDays;
    document.getElementById('stat-streak').textContent = streak;

    // Render history
    const historyList = document.getElementById('history-list');
    if (state.sessions.length === 0) {
        historyList.innerHTML = '<p class="empty-state">No sessions yet</p>';
    } else {
        const sortedSessions = [...state.sessions].sort((a, b) => new Date(b.date) - new Date(a.date)).slice(0, 20);
        historyList.innerHTML = sortedSessions.map(s => `
            <div class="history-item">
                <div class="history-item-info">
                    <span class="history-item-name">${s.profileName}</span>
                    <span class="history-item-date">${formatDate(s.date)}</span>
                </div>
                <span class="history-item-duration">${Math.round(s.durationSeconds / 60)} min</span>
            </div>
        `).join('');
    }
}

function calculateStreak() {
    if (state.sessions.length === 0) return 0;

    const dates = [...new Set(state.sessions.map(s => new Date(s.date).toDateString()))];
    dates.sort((a, b) => new Date(b) - new Date(a));

    let streak = 0;
    let currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0);

    for (const dateStr of dates) {
        const sessionDate = new Date(dateStr);
        sessionDate.setHours(0, 0, 0, 0);

        const diffDays = Math.round((currentDate - sessionDate) / (24 * 60 * 60 * 1000));

        if (diffDays === 0 || diffDays === 1) {
            streak++;
            currentDate = sessionDate;
            currentDate.setDate(currentDate.getDate() - 1);
        } else if (streak === 0 && diffDays === 1) {
            // Allow starting from yesterday
            streak++;
            currentDate = sessionDate;
        } else {
            break;
        }
    }

    return streak;
}

// Settings
function applySettings() {
    document.getElementById('sound-toggle').checked = state.settings.sound;
    document.getElementById('vibration-toggle').checked = state.settings.vibration;
    document.getElementById('wakelock-toggle').checked = state.settings.wakelock;
}

// Audio & Vibration
function playAlert() {
    if (state.settings.sound) {
        playBeep();
    }
    if (state.settings.vibration && navigator.vibrate) {
        navigator.vibrate([200, 100, 200]);
    }
}

function playBeep() {
    try {
        if (!audioContext) {
            audioContext = new (window.AudioContext || window.webkitAudioContext)();
        }

        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.value = 800;
        oscillator.type = 'sine';

        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);

        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.3);
    } catch (e) {
        console.log('Audio not supported');
    }
}

// Wake Lock
async function requestWakeLock() {
    if (!state.settings.wakelock) return;

    try {
        if ('wakeLock' in navigator) {
            wakeLock = await navigator.wakeLock.request('screen');
        }
    } catch (e) {
        console.log('Wake Lock not supported');
    }
}

function releaseWakeLock() {
    if (wakeLock) {
        wakeLock.release();
        wakeLock = null;
    }
}

// Utilities
function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
        return 'Today ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
    } else {
        return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
    }
}

// Service Worker
function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('sw.js')
            .then(reg => console.log('Service Worker registered'))
            .catch(err => console.log('Service Worker registration failed:', err));
    }
}

// Event Listeners
function setupEventListeners() {
    // Navigation
    document.getElementById('stats-btn').addEventListener('click', () => {
        renderStatistics();
        showScreen('stats');
    });
    document.getElementById('settings-btn').addEventListener('click', () => showScreen('settings'));
    document.getElementById('add-profile-btn').addEventListener('click', () => openProfileEditor());

    document.getElementById('timer-back-btn').addEventListener('click', () => {
        if (state.timer.status !== 'idle' && state.timer.status !== 'completed') {
            if (confirm('Stop the timer?')) {
                stopTimer();
            }
        } else {
            showScreen('home');
        }
    });

    document.getElementById('editor-close-btn').addEventListener('click', () => showScreen('home'));
    document.getElementById('editor-save-btn').addEventListener('click', saveProfile);
    document.getElementById('delete-profile-btn').addEventListener('click', deleteProfile);

    document.getElementById('settings-back-btn').addEventListener('click', () => showScreen('home'));
    document.getElementById('stats-back-btn').addEventListener('click', () => showScreen('home'));

    // Editor sliders
    document.getElementById('interval-slider').addEventListener('input', (e) => {
        document.getElementById('interval-value').textContent = e.target.value;
    });
    document.getElementById('session-slider').addEventListener('input', (e) => {
        document.getElementById('session-value').textContent = e.target.value;
    });
    document.getElementById('break-toggle').addEventListener('change', (e) => {
        document.getElementById('break-settings').classList.toggle('hidden', !e.target.checked);
    });
    document.getElementById('break-interval-slider').addEventListener('input', (e) => {
        document.getElementById('break-interval-value').textContent = e.target.value;
    });
    document.getElementById('break-duration-slider').addEventListener('input', (e) => {
        document.getElementById('break-duration-value').textContent = e.target.value;
    });

    // Color selection
    document.querySelectorAll('.color-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
        });
    });

    // Settings toggles
    document.getElementById('sound-toggle').addEventListener('change', (e) => {
        state.settings.sound = e.target.checked;
        saveSettings();
        // Initialize audio context on user interaction
        if (e.target.checked && !audioContext) {
            audioContext = new (window.AudioContext || window.webkitAudioContext)();
        }
    });
    document.getElementById('vibration-toggle').addEventListener('change', (e) => {
        state.settings.vibration = e.target.checked;
        saveSettings();
    });
    document.getElementById('wakelock-toggle').addEventListener('change', (e) => {
        state.settings.wakelock = e.target.checked;
        saveSettings();
    });

    document.getElementById('clear-stats-btn').addEventListener('click', () => {
        if (confirm('Clear all statistics?')) {
            state.sessions = [];
            saveSessions();
            renderStatistics();
        }
    });
    document.getElementById('reset-profiles-btn').addEventListener('click', () => {
        if (confirm('Reset to default profiles?')) {
            state.profiles = [...DEFAULT_PROFILES];
            saveProfiles();
            renderProfiles();
            showScreen('home');
        }
    });

    // Handle visibility change (pause wake lock when hidden)
    document.addEventListener('visibilitychange', async () => {
        if (document.visibilityState === 'visible' && state.timer.status === 'running') {
            await requestWakeLock();
        }
    });
}

// Initialize
document.addEventListener('DOMContentLoaded', init);
