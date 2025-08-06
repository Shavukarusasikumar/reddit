// Global Theme Management
class ThemeManager {
    constructor() {
        this.currentTheme = localStorage.getItem('theme') || 'light';
        this.init();
    }

    init() {
        // Apply theme on page load
        this.applyTheme(this.currentTheme);
        
        // Update all theme toggle buttons
        this.updateThemeToggleButtons();
        
        // Add event listeners for theme toggle buttons
        this.addThemeToggleListeners();
    }

    applyTheme(theme) {
        // Apply theme to both html and body elements for better coverage
        document.documentElement.setAttribute('data-theme', theme);
        document.body.setAttribute('data-theme', theme);
        this.currentTheme = theme;
        localStorage.setItem('theme', theme);
        
        // Update all theme toggle buttons on the page
        this.updateThemeToggleButtons();
        
        // Dispatch custom event for other components
        document.dispatchEvent(new CustomEvent('themeChanged', { 
            detail: { theme: theme } 
        }));
    }

    toggleTheme() {
        const newTheme = this.currentTheme === 'dark' ? 'light' : 'dark';
        this.applyTheme(newTheme);
    }

    updateThemeToggleButtons() {
        const toggleButtons = document.querySelectorAll('#themeToggle, .theme-toggle');
        
        toggleButtons.forEach(button => {
            if (this.currentTheme === 'dark') {
                button.innerHTML = '☀️ <span>Light</span>';
            } else {
                button.innerHTML = '🌙 <span>Dark</span>';
            }
        });
    }

    addThemeToggleListeners() {
        // Listen for clicks on theme toggle buttons using event delegation
        document.addEventListener('click', (e) => {
            const button = e.target.closest('#themeToggle') || e.target.closest('.theme-toggle');
            if (button) {
                e.preventDefault();
                e.stopPropagation();
                this.toggleTheme();
            }
        });
        
        // Also add direct event listeners to existing theme toggle buttons
        const existingButtons = document.querySelectorAll('#themeToggle, .theme-toggle');
        existingButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                this.toggleTheme();
            });
        });
    }
}

// Initialize theme manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.themeManager = new ThemeManager();
});

// Global function for theme toggle (for backward compatibility)
function toggleTheme() {
    if (window.themeManager) {
        window.themeManager.toggleTheme();
    }
}

// Emergency fallback - ensure theme manager exists
if (!window.themeManager) {
    window.themeManager = new ThemeManager();
}

// Listen for theme changes and update components that need it
document.addEventListener('themeChanged', (e) => {
    const theme = e.detail.theme;
    // Update any components that need theme-specific behavior
    // For example, charts, maps, or other components that need theme awareness
}); 