import React, { useState, useEffect, useRef } from 'react';
import MainLayout from '../components/layout/MainLayout';
import { conversationApi } from '../api/conversationApi';

const AiChatPage = () => {
  const [conversations, setConversations] = useState([]);
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const [newTitle, setNewTitle] = useState('');
  const [showNewModal, setShowNewModal] = useState(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [activeProposal, setActiveProposal] = useState(null);
  const [actionResult, setActionResult] = useState(null);
  const [executingAction, setExecutingAction] = useState(false);

  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, sending, activeProposal, actionResult]);

  useEffect(() => {
    loadConversations();
  }, []);

  const loadConversations = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await conversationApi.getConversations();
      if (res.success && res.data) {
        setConversations(res.data);
        if (res.data.length > 0 && !activeConversation) {
          selectConversation(res.data[0].id);
        }
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load conversations');
    } finally {
      setLoading(false);
    }
  };

  const selectConversation = async (id) => {
    try {
      setError('');
      setActiveProposal(null);
      setActionResult(null);
      const res = await conversationApi.getConversationById(id);
      if (res.success && res.data) {
        setActiveConversation(res.data);
        setMessages(res.data.messages || []);
        setIsSidebarOpen(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load conversation details');
    }
  };

  const handleCreateConversation = async (e) => {
    e.preventDefault();
    if (!newTitle.trim()) return;

    try {
      setError('');
      const res = await conversationApi.createConversation(newTitle.trim());
      if (res.success && res.data) {
        setConversations([res.data, ...conversations]);
        setActiveConversation(res.data);
        setMessages([]);
        setActiveProposal(null);
        setActionResult(null);
        setNewTitle('');
        setShowNewModal(false);
        setIsSidebarOpen(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create conversation');
    }
  };

  const handleDeleteConversation = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this conversation?')) return;

    try {
      setError('');
      await conversationApi.deleteConversation(id);
      const updated = conversations.filter((c) => c.id !== id);
      setConversations(updated);

      if (activeConversation?.id === id) {
        if (updated.length > 0) {
          selectConversation(updated[0].id);
        } else {
          setActiveConversation(null);
          setMessages([]);
          setActiveProposal(null);
          setActionResult(null);
        }
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete conversation');
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!inputMessage.trim() || sending || !activeConversation) return;

    const messageText = inputMessage.trim();
    setInputMessage('');
    setError('');
    setActiveProposal(null);
    setActionResult(null);

    const tempUserMsg = {
      id: 'temp-' + Date.now(),
      role: 'USER',
      content: messageText,
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, tempUserMsg]);
    setSending(true);

    try {
      // 1. Send conversation message
      const res = await conversationApi.sendMessage(activeConversation.id, messageText);
      if (res.success && res.data) {
        setMessages((prev) => [...prev, res.data]);
        const updatedList = await conversationApi.getConversations();
        if (updatedList.success) {
          setConversations(updatedList.data);
        }
      }

      // 2. Check for action intent proposal
      const extractRes = await conversationApi.extractAction(messageText);
      if (extractRes.success && extractRes.data && extractRes.data.actionType) {
        setActiveProposal(extractRes.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send message to AI assistant.');
    } finally {
      setSending(false);
    }
  };

  const handleConfirmAction = async () => {
    if (!activeProposal || executingAction) return;

    try {
      setExecutingAction(true);
      setError('');

      const payload = {
        actionType: activeProposal.actionType,
        clientPayload: activeProposal.clientPayload,
        projectPayload: activeProposal.projectPayload,
        taskPayload: activeProposal.taskPayload,
        projectId: activeProposal.projectId,
        confirmed: true,
      };

      const res = await conversationApi.executeAction(payload);
      if (res.success && res.data) {
        setActionResult(res.data);
        setActiveProposal(null);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to execute proposed AI action');
    } finally {
      setExecutingAction(false);
    }
  };

  const handleCancelAction = () => {
    setActiveProposal(null);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(e);
    }
  };

  const renderFormattedContent = (content) => {
    if (!content) return null;
    const lines = content.split('\n');
    return lines.map((line, index) => {
      const parts = line.split(/(\*\*.*?\*\*)/g);
      const formattedLine = parts.map((part, pIdx) => {
        if (part.startsWith('**') && part.endsWith('**')) {
          return <strong key={pIdx}>{part.slice(2, -2)}</strong>;
        }
        return part;
      });

      return (
        <React.Fragment key={index}>
          {formattedLine}
          {index < lines.length - 1 && <br />}
        </React.Fragment>
      );
    });
  };

  return (
    <MainLayout fluid={true}>
      <div className="chat-container">
        {/* Mobile Backdrop Overlay */}
        {isSidebarOpen && (
          <div className="sidebar-backdrop" onClick={() => setIsSidebarOpen(false)} />
        )}

        {/* Sidebar Drawer */}
        <aside className={`chat-sidebar ${isSidebarOpen ? 'open' : ''}`}>
          <div className="sidebar-header">
            <h3>Conversations</h3>
            <button className="btn btn-primary btn-sm" onClick={() => setShowNewModal(true)}>
              + New Chat
            </button>
          </div>

          {loading && <div className="loading-spinner">Loading chats...</div>}

          <ul className="conversation-list">
            {conversations.map((conv) => (
              <li
                key={conv.id}
                className={`conversation-item ${activeConversation?.id === conv.id ? 'active' : ''}`}
                onClick={() => selectConversation(conv.id)}
              >
                <div className="conv-title">{conv.title}</div>
                <button
                  className="conv-delete-btn"
                  onClick={(e) => handleDeleteConversation(conv.id, e)}
                  title="Delete Conversation"
                >
                  ✕
                </button>
              </li>
            ))}
            {!loading && conversations.length === 0 && (
              <li className="empty-chats">No conversations yet. Create your first chat!</li>
            )}
          </ul>
        </aside>

        {/* Main Chat Content */}
        <main className="chat-main">
          <header className="chat-header">
            <button
              className="mobile-menu-toggle"
              onClick={() => setIsSidebarOpen(!isSidebarOpen)}
              title="Toggle Conversations"
            >
              ☰
            </button>
            <h2>{activeConversation ? activeConversation.title : 'AI Chat'}</h2>
            <button className="mobile-new-chat-btn" onClick={() => setShowNewModal(true)}>
              + New
            </button>
          </header>

          {error && <div className="alert alert-error m-2">{error}</div>}

          {activeConversation ? (
            <>
              <div className="messages-container">
                {messages.length === 0 && (
                  <div className="chat-welcome">
                    <h3>AI Freelance Engineering Assistant</h3>
                    <p>Ask questions or issue commands like <em>"Create client Acme Corp"</em> or <em>"Create project Cloud Redesign"</em>!</p>
                  </div>
                )}

                {messages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`message-bubble ${msg.role === 'USER' ? 'user-message' : 'assistant-message'}`}
                  >
                    <div className="message-header">
                      <span className="sender-name">{msg.role === 'USER' ? 'You' : 'AI Assistant'}</span>
                    </div>
                    <div className="message-content">{renderFormattedContent(msg.content)}</div>
                  </div>
                ))}

                {/* AI Action Proposal Card */}
                {activeProposal && (
                  <div className="action-proposal-card">
                    <div className="action-card-header">
                      <span className="action-badge">PROPOSED ACTION: {activeProposal.actionType}</span>
                    </div>
                    <div className="action-card-body">
                      <p className="action-prompt">{activeProposal.confirmationPrompt}</p>
                      <p className="action-desc"><strong>Details:</strong> {activeProposal.description}</p>
                    </div>
                    <div className="action-card-actions">
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={handleConfirmAction}
                        disabled={executingAction}
                      >
                        {executingAction ? 'Executing...' : '✓ Confirm & Create'}
                      </button>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={handleCancelAction}
                        disabled={executingAction}
                      >
                        ✕ Cancel
                      </button>
                    </div>
                  </div>
                )}

                {/* AI Action Execution Result Badge */}
                {actionResult && (
                  <div className="action-executed-badge">
                    <span>✓ {actionResult.message}</span>
                  </div>
                )}

                {sending && (
                  <div className="message-bubble assistant-message sending-bubble">
                    <span className="sender-name">AI Assistant</span>
                    <div className="typing-indicator">
                      <span></span>
                      <span></span>
                      <span></span>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              <form className="chat-composer" onSubmit={handleSendMessage}>
                <textarea
                  className="composer-input"
                  placeholder="Ask anything or command e.g. 'Create client Acme Corp'... (Press Enter to send)"
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onKeyDown={handleKeyDown}
                  disabled={sending}
                  rows={2}
                />
                <button
                  type="submit"
                  className="btn btn-primary send-btn"
                  disabled={sending || !inputMessage.trim()}
                >
                  {sending ? 'Sending...' : 'Send'}
                </button>
              </form>
            </>
          ) : (
            <div className="no-active-chat">
              Select a conversation from the sidebar or create a new one to begin!
            </div>
          )}
        </main>
      </div>

      {/* New Conversation Modal */}
      {showNewModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Create New Conversation</h3>
              <button className="modal-close" onClick={() => setShowNewModal(false)}>
                ×
              </button>
            </div>
            <form onSubmit={handleCreateConversation}>
              <div className="form-group">
                <label>Conversation Title</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Q3 Client Strategy, Backend Estimation"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  required
                />
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowNewModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" style={{ width: 'auto' }}>
                  Create Chat
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </MainLayout>
  );
};

export default AiChatPage;
