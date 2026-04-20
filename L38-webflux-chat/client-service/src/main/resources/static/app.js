let stompClient = null;
let activeRoomId = null;

const chatLineElementId = "chatLine";
const roomIdElementId = "roomId";
const messageElementId = "message";
const aggregateRoomId = "1408";


const setConnected = (connected) => {
    const connectBtn = document.getElementById("connect");
    const disconnectBtn = document.getElementById("disconnect");
    const sendBtn = document.getElementById("send");

    connectBtn.disabled = connected;
    disconnectBtn.disabled = !connected;
    const chatLine = document.getElementById(chatLineElementId);
    chatLine.hidden = !connected;
    sendBtn.disabled = !connected || isAggregateRoom(getSelectedRoomId());
    updateComposerState();
}

const connect = () => {
    stompClient = Stomp.over(new SockJS('/gs-guide-websocket'));
    stompClient.connect({}, (frame) => {
        setConnected(true);
        const userName = frame.headers["user-name"];
        const roomId = getSelectedRoomId();
        activeRoomId = roomId;
        clearMessages();
        updateComposerState();
        console.log(`Connected to roomId: ${roomId} frame:${frame}`);
        const topicName = `/topic/response.${roomId}`;
        const topicNameUser = `/user/${userName}${topicName}`;
        stompClient.subscribe(topicName, (message) => showMessage(JSON.parse(message.body).messageStr));
        stompClient.subscribe(topicNameUser, (message) => showMessage(JSON.parse(message.body).messageStr));
    });
}

const disconnect = () => {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    stompClient = null;
    activeRoomId = null;
    setConnected(false);
    updateComposerState();
    console.log("Disconnected");
}

const sendMsg = () => {
    const roomId = activeRoomId ?? getSelectedRoomId();
    if (isAggregateRoom(roomId)) {
        return;
    }
    const message = document.getElementById(messageElementId).value;
    stompClient.send(`/app/message.${roomId}`, {}, JSON.stringify({'messageStr': message}))
}

const showMessage = (message) => {
    const chatLine = document.getElementById(chatLineElementId);
    let newRow = chatLine.insertRow(-1);
    let newCell = newRow.insertCell(0);
    let newText = document.createTextNode(message);
    newCell.appendChild(newText);
}

const getSelectedRoomId = () => document.getElementById(roomIdElementId).value;

const isAggregateRoom = (roomId) => roomId === aggregateRoomId;

const updateComposerState = () => {
    const messageInput = document.getElementById(messageElementId);
    const sendBtn = document.getElementById("send");
    const roomId = activeRoomId ?? getSelectedRoomId();
    const readOnly = isAggregateRoom(roomId);

    messageInput.disabled = readOnly;
    messageInput.placeholder = readOnly ? "room 1408 is read-only" : "type a message...";
    sendBtn.disabled = !stompClient || readOnly;
};

const clearMessages = () => {
    document.getElementById(chatLineElementId).replaceChildren();
};

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById(roomIdElementId).addEventListener("input", () => {
        if (stompClient === null) {
            updateComposerState();
        }
    });
    updateComposerState();
});
