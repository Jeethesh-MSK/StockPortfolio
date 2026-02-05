import React, { useRef, useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import Dashboard from './components/Dashboard';
import PortfolioPage from './components/PortfolioPage';
import './App.css';

/**
 * Main App Component
 * Serves as the entry point for the Stock Portfolio application
 * Features sidebar navigation with Dashboard and Portfolio pages
 */
function App() {
  // Reference to portfolio list for triggering refresh
  const portfolioListRef = useRef(null);

  // Current active page
  const [activePage, setActivePage] = useState('dashboard');

  // Dark mode state - check localStorage for saved preference
  const [darkMode, setDarkMode] = useState(() => {
    const saved = localStorage.getItem('darkMode');
    // Default to dark mode
    return saved !== null ? JSON.parse(saved) : true;
  });

  // Apply dark mode class to body and save preference
  useEffect(() => {
    document.body.classList.toggle('dark-mode', darkMode);
    localStorage.setItem('darkMode', JSON.stringify(darkMode));
  }, [darkMode]);

  const toggleDarkMode = () => {
    setDarkMode(prev => !prev);
  };

  const handlePurchaseComplete = () => {
    // Trigger portfolio refresh when a stock is purchased
    if (portfolioListRef.current && portfolioListRef.current.refreshPortfolio) {
      portfolioListRef.current.refreshPortfolio();
    }
    // Force re-render by triggering window event
    window.dispatchEvent(new CustomEvent('portfolioUpdated'));
  };

  const handlePageChange = (page) => {
    setActivePage(page);
  };

  // Render the active page
  const renderPage = () => {
    switch (activePage) {
      case 'dashboard':
        return <Dashboard onPurchaseComplete={handlePurchaseComplete} />;
      case 'portfolio':
        return <PortfolioPage ref={portfolioListRef} />;
      default:
        return <Dashboard onPurchaseComplete={handlePurchaseComplete} />;
    }
  };

  // SVG Icons for theme toggle
  const SunIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="4"/>
      <path d="M12 2v2"/>
      <path d="M12 20v2"/>
      <path d="m4.93 4.93 1.41 1.41"/>
      <path d="m17.66 17.66 1.41 1.41"/>
      <path d="M2 12h2"/>
      <path d="M20 12h2"/>
      <path d="m6.34 17.66-1.41 1.41"/>
      <path d="m19.07 4.93-1.41 1.41"/>
    </svg>
  );

  const MoonIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>
    </svg>
  );

  return (
    <div className={`app-layout ${darkMode ? 'dark-mode' : ''}`}>
      {/* Sidebar Navigation */}
      <Sidebar activePage={activePage} onPageChange={handlePageChange} />

      {/* Main Content Area */}
      <div className="main-content">
        {/* Top Header Bar */}
        <header className="top-header">
          <div className="header-left">
            <h2 className="page-title">
              {activePage === 'dashboard' ? 'Dashboard' : 'Portfolio'}
            </h2>
          </div>
          <div className="header-right">
            <button
              className="theme-toggle"
              onClick={toggleDarkMode}
              title={darkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
            >
              <span className="theme-icon">
                {darkMode ? <SunIcon /> : <MoonIcon />}
              </span>
              <span className="theme-label">{darkMode ? 'Light' : 'Dark'}</span>
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="page-content">
          {renderPage()}
        </main>

        {/* Footer */}
        <footer className="app-footer">
          <p>Stock Portfolio Manager v1.0.0 | Powered by Spring Boot & React</p>
        </footer>
      </div>
    </div>
  );
}

export default App;
