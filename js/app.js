// R3D1 Interval Timer - PWA
// ========================

// Icon SVG paths
const PROFILE_ICONS = {
    timer: 'M15 1H9v2h6V1zm-4 13h2V8h-2v6zm8.03-6.61l1.42-1.42c-.43-.51-.9-.99-1.41-1.41l-1.42 1.42A8.962 8.962 0 0012 4c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-2.12-.74-4.07-1.97-5.61zM12 20c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z',
    code: 'M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z',
    chat: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z',
    meeting: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
    coffee: 'M20 3H4v10c0 2.21 1.79 4 4 4h6c2.21 0 4-1.79 4-4v-3h2c1.11 0 2-.89 2-2V5c0-1.11-.89-2-2-2zm0 5h-2V5h2v3zM4 19h16v2H4v-2z',
    bolt: 'M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.51c.4 0 .62.19.4.66C12.97 17.55 11 21 11 21z',
    fitness: 'M20.57 14.86L22 13.43 20.57 12 17 15.57 8.43 7 12 3.43 10.57 2 9.14 3.43 7.71 2 5.57 4.14 4.14 2.71 2.71 4.14l1.43 1.43L2 7.71l1.43 1.43L2 10.57 3.43 12 7 8.43 15.57 17 12 20.57 13.43 22l1.43-1.43L16.29 22l2.14-2.14 1.43 1.43 1.43-1.43-1.43-1.43L22 16.29l-1.43-1.43z',
    book: 'M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z',
    // New icons
    presentation: 'M21 3H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h5v2h8v-2h5c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 14H3V5h18v12z',
    writing: 'M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z',
    walking: 'M13.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM9.8 8.9L7 23h2.1l1.8-8 2.1 2v6h2v-7.5l-2.1-2 .6-3C14.8 12 16.8 13 19 13v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.4-.6-1-1-1.7-1-.3 0-.5.1-.8.1L6 8.3V13h2V9.6l1.8-.7',
    science: 'M19.8 18.4L14 10.67V6.5l1.35-1.69c.26-.33.03-.81-.39-.81H9.04c-.42 0-.65.48-.39.81L10 6.5v4.17L4.2 18.4c-.49.66-.02 1.6.8 1.6h14c.82 0 1.29-.94.8-1.6z',
    cell: 'M7 18c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zM3 7c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm4 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm7 14c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm3-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-3-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm7-2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z',
    dna: 'M4 2h2c0 1.77.78 3.34 2 4.44V5c0-.55.45-1 1-1h6c.55 0 1 .45 1 1v1.44C17.22 5.34 18 3.77 18 2h2c0 2.76-1.44 5.16-3.6 6.54.37.67.6 1.43.6 2.21 0 .78-.23 1.53-.6 2.21C18.56 14.35 20 16.74 20 19.5V22h-2v-2.5c0-1.77-.78-3.34-2-4.44V16c0 .55-.45 1-1 1H9c-.55 0-1-.45-1-1v-1.44C6.78 15.66 6 17.23 6 19v3H4v-3c0-2.76 1.44-5.15 3.6-6.54C7.23 11.78 7 11.03 7 10.25c0-.78.23-1.54.6-2.21C5.44 6.65 4 4.26 4 2zm5 7.25c0 .97.53 1.75 1.5 1.75s1.5-.78 1.5-1.75S11.47 7.5 10.5 7.5 9 8.28 9 9.25zm3 3.5c-.97 0-1.75.53-1.75 1.5s.78 1.5 1.75 1.5 1.75-.53 1.75-1.5-.78-1.5-1.75-1.5z'
};

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
        icon: 'meeting',
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
        icon: 'code',
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
        icon: 'chat',
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
        icon: 'bolt',
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
        wakelock: false,
        darkTheme: true
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
    applyTheme();
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
    grid.innerHTML = state.profiles.map(profile => {
        const iconPath = PROFILE_ICONS[profile.icon] || PROFILE_ICONS.timer;
        return `
        <div class="profile-card" data-id="${profile.id}" style="--profile-color: ${profile.color}">
            <div class="profile-icon" style="background: ${profile.color}33">
                <svg viewBox="0 0 24 24" style="fill: ${profile.color}">
                    <path d="${iconPath}"/>
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
    `}).join('');

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

        // Use "Quick Timer" for quick start sessions (no profileId)
        const profileName = state.timer.profileId ?
            (profile ? profile.name : 'Unknown') :
            'Quick Timer';

        state.sessions.push({
            profileName: profileName,
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

    // Use "Quick Timer" for quick start sessions (no profileId)
    const profileName = state.timer.profileId ?
        (profile ? profile.name : 'Unknown') :
        'Quick Timer';

    state.sessions.push({
        profileName: profileName,
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

    // Icon selection
    const selectedIcon = profile ? profile.icon || 'timer' : 'timer';
    document.querySelectorAll('.icon-btn-select').forEach(btn => {
        btn.classList.toggle('selected', btn.dataset.icon === selectedIcon);
    });

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
    const selectedIcon = document.querySelector('.icon-btn-select.selected');
    const breakEnabled = document.getElementById('break-toggle').checked;

    const profileData = {
        name: name,
        intervalMinutes: parseInt(document.getElementById('interval-slider').value),
        sessionMinutes: parseInt(document.getElementById('session-slider').value),
        breakAfterIntervals: breakEnabled ? parseInt(document.getElementById('break-interval-slider').value) : 0,
        breakDurationMinutes: parseInt(document.getElementById('break-duration-slider').value),
        color: selectedColor ? selectedColor.dataset.color : '#2196F3',
        icon: selectedIcon ? selectedIcon.dataset.icon : 'timer',
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
    document.getElementById('theme-toggle').checked = state.settings.darkTheme;
}

// Theme
function applyTheme() {
    if (state.settings.darkTheme) {
        document.documentElement.removeAttribute('data-theme');
    } else {
        document.documentElement.setAttribute('data-theme', 'light');
    }
}

// Haptic feedback
function hapticFeedback() {
    if (state.settings.vibration && navigator.vibrate) {
        navigator.vibrate(10);
    }
}

// Export data
function exportData() {
    const data = {
        profiles: state.profiles,
        sessions: state.sessions,
        settings: state.settings,
        exportDate: new Date().toISOString(),
        version: '1.0.0'
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `r3d1-backup-${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Import data
function importData(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
        try {
            const data = JSON.parse(e.target.result);

            if (!data.profiles || !Array.isArray(data.profiles)) {
                alert('Invalid backup file format');
                return;
            }

            if (confirm('This will replace all your current data. Continue?')) {
                state.profiles = data.profiles;
                state.sessions = data.sessions || [];
                if (data.settings) {
                    state.settings = { ...state.settings, ...data.settings };
                }

                saveProfiles();
                saveSessions();
                saveSettings();
                applySettings();
                applyTheme();
                renderProfiles();

                alert('Data imported successfully!');
            }
        } catch (err) {
            alert('Error reading backup file: ' + err.message);
        }
    };
    reader.readAsText(file);
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

    // Icon selection
    document.querySelectorAll('.icon-btn-select').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.icon-btn-select').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
        });
    });

    // Color selection
    document.querySelectorAll('.color-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
        });
    });

    // Test buttons
    document.getElementById('test-sound-btn').addEventListener('click', () => {
        playBeep();
    });
    document.getElementById('test-vibration-btn').addEventListener('click', () => {
        if (navigator.vibrate) {
            navigator.vibrate([200, 100, 200]);
        } else {
            alert('Vibration not supported on this device');
        }
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
    document.getElementById('theme-toggle').addEventListener('change', (e) => {
        state.settings.darkTheme = e.target.checked;
        saveSettings();
        applyTheme();
    });

    // Export/Import
    document.getElementById('export-data-btn').addEventListener('click', exportData);
    document.getElementById('import-data-btn').addEventListener('click', () => {
        document.getElementById('import-file-input').click();
    });
    document.getElementById('import-file-input').addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            importData(e.target.files[0]);
            e.target.value = ''; // Reset for future imports
        }
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

    // Quick Start picker controls
    setupQuickStartPickers();
}

// Quick Start Functionality
let quickStartValues = {
    minutes: 5,
    intervals: 4
};

function updateQuickTotalTime() {
    const total = quickStartValues.minutes * quickStartValues.intervals;
    const hours = Math.floor(total / 60);
    const mins = total % 60;

    let display;
    if (hours > 0 && mins > 0) {
        display = `${hours}h ${mins}m`;
    } else if (hours > 0) {
        display = `${hours}h`;
    } else {
        display = `${total} min`;
    }

    document.getElementById('quick-total-time').textContent = display;
}

function setupQuickStartPickers() {
    // Initialize total time display
    updateQuickTotalTime();

    // Picker arrow buttons
    document.querySelectorAll('.picker-arrow').forEach(btn => {
        btn.addEventListener('click', () => {
            const picker = btn.dataset.picker;
            const dir = btn.dataset.dir;

            // Haptic feedback on picker change
            hapticFeedback();

            if (picker === 'minutes') {
                if (dir === 'up') {
                    quickStartValues.minutes = quickStartValues.minutes >= 60 ? 1 : quickStartValues.minutes + 1;
                } else {
                    quickStartValues.minutes = quickStartValues.minutes <= 1 ? 60 : quickStartValues.minutes - 1;
                }
                document.getElementById('quick-minutes').textContent = quickStartValues.minutes;
            } else if (picker === 'intervals') {
                if (dir === 'up') {
                    quickStartValues.intervals = quickStartValues.intervals >= 60 ? 1 : quickStartValues.intervals + 1;
                } else {
                    quickStartValues.intervals = quickStartValues.intervals <= 1 ? 60 : quickStartValues.intervals - 1;
                }
                document.getElementById('quick-intervals').textContent = quickStartValues.intervals;
            }
            updateQuickTotalTime();
        });

        // Long press for fast scrolling
        let intervalId = null;
        btn.addEventListener('mousedown', () => {
            intervalId = setInterval(() => btn.click(), 100);
        });
        btn.addEventListener('mouseup', () => clearInterval(intervalId));
        btn.addEventListener('mouseleave', () => clearInterval(intervalId));
        btn.addEventListener('touchstart', (e) => {
            e.preventDefault();
            intervalId = setInterval(() => btn.click(), 100);
        });
        btn.addEventListener('touchend', () => clearInterval(intervalId));
    });

    // Quick start button
    document.getElementById('quick-start-btn').addEventListener('click', startQuickTimer);
}

function startQuickTimer() {
    const intervalMinutes = quickStartValues.minutes;
    const totalIntervals = quickStartValues.intervals;
    const sessionMinutes = intervalMinutes * totalIntervals;

    state.timer = {
        status: 'idle',
        profileId: null,
        intervalSeconds: intervalMinutes * 60,
        remainingIntervalSeconds: intervalMinutes * 60,
        sessionSeconds: sessionMinutes * 60,
        remainingSessionSeconds: sessionMinutes * 60,
        intervalsCompleted: 0,
        breakRemainingSeconds: 0,
        breakAfterIntervals: 0,
        breakDurationSeconds: 0,
        startTime: null,
        totalIntervals: totalIntervals
    };

    document.getElementById('timer-profile-name').textContent = `Quick ${intervalMinutes}min × ${totalIntervals}`;
    document.getElementById('progress-circle').style.stroke = '#4CAF50';

    updateTimerDisplay();
    showScreen('timer');
    showTimerControls('idle');
}

// Initialize
document.addEventListener('DOMContentLoaded', init);
