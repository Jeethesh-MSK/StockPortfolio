import React, { useEffect, useRef, useState, useCallback } from 'react';
import { createChart, AreaSeries, CandlestickSeries } from 'lightweight-charts';
import '../styles/CandlestickChart.css';

/**
 * StockChart Component
 * Displays stock price history using Area charts (Google Finance style) or Candlestick charts.
 *
 * Time Range Behavior (matches Google Finance):
 * - 1D: Today's trading data (5-minute intervals)
 * - 1W: Past 5 trading days (30-minute intervals)
 * - 1M: Past ~22 trading days (daily data)
 * - 1Y: Past year (~252 trading days, daily data)
 * - 5Y: Past 5 years (weekly data)
 * - Max: All available data (monthly data)
 *
 * Percentage Change Calculation:
 * Compares FIRST data point's open price to the CURRENT/LAST close price.
 * This shows the total change over the selected time period.
 */
const StockChart = () => {
    const chartContainerRef = useRef(null);
    const chartRef = useRef(null);
    const seriesRef = useRef(null);
    const refreshIntervalRef = useRef(null);

    const [symbol, setSymbol] = useState('NVDA');
    const [searchInput, setSearchInput] = useState('NVDA');
    const [resolution, setResolution] = useState('1M');
    const [chartType, setChartType] = useState('area'); // 'area' or 'candle'
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [lastPrice, setLastPrice] = useState(null);
    const [priceChange, setPriceChange] = useState(null);
    const [priceChangeDollar, setPriceChangeDollar] = useState(null);
    const [lastUpdated, setLastUpdated] = useState(null);
    const [periodStartPrice, setPeriodStartPrice] = useState(null);

    // Auto-refresh interval (5 minutes to respect rate limits)
    const AUTO_REFRESH_INTERVAL = 5 * 60 * 1000;

    // Time range options (Google Finance style)
    const resolutions = [
        { value: '1D', label: '1D', description: 'Today' },
        { value: '1W', label: '1W', description: 'Past Week' },
        { value: '1M', label: '1M', description: 'Past Month' },
        { value: '1Y', label: '1Y', description: 'Past Year' },
        { value: '5Y', label: '5Y', description: '5 Years' },
        { value: 'MAX', label: 'Max', description: 'All Time' },
    ];

    // Get resolution label for display
    const getResolutionLabel = () => {
        const res = resolutions.find(r => r.value === resolution);
        return res ? res.description : 'Past Month';
    };

    // Fetch candle data from backend
    const fetchChartData = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(
                `http://localhost:8080/api/candles/${symbol}?resolution=${resolution}`
            );

            const data = await response.json();

            // Handle error responses
            if (!response.ok) {
                if (response.status === 429) {
                    throw new Error('Rate limit exceeded. Please wait a moment before trying again.');
                } else if (response.status === 503) {
                    throw new Error('Chart service unavailable. Please check API configuration.');
                } else if (data.message) {
                    throw new Error(data.message);
                } else {
                    throw new Error('Failed to fetch chart data');
                }
            }

            // Check for API error in response body
            if (data.s === 'error') {
                throw new Error(data.message || 'Failed to fetch chart data');
            }

            // Validate data structure
            if (!data.c || data.c.length === 0) {
                throw new Error('No data available for this symbol');
            }

            // Transform data for lightweight-charts
            const chartData = data.t.map((time, index) => ({
                time: time,
                open: parseFloat(data.o[index]),
                high: parseFloat(data.h[index]),
                low: parseFloat(data.l[index]),
                close: parseFloat(data.c[index]),
                value: parseFloat(data.c[index]), // For area chart
            }));

            // Sort by time ascending (oldest first)
            chartData.sort((a, b) => a.time - b.time);

            // ============================================
            // CORRECT PERCENTAGE CALCULATION (Google Finance style)
            // ============================================
            // Compare: First data point's OPEN price (start of period)
            // To: Last data point's CLOSE price (current/end of period)
            //
            // Example for 1M view with 22 trading days:
            // - firstPrice = price at the START of the month
            // - currentPrice = price NOW
            // - change = ((currentPrice - firstPrice) / firstPrice) * 100
            // ============================================
            if (chartData.length > 0) {
                const firstPrice = chartData[0].open;  // Period start price
                const currentPrice = chartData[chartData.length - 1].close;  // Current price

                const percentChange = ((currentPrice - firstPrice) / firstPrice) * 100;
                const dollarChange = currentPrice - firstPrice;

                setPriceChange(percentChange);
                setPriceChangeDollar(dollarChange);
                setLastPrice(currentPrice);
                setPeriodStartPrice(firstPrice);
            }

            // Update chart with new data
            if (seriesRef.current && chartRef.current) {
                if (chartType === 'area') {
                    // Area chart uses 'value' (close price)
                    seriesRef.current.setData(chartData.map(d => ({
                        time: d.time,
                        value: d.close
                    })));
                } else {
                    // Candlestick chart uses OHLC
                    seriesRef.current.setData(chartData);
                }
                chartRef.current.timeScale().fitContent();
            }

            // Update last updated timestamp
            setLastUpdated(new Date());

        } catch (err) {
            setError(err.message);
            setLastPrice(null);
            setPriceChange(null);
            setPriceChangeDollar(null);
        } finally {
            setLoading(false);
        }
    }, [symbol, resolution, chartType]);

    // Initialize chart
    useEffect(() => {
        if (!chartContainerRef.current) return;

        // Clean up existing chart
        if (chartRef.current) {
            chartRef.current.remove();
            chartRef.current = null;
            seriesRef.current = null;
        }

        const isDark = true; // Force dark mode for consistency

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: 400,
            layout: {
                background: { type: 'solid', color: '#161b22' },
                textColor: '#8b949e',
            },
            grid: {
                vertLines: { color: '#30363d', style: 1 },
                horzLines: { color: '#30363d', style: 1 },
            },
            crosshair: {
                mode: 1,
                vertLine: {
                    color: '#58a6ff',
                    width: 1,
                    style: 2,
                    labelBackgroundColor: '#58a6ff',
                },
                horzLine: {
                    color: '#58a6ff',
                    width: 1,
                    style: 2,
                    labelBackgroundColor: '#58a6ff',
                },
            },
            rightPriceScale: {
                borderColor: '#30363d',
                scaleMargins: { top: 0.1, bottom: 0.1 },
            },
            timeScale: {
                borderColor: '#30363d',
                timeVisible: true,
                secondsVisible: false,
            },
            handleScroll: { mouseWheel: true, pressedMouseMove: true },
            handleScale: { axisPressedMouseMove: true, mouseWheel: true, pinch: true },
        });

        // Create appropriate series based on chart type
        let series;
        if (chartType === 'area') {
            series = chart.addSeries(AreaSeries, {
                topColor: 'rgba(38, 166, 154, 0.56)',
                bottomColor: 'rgba(38, 166, 154, 0.04)',
                lineColor: 'rgba(38, 166, 154, 1)',
                lineWidth: 2,
            });
        } else {
            series = chart.addSeries(CandlestickSeries, {
                upColor: '#26a69a',
                downColor: '#ef5350',
                borderDownColor: '#ef5350',
                borderUpColor: '#26a69a',
                wickDownColor: '#ef5350',
                wickUpColor: '#26a69a',
            });
        }

        chartRef.current = chart;
        seriesRef.current = series;

        // Handle resize
        const handleResize = () => {
            if (chartContainerRef.current && chartRef.current) {
                chartRef.current.applyOptions({
                    width: chartContainerRef.current.clientWidth,
                });
            }
        };

        window.addEventListener('resize', handleResize);

        // Fetch initial data
        fetchChartData();

        // Auto-refresh every 5 minutes
        refreshIntervalRef.current = setInterval(() => {
            console.log('Auto-refreshing chart data...');
            fetchChartData();
        }, AUTO_REFRESH_INTERVAL);

        return () => {
            window.removeEventListener('resize', handleResize);
            if (refreshIntervalRef.current) {
                clearInterval(refreshIntervalRef.current);
            }
            if (chartRef.current) {
                chartRef.current.remove();
                chartRef.current = null;
            }
        };
    }, [chartType]); // Recreate chart when type changes

    // Fetch data when symbol or resolution changes
    useEffect(() => {
        if (chartRef.current && seriesRef.current) {
            fetchChartData();
        }
    }, [symbol, resolution, fetchChartData]);

    // Update series colors based on price change
    useEffect(() => {
        if (!seriesRef.current || chartType !== 'area') return;

        const isPositive = priceChange !== null && priceChange >= 0;

        seriesRef.current.applyOptions({
            topColor: isPositive ? 'rgba(38, 166, 154, 0.56)' : 'rgba(239, 83, 80, 0.56)',
            bottomColor: isPositive ? 'rgba(38, 166, 154, 0.04)' : 'rgba(239, 83, 80, 0.04)',
            lineColor: isPositive ? 'rgba(38, 166, 154, 1)' : 'rgba(239, 83, 80, 1)',
        });
    }, [priceChange, chartType]);

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchInput.trim()) {
            setSymbol(searchInput.toUpperCase().trim());
        }
    };

    // Handle resolution change
    const handleResolutionChange = (newResolution) => {
        setResolution(newResolution);
    };

    // Handle chart type toggle
    const handleChartTypeChange = (type) => {
        setChartType(type);
    };

    // Format time
    const formatLastUpdated = () => {
        if (!lastUpdated) return null;
        return lastUpdated.toLocaleTimeString();
    };

    // SVG Icons for clean professional look
    const RefreshIcon = () => (
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
            <path d="M3 3v5h5"/>
            <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"/>
            <path d="M16 21h5v-5"/>
        </svg>
    );

    const AreaChartIcon = () => (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3v18h18"/>
            <path d="M7 16l4-4 4 4 5-5"/>
        </svg>
    );

    const CandleIcon = () => (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9 5v14"/>
            <rect x="5" y="8" width="8" height="8" rx="1"/>
            <path d="M15 5v14"/>
            <rect x="15" y="10" width="4" height="6" rx="1"/>
        </svg>
    );

    const AlertIcon = () => (
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
    );

    return (
        <div className="candlestick-chart-container">
            <div className="chart-header">
                <div className="chart-title-section">
                    <h2>Stock Chart</h2>
                    <div className="symbol-info">
                        <span className="current-symbol">{symbol}</span>
                        {lastPrice !== null && (
                            <span className="current-price">${lastPrice.toFixed(2)}</span>
                        )}
                        {priceChange !== null && priceChangeDollar !== null && (
                            <span className={`price-change ${priceChange >= 0 ? 'positive' : 'negative'}`}>
                                <span className="change-arrow">{priceChange >= 0 ? '↑' : '↓'}</span>
                                ${Math.abs(priceChangeDollar).toFixed(2)} ({priceChange >= 0 ? '+' : ''}{priceChange.toFixed(2)}%) {getResolutionLabel()}
                            </span>
                        )}
                    </div>
                    {lastUpdated && (
                        <div className="last-updated">
                            <span>Updated: {formatLastUpdated()}</span>
                            <button
                                onClick={fetchChartData}
                                className="refresh-btn"
                                disabled={loading}
                                title="Refresh chart data"
                            >
                                <RefreshIcon />
                            </button>
                        </div>
                    )}
                </div>

                <form onSubmit={handleSearch} className="chart-search-form">
                    <input
                        type="text"
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value.toUpperCase())}
                        placeholder="NVDA"
                        className="chart-search-input"
                        maxLength={10}
                    />
                    <button type="submit" className="chart-search-btn" disabled={loading}>
                        Search
                    </button>
                </form>
            </div>

            <div className="chart-controls">
                <div className="resolution-buttons">
                    {resolutions.map((res) => (
                        <button
                            key={res.value}
                            onClick={() => handleResolutionChange(res.value)}
                            className={`resolution-btn ${resolution === res.value ? 'active' : ''}`}
                            disabled={loading}
                            title={res.description}
                        >
                            {res.label}
                        </button>
                    ))}
                </div>
                <div className="chart-type-toggle">
                    <button
                        onClick={() => handleChartTypeChange('area')}
                        className={`chart-type-btn ${chartType === 'area' ? 'active' : ''}`}
                        disabled={loading}
                        title="Area Chart"
                    >
                        <AreaChartIcon /> Area
                    </button>
                    <button
                        onClick={() => handleChartTypeChange('candle')}
                        className={`chart-type-btn ${chartType === 'candle' ? 'active' : ''}`}
                        disabled={loading}
                        title="Candlestick Chart"
                    >
                        <CandleIcon /> Candle
                    </button>
                </div>
            </div>

            {error && (
                <div className="chart-error">
                    <AlertIcon />
                    <span>{error}</span>
                </div>
            )}

            {loading && (
                <div className="chart-loading">
                    <div className="chart-spinner"></div>
                    <span>Loading chart data...</span>
                </div>
            )}

            <div
                ref={chartContainerRef}
                className="chart-area"
                style={{ opacity: loading ? 0.5 : 1 }}
            />

            <div className="chart-legend">
                {periodStartPrice !== null && lastPrice !== null && (
                    <span className="legend-item period-info">
                        {getResolutionLabel()}: ${periodStartPrice.toFixed(2)} → ${lastPrice.toFixed(2)}
                    </span>
                )}
                <span className="data-source">
                    Data by Twelve Data
                </span>
            </div>
        </div>
    );
};

export default StockChart;
