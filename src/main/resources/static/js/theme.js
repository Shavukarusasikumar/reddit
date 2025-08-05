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
        // Listen for clicks on theme toggle buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('#themeToggle') || e.target.closest('.theme-toggle')) {
                this.toggleTheme();
            }
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

// Listen for theme changes and update components that need it
document.addEventListener('themeChanged', (e) => {
    const theme = e.detail.theme;
    console.log('Theme changed to:', theme);
    
    // Update any components that need theme-specific behavior
    // For example, charts, maps, or other components that need theme awareness
}); 