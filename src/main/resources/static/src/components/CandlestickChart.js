import React, { useEffect, useRef, useState } from 'react';
import { createChart, CandlestickSeries } from 'lightweight-charts';
import '../styles/CandlestickChart.css';

/**
 * CandlestickChart Component
 * Displays stock price history using candlestick charts like trading platforms.
 * Supports multiple timeframes and dark/light mode.
 */
const CandlestickChart = () => {
    const chartContainerRef = useRef(null);
    const chartRef = useRef(null);
    const candleSeriesRef = useRef(null);

    const [symbol, setSymbol] = useState('NVDA');
    const [searchInput, setSearchInput] = useState('NVDA');
    const [resolution, setResolution] = useState('D');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [lastPrice, setLastPrice] = useState(null);
    const [priceChange, setPriceChange] = useState(null);

    // Finnhub free API only supports D, W, M resolutions
    // Intraday resolutions (1, 5, 15, 60) require paid subscription
    const resolutions = [
        { value: 'D', label: '1D' },
        { value: 'W', label: '1W' },
        { value: 'M', label: '1M' },
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

    // Fetch candle data from backend
    const fetchCandleData = async () => {
        setLoading(true);
        setError(null);

        try {
            // Fetch candle data and current price in parallel
            const [candleResponse] = await Promise.all([
                fetch(`http://localhost:8080/api/candles/${symbol}?resolution=${resolution}`),
            ]);

            if (!candleResponse.ok) {
                const errorData = await candleResponse.json();
                throw new Error(errorData.message || 'Failed to fetch candle data');
            }

            const data = await candleResponse.json();

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

            // Calculate price change based on candle data
            if (candleData.length > 1) {
                const firstPrice = candleData[0].open;
                const lastCandleClose = candleData[candleData.length - 1].close;
                const change = ((lastCandleClose - firstPrice) / firstPrice) * 100;
                setPriceChange(change);
            }

            // Update chart with new data
            if (candleSeriesRef.current) {
                candleSeriesRef.current.setData(candleData);
                chartRef.current.timeScale().fitContent();
            }

            // Fetch current real-time price separately for consistency
            await fetchCurrentPrice();

        } catch (err) {
            setError(err.message);
            setLastPrice(null);
            setPriceChange(null);
        } finally {
            setLoading(false);
        }
    };

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

        return () => {
            window.removeEventListener('resize', handleResize);
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
            </div>
        </div>
    );
};

export default CandlestickChart;
