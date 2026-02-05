import React, { useState, useRef, useEffect } from 'react';
import '../styles/AIAssistant.css';

/**
 * AI Assistant Component
 * Provides a chatbot interface for users to get insights about stocks
 * Uses OpenAI's GPT-4o-mini model (most cost-effective option)
 */
const AIAssistant = () => {
    const [messages, setMessages] = useState([
        {
            role: 'assistant',
            content: 'Hello! I\'m your Stock AI Assistant. Ask me anything about stocks, market trends, investment strategies, or specific companies. I\'m here to help you make informed decisions!'
        }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [stockSymbol, setStockSymbol] = useState('');
    const messagesEndRef = useRef(null);
    const inputRef = useRef(null);

    // Auto-scroll to bottom when new messages arrive
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    // Focus input on mount
    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    // Quick action suggestions
    const quickActions = [
        { label: 'What is P/E ratio?', query: 'Explain what P/E ratio means and how to use it for stock analysis' },
        { label: 'Market trends', query: 'What are the current market trends I should be aware of?' },
        { label: 'Risk management', query: 'What are some basic risk management strategies for stock investing?' },
        { label: 'Diversification tips', query: 'How should I diversify my stock portfolio?' }
    ];

    const handleQuickAction = (query) => {
        setInput(query);
        inputRef.current?.focus();
    };

    const handleStockLookup = () => {
        if (stockSymbol.trim()) {
            const query = `Give me a detailed analysis and insights about ${stockSymbol.toUpperCase()} stock. Include key metrics, recent performance, and things to consider.`;
            handleSendMessage(query);
            setStockSymbol('');
        }
    };

    const handleSendMessage = async (messageText = input) => {
        const userMessage = messageText.trim();
        if (!userMessage || isLoading) return;

        // Add user message to chat
        const newUserMessage = { role: 'user', content: userMessage };
        setMessages(prev => [...prev, newUserMessage]);
        setInput('');
        setIsLoading(true);

        try {
            // Call OpenAI API through our backend proxy
            const response = await fetch('http://localhost:8080/api/ai/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    messages: [...messages, newUserMessage].map(m => ({
                        role: m.role,
                        content: m.content
                    }))
                })
            });

            if (!response.ok) {
                throw new Error('Failed to get response from AI');
            }

            const data = await response.json();

            // Add assistant response to chat
            setMessages(prev => [...prev, {
                role: 'assistant',
                content: data.response
            }]);
        } catch (error) {
            console.error('AI Chat Error:', error);
            setMessages(prev => [...prev, {
                role: 'assistant',
                content: 'Sorry, I encountered an error. Please try again later. Make sure the backend server is running.'
            }]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSendMessage();
        }
    };

    const clearChat = () => {
        setMessages([{
            role: 'assistant',
            content: 'Chat cleared! How can I help you with your stock investments today?'
        }]);
    };

    return (
        <div className="ai-assistant-container">
            <div className="ai-header">
                <div className="ai-header-info">
                    <div className="ai-avatar">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M12 8V4H8"/>
                            <rect x="8" y="8" width="8" height="8" rx="2"/>
                            <path d="M16 8V4h4"/>
                            <path d="M12 16v4h4"/>
                            <path d="M8 16v4H4"/>
                        </svg>
                    </div>
                    <div className="ai-title-section">
                        <h2 className="ai-title">Stock AI Assistant</h2>
                        <span className="ai-subtitle">Powered by GPT-4o-mini</span>
                    </div>
                </div>
                <button className="clear-chat-btn" onClick={clearChat} title="Clear chat">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M3 6h18"/>
                        <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
                        <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
                    </svg>
                </button>
            </div>

            {/* Stock Quick Lookup */}
            <div className="stock-lookup-section">
                <div className="stock-lookup-input">
                    <input
                        type="text"
                        placeholder="Enter stock symbol (e.g., AAPL)"
                        value={stockSymbol}
                        onChange={(e) => setStockSymbol(e.target.value.toUpperCase())}
                        onKeyPress={(e) => e.key === 'Enter' && handleStockLookup()}
                    />
                    <button onClick={handleStockLookup} disabled={!stockSymbol.trim()}>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="11" cy="11" r="8"/>
                            <path d="m21 21-4.3-4.3"/>
                        </svg>
                        Analyze
                    </button>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="quick-actions">
                {quickActions.map((action, index) => (
                    <button
                        key={index}
                        className="quick-action-btn"
                        onClick={() => handleQuickAction(action.query)}
                    >
                        {action.label}
                    </button>
                ))}
            </div>

            {/* Chat Messages */}
            <div className="chat-messages">
                {messages.map((message, index) => (
                    <div
                        key={index}
                        className={`message ${message.role === 'user' ? 'user-message' : 'assistant-message'}`}
                    >
                        {message.role === 'assistant' && (
                            <div className="message-avatar">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M12 8V4H8"/>
                                    <rect x="8" y="8" width="8" height="8" rx="2"/>
                                    <path d="M16 8V4h4"/>
                                    <path d="M12 16v4h4"/>
                                    <path d="M8 16v4H4"/>
                                </svg>
                            </div>
                        )}
                        <div className="message-content">
                            {message.content.split('\n').map((line, i) => (
                                <p key={i}>{line || '\u00A0'}</p>
                            ))}
                        </div>
                    </div>
                ))}
                {isLoading && (
                    <div className="message assistant-message">
                        <div className="message-avatar">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <path d="M12 8V4H8"/>
                                <rect x="8" y="8" width="8" height="8" rx="2"/>
                                <path d="M16 8V4h4"/>
                                <path d="M12 16v4h4"/>
                                <path d="M8 16v4H4"/>
                            </svg>
                        </div>
                        <div className="message-content typing-indicator">
                            <span></span>
                            <span></span>
                            <span></span>
                        </div>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className="chat-input-container">
                <textarea
                    ref={inputRef}
                    className="chat-input"
                    placeholder="Ask about stocks, market trends, or investment strategies..."
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    rows={1}
                    disabled={isLoading}
                />
                <button
                    className="send-button"
                    onClick={() => handleSendMessage()}
                    disabled={!input.trim() || isLoading}
                >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="22" y1="2" x2="11" y2="13"/>
                        <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                    </svg>
                </button>
            </div>

            <div className="ai-disclaimer">
                <p>⚠️ AI responses are for informational purposes only and should not be considered financial advice.</p>
            </div>
        </div>
    );
};

export default AIAssistant;
