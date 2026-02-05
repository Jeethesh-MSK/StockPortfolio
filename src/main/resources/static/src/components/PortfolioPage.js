import React, { forwardRef } from 'react';
import PortfolioList from './PortfolioList';
import '../styles/PortfolioPage.css';

/**
 * Portfolio Page Component
 * Shows only the user's portfolio holdings
 */
const PortfolioPage = forwardRef((props, ref) => {
    return (
        <div className="portfolio-page">

            <div className="portfolio-content">
                <PortfolioList ref={ref} />
            </div>
        </div>
    );
});

PortfolioPage.displayName = 'PortfolioPage';

export default PortfolioPage;
