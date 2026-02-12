// ===== CHATBOT MODULE =====
const Chatbot = {
    isOpen: false,

    init() {
        // Create FAB
        const fab = document.createElement('button');
        fab.className = 'chatbot-fab';
        fab.innerHTML = '💬';
        fab.title = 'Chat with us';
        fab.addEventListener('click', () => this.toggle());
        document.body.appendChild(fab);

        // Create Window
        const win = document.createElement('div');
        win.className = 'chatbot-window';
        win.id = 'chatbotWindow';
        win.innerHTML = `
            <div class="chatbot-header">
                <div class="bot-avatar">🤖</div>
                <div class="bot-info">
                    <h4>HelpBot</h4>
                    <p>Online • Ready to help</p>
                </div>
            </div>
            <div class="chatbot-messages" id="chatMessages">
                <div class="chat-message bot">👋 Hi there! I'm HelpBot. How can I assist you today? Try asking about creating tickets, tracking status, or priority levels!</div>
            </div>
            <div class="chatbot-input">
                <input type="text" id="chatInput" placeholder="Type a message..." />
                <button id="chatSend">➤</button>
            </div>
        `;
        document.body.appendChild(win);

        // Events
        document.getElementById('chatSend').addEventListener('click', () => this.sendMessage());
        document.getElementById('chatInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    },

    toggle() {
        const win = document.getElementById('chatbotWindow');
        this.isOpen = !this.isOpen;
        win.classList.toggle('open', this.isOpen);
    },

    async sendMessage() {
        const input = document.getElementById('chatInput');
        const message = input.value.trim();
        if (!message) return;

        // Add user message
        this.addMessage(message, 'user');
        input.value = '';

        // Show typing
        this.showTyping();

        try {
            const response = await fetch('/api/chatbot/message', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message })
            });
            const data = await response.json();

            // Remove typing and show response
            setTimeout(() => {
                this.hideTyping();
                this.addMessage(data.response, 'bot');
            }, 800 + Math.random() * 700);
        } catch (error) {
            this.hideTyping();
            this.addMessage('Sorry, I\'m having trouble connecting. Please try again!', 'bot');
        }
    },

    addMessage(text, sender) {
        const container = document.getElementById('chatMessages');
        const msg = document.createElement('div');
        msg.className = `chat-message ${sender}`;
        msg.textContent = text;
        container.appendChild(msg);
        container.scrollTop = container.scrollHeight;
    },

    showTyping() {
        const container = document.getElementById('chatMessages');
        const typing = document.createElement('div');
        typing.className = 'chat-typing';
        typing.id = 'chatTyping';
        typing.innerHTML = '<span></span><span></span><span></span>';
        container.appendChild(typing);
        container.scrollTop = container.scrollHeight;
    },

    hideTyping() {
        const typing = document.getElementById('chatTyping');
        if (typing) typing.remove();
    }
};

// Init chatbot on page load
document.addEventListener('DOMContentLoaded', () => {
    Chatbot.init();
});
