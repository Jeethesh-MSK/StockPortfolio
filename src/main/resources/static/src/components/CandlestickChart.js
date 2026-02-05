import React, { useEffect, useRef, useState, useCallback } from 'react';
import { createChart, CandlestickSeries } from 'lightweight-charts';
import '../styles/CandlestickChart.css';

/**
 * CandlestickChart Component
 * Displays stock price history using candlestick charts like trading platforms.
 * Supports multiple timeframes and dark/light mode.
 * Data powered by Twelve Data API with auto-refresh every 5 minutes.
 */
const CandlestickChart = () => {
    const chartContainerRef = useRef(null);
    const chartRef = useRef(null);
    const candleSeriesRef = useRef(null);
    const refreshIntervalRef = useRef(null);

    const [symbol, setSymbol] = useState('NVDA');
    const [searchInput, setSearchInput] = useState('NVDA');
    const [resolution, setResolution] = useState('1M');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [lastPrice, setLastPrice] = useState(null);
    const [priceChange, setPriceChange] = useState(null);
    const [lastUpdated, setLastUpdated] = useState(null);

    // Auto-refresh interval in milliseconds (5 minutes to respect rate limits)
    const AUTO_REFRESH_INTERVAL = 5 * 60 * 1000;

    // Supported time ranges (Google Finance style)
    // 1D = Today's hourly data, 1W = Past week daily, 1M = Past month daily, 1Y = Past year weekly, MAX = All time monthly
    const resolutions = [
        { value: '1D', label: '1D' },
        { value: '1W', label: '1W' },
        { value: '1M', label: '1M' },
    ];

    // Fetch current real-time price from quote API
    const fetchCurrentPrice = async () => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/stocks/price/${symbol}`
            );
            if (response.ok) {
                const data = await response.json();
                if (data.price) {
                    setLastPrice(data.price);
                }
            }
        } catch (err) {
            console.warn('Failed to fetch current price:', err.message);
        }
    };

    // Fetch candle data from backend (Twelve Data API)
    const fetchCandleData = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(
                `http://localhost:8080/api/candles/${symbol}?resolution=${resolution}`
            );

            const data = await response.json();

            // Handle error responses
            if (!response.ok) {
                // Check for specific error types
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
            const candleData = data.t.map((time, index) => ({
                time: time,
                open: data.o[index],
                high: data.h[index],
                low: data.l[index],
                close: data.c[index],
            }));

            // Calculate price change based on the selected timeframe
            // For accurate period change: compare current close to the close from one period ago
            if (candleData.length > 1) {
                // Get the previous period's close price (second-to-last candle)
                const previousClose = candleData[candleData.length - 2].close;
                const currentClose = candleData[candleData.length - 1].close;
                const change = ((currentClose - previousClose) / previousClose) * 100;
                setPriceChange(change);
            }

            // Update chart with new data
            if (candleSeriesRef.current) {
                candleSeriesRef.current.setData(candleData);
                chartRef.current.timeScale().fitContent();
            }

            // Update last updated timestamp
            setLastUpdated(new Date());

            // Fetch current real-time price separately for consistency
            await fetchCurrentPrice();

        } catch (err) {
            setError(err.message);
            setLastPrice(null);
            setPriceChange(null);
        } finally {
            setLoading(false);
        }
    }, [symbol, resolution]);

    // Initialize chart
    useEffect(() => {
        if (!chartContainerRef.current) return;

        const isDark = document.body.classList.contains('dark-mode');

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: 450,
            layout: {
                background: { type: 'solid', color: isDark ? '#161b22' : '#ffffff' },
                textColor: isDark ? '#8b949e' : '#6b778c',
            },
            grid: {
                vertLines: { color: isDark ? '#30363d' : '#e1e4e8' },
                horzLines: { color: isDark ? '#30363d' : '#e1e4e8' },
            },
            crosshair: {
                mode: 1,
                vertLine: {
                    color: isDark ? '#58a6ff' : '#0052cc',
                    width: 1,
                    style: 2,
                    labelBackgroundColor: isDark ? '#58a6ff' : '#0052cc',
                },
                horzLine: {
                    color: isDark ? '#58a6ff' : '#0052cc',
                    width: 1,
                    style: 2,
                    labelBackgroundColor: isDark ? '#58a6ff' : '#0052cc',
                },
            },
            rightPriceScale: {
                borderColor: isDark ? '#30363d' : '#e1e4e8',
                scaleMargins: {
                    top: 0.1,
                    bottom: 0.1,
                },
            },
            timeScale: {
                borderColor: isDark ? '#30363d' : '#e1e4e8',
                timeVisible: true,
                secondsVisible: false,
            },
            handleScroll: {
                mouseWheel: true,
                pressedMouseMove: true,
            },
            handleScale: {
                axisPressedMouseMove: true,
                mouseWheel: true,
                pinch: true,
            },
        });

        const candlestickSeries = chart.addSeries(CandlestickSeries, {
            upColor: '#3fb950',
            downColor: '#f85149',
            borderDownColor: '#f85149',
            borderUpColor: '#3fb950',
            wickDownColor: '#f85149',
            wickUpColor: '#3fb950',
        });

        chartRef.current = chart;
        candleSeriesRef.current = candlestickSeries;

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
        fetchCandleData();

        // Set up auto-refresh interval (every 5 minutes)
        refreshIntervalRef.current = setInterval(() => {
            console.log('Auto-refreshing chart data...');
            fetchCandleData();
        }, AUTO_REFRESH_INTERVAL);

        return () => {
            window.removeEventListener('resize', handleResize);
            // Clear auto-refresh interval
            if (refreshIntervalRef.current) {
                clearInterval(refreshIntervalRef.current);
            }
            if (chartRef.current) {
                chartRef.current.remove();
                chartRef.current = null;
            }
        };
    }, []);

    // Update chart theme when dark mode changes
    useEffect(() => {
        if (!chartRef.current) return;

        const isDark = document.body.classList.contains('dark-mode');

        chartRef.current.applyOptions({
            layout: {
                background: { type: 'solid', color: isDark ? '#161b22' : '#ffffff' },
                textColor: isDark ? '#8b949e' : '#6b778c',
            },
            grid: {
                vertLines: { color: isDark ? '#30363d' : '#e1e4e8' },
                horzLines: { color: isDark ? '#30363d' : '#e1e4e8' },
            },
            rightPriceScale: {
                borderColor: isDark ? '#30363d' : '#e1e4e8',
            },
            timeScale: {
                borderColor: isDark ? '#30363d' : '#e1e4e8',
            },
        });
    });

    // Fetch data when symbol or resolution changes
    useEffect(() => {
        if (chartRef.current && candleSeriesRef.current) {
            fetchCandleData();
        }
    }, [symbol, resolution]);

    // Handle search form submit
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

    // Handle manual refresh
    const handleRefresh = () => {
        fetchCandleData();
    };

    // Format last updated time
    const formatLastUpdated = () => {
        if (!lastUpdated) return null;
        return lastUpdated.toLocaleTimeString();
    };

    return (
        <div className="candlestick-chart-container">
            <div className="chart-header">
                <div className="chart-title-section">
                    <h2>📊 Stock Chart</h2>
                    <div className="symbol-info">
                        <span className="current-symbol">{symbol}</span>
                        {lastPrice && (
                            <span className="current-price">${lastPrice.toFixed(2)}</span>
                        )}
                        {priceChange !== null && (
                            <span className={`price-change ${priceChange >= 0 ? 'positive' : 'negative'}`}>
                                {priceChange >= 0 ? '+' : ''}{priceChange.toFixed(2)}%
                            </span>
                        )}
                    </div>
                    {lastUpdated && (
                        <div className="last-updated">
                            <span>Updated: {formatLastUpdated()}</span>
                            <button
                                onClick={handleRefresh}
                                className="refresh-btn"
                                disabled={loading}
                                title="Refresh chart data"
                            >
                                🔄
                            </button>
                        </div>
                    )}
                </div>

                <form onSubmit={handleSearch} className="chart-search-form">
                    <input
                        type="text"
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value.toUpperCase())}
                        placeholder="Symbol..."
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
                        >
                            {res.label}
                        </button>
                    ))}
                </div>
                <div className="chart-info">
                    <span className="api-badge" title="Data refreshes every 5 minutes">
                        ⏱️ Auto-refresh: 5min
                    </span>
                </div>
            </div>

            {error && (
                <div className="chart-error">
                    <span>⚠️ {error}</span>
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
                <span className="legend-item">
                    <span className="legend-color up"></span> Bullish (Price Up)
                </span>
                <span className="legend-item">
                    <span className="legend-color down"></span> Bearish (Price Down)
                </span>
                <span className="data-source">
                    Data by Twelve Data
                </span>
            </div>
        </div>
    );
};

export default CandlestickChart;
