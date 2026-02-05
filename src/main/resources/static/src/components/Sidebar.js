import React from 'react';
import '../styles/Sidebar.css';

/**
 * Sidebar Navigation Component
 * Provides navigation between Dashboard and Portfolio pages
 */
const Sidebar = ({ activePage, onPageChange }) => {
    const menuItems = [
        { id: 'dashboard', label: 'Dashboard', icon: 'chart', description: 'Charts & Stock Lookup' },
        { id: 'portfolio', label: 'Portfolio', icon: 'briefcase', description: 'Your Holdings' },
    ];

    // SVG Icons for clean professional look
    const icons = {
        chart: (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 3v18h18"/>
                <path d="M18 17V9"/>
                <path d="M13 17V5"/>
                <path d="M8 17v-3"/>
            </svg>
        ),
        briefcase: (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
            </svg>
        ),
        trending: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
                <polyline points="17 6 23 6 23 12"/>
            </svg>
        )
    };

    return (
        <nav className="sidebar-nav">
            <div className="sidebar-brand">
                <span className="brand-icon">{icons.trending}</span>
                <div className="brand-text">
                    <span className="brand-name">StockPortfolio</span>
                    <span className="brand-tagline">Investment Manager</span>
                </div>
            </div>

            <div className="sidebar-menu">
                {menuItems.map((item) => (
                    <button
                        key={item.id}
                        className={`sidebar-item ${activePage === item.id ? 'active' : ''}`}
                        onClick={() => onPageChange(item.id)}
                    >
                        <span className="sidebar-icon">{icons[item.icon]}</span>
                        <div className="sidebar-item-text">
                            <span className="sidebar-label">{item.label}</span>
                            <span className="sidebar-desc">{item.description}</span>
                        </div>
                    </button>
                ))}
            </div>

            <div className="sidebar-footer">
                <div className="sidebar-info">
                    <span className="info-text">Data by Twelve Data & Finnhub</span>
                </div>
            </div>
        </nav>
    );
};

export default Sidebar;
