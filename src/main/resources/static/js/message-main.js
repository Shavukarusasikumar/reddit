'use strict';

const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const chatArea = document.querySelector('#chat-messages');
const userList = document.querySelectorAll('.user-item');

let stompClient = null;
let username = document.body.getAttribute('data-username');
let selectedUser = null;

function connectToSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        stompClient.subscribe(`/user/${username}/queue/messages`, onMessageReceived);

        if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
            Notification.requestPermission().then(permission => {
                if (permission === 'granted') {
                    console.log('✅ Notification permission granted.');
                } else {
                    console.warn('🚫 Notification permission denied.');
                }
            });
        } else {
            console.log(`🔔 Notification permission status: ${Notification.permission}`);
        }
    }, () => console.error('WebSocket connection failed'));
}

userList.forEach(userItem => {
    userItem.addEventListener('click', () => {
        userList.forEach(el => el.classList.remove('active'));
        userItem.classList.add('active');

        selectedUser = userItem.getAttribute('data-username');
        messageForm.classList.remove('hidden');
        chatArea.innerHTML = '';

        // Fetch old messages from the server
        fetch(`/messages/${username}/${selectedUser}`)
            .then(response => response.json())
            .then(messages => {
                messages.forEach(msg => {
                    displayMessage(msg.senderId, msg.content);
                });
                chatArea.scrollTop = chatArea.scrollHeight;
            })
            .catch(err => console.error("Error fetching messages:", err));
    });
});


function sendMessage(event) {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient && selectedUser) {
        const chatMessage = {
            senderId: username,
            recipientId: selectedUser,
            content: messageContent,
            timestamp: new Date()
        };
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        displayMessage(username, messageContent);
        messageInput.value = '';
    }
    chatArea.scrollTop = chatArea.scrollHeight;
    event.preventDefault();
}

function displayMessage(senderId, content) {
    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message', senderId === username ? 'sender' : 'receiver');

    const message = document.createElement('p');
    message.textContent = content;

    messageContainer.appendChild(message);
    chatArea.appendChild(messageContainer);
}


function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);

    if (selectedUser === message.senderId) {
        displayMessage(message.senderId, message.content);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
    else if (message.senderId !== username) {
        if (Notification.permission === 'granted') {
            new Notification(`New message from ${message.senderId}`, {
                body: message.content
            });
        }
        console.log(`📨 New message notification from ${message.senderId}: ${message.content}`);
    }
}


messageForm.addEventListener('submit', sendMessage, true);
connectToSocket();
