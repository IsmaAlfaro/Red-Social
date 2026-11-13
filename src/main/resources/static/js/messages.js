var currentProfileId = null;
var selectedProfileId = null;

function csrfToken() {
    var item = document.cookie.split("; ").find(function (cookie) { return cookie.indexOf("XSRF-TOKEN=") === 0; });
    return item ? decodeURIComponent(item.split("=")[1]) : "";
}

async function request(path, options) {
    var response = await fetch(path, Object.assign({ credentials: "same-origin" }, options));
    if (response.status === 401) { window.location.assign("/access"); throw new Error("Sesion requerida"); }
    if (!response.ok) { throw new Error("No se pudo cargar la conversacion"); }
    return response.status === 204 ? null : response.json();
}

function renderMessages(messages) {
    var container = document.getElementById("conversation-messages");
    container.textContent = "";
    messages.forEach(function (message) {
        var bubble = document.createElement("p");
        bubble.className = message.senderId === currentProfileId ? "message-own" : "message-other";
        bubble.textContent = message.content;
        container.appendChild(bubble);
    });
    container.scrollTop = container.scrollHeight;
}

async function selectMatch(profile) {
    selectedProfileId = profile.id;
    var title = document.getElementById("conversation-title");
    title.textContent = "";
    var name = document.createElement("strong");
    name.textContent = profile.name;
    var link = document.createElement("a");
    link.className = "conversation-profile-link";
    link.href = "/profile-view?id=" + profile.id;
    link.textContent = "Ver perfil";
    title.appendChild(name);
    title.appendChild(link);
    document.getElementById("message-form").classList.remove("hidden");
    refreshConversation();
}

async function refreshConversation() {
    if (!selectedProfileId) { return; }
    try {
        renderMessages(await request("/api/messages/" + selectedProfileId));
    } catch (error) {
        // A temporary request failure should not interrupt the open chat.
    }
}

async function loadPage() {
    var me = await request("/api/auth/me");
    currentProfileId = me.profile.id;
    var matches = await request("/api/likes/matches");
    var contacts = document.getElementById("match-contacts");
    contacts.textContent = "";
    if (!matches.length) { contacts.textContent = "Aun no tienes matches mutuos."; return; }
    matches.forEach(function (match) {
        var button = document.createElement("button");
        button.className = "chat-match-button";
        if (match.profileImage) {
            var image = document.createElement("img");
            image.className = "contact-avatar";
            image.src = match.profileImage;
            image.alt = "Foto de " + match.name;
            button.appendChild(image);
        } else {
            var initial = document.createElement("span");
            initial.className = "contact-avatar contact-initial";
            initial.textContent = match.name.charAt(0).toUpperCase();
            button.appendChild(initial);
        }
        var name = document.createElement("span");
        name.textContent = match.name;
        button.appendChild(name);
        button.addEventListener("click", function () { selectMatch(match); });
        contacts.appendChild(button);
        var hide = document.createElement("button");
        hide.className = "hide-contact";
        hide.type = "button";
        hide.textContent = "Quitar contacto";
        hide.addEventListener("click", async function () {
            await request("/api/likes/matches/" + match.id, { method: "DELETE", headers: { "X-XSRF-TOKEN": csrfToken() } });
            loadPage();
        });
        contacts.appendChild(hide);
    });
}

document.getElementById("message-form").addEventListener("submit", async function (event) {
    event.preventDefault();
    var input = document.getElementById("message-input");
    var message = await request("/api/messages/" + selectedProfileId, { method: "POST", headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken() }, body: JSON.stringify({ content: input.value }) });
    var bubble = document.createElement("p");
    bubble.className = "message-own";
    bubble.textContent = message.content;
    document.getElementById("conversation-messages").appendChild(bubble);
    input.value = "";
});

loadPage();
window.setInterval(refreshConversation, 3000);
