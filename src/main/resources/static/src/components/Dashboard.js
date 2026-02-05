import React from 'react';
import StockChart from './StockChart';
import StockPriceFetcher from './StockPriceFetcher';
import '../styles/Dashboard.css';

/**
 * Dashboard Page Component
 * Shows the stock chart and stock price lookup functionality
 */
const Dashboard = ({ onPurchaseComplete }) => {
    return (
        <div className="dashboard-page">
            <div className="dashboard-content">
                <div className="dashboard-main">
                    <StockChart />
                </div>

                <div className="dashboard-sidebar">
                    <StockPriceFetcher onPurchaseComplete={onPurchaseComplete} />

                    <div className="market-info-card">
                        <h3>Market Info</h3>
                        <div className="market-status">
                            <span className="status-dot"></span>
                            <span>Market Hours: 9:30 AM - 4:00 PM EST</span>
                        </div>
                        <p className="market-note">
                            Stock prices update in real-time during market hours.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
