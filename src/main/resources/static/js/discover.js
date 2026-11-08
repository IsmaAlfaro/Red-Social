function csrfHeader() {
    var cookie = document.cookie.split("; ").find(function (item) {
        return item.indexOf("XSRF-TOKEN=") === 0;
    });
    return cookie ? decodeURIComponent(cookie.split("=")[1]) : "";
}

async function api(path, options) {
    var response = await fetch(path, Object.assign({ credentials: "same-origin" }, options));
    if (response.status === 401) {
        window.location.assign("/access");
        throw new Error("Sesion requerida");
    }
    if (!response.ok) {
        throw new Error("No se pudo cargar la informacion");
    }
    return response.status === 204 ? null : response.json();
}

function addText(element, className, text) {
    var child = document.createElement("p");
    child.className = className;
    child.textContent = text;
    element.appendChild(child);
}

function renderMatches(matches) {
    var list = document.getElementById("match-list");
    list.textContent = "";
    if (!matches.length) {
        addText(list, "empty-state", "Aun no hay perfiles para mostrar.");
        return;
    }
    var match = matches[0];
    var card = document.createElement("article");
    card.className = "match-card";
    if (match.profile.profileImage) {
        var image = document.createElement("img");
        image.className = "candidate-image";
        image.src = match.profile.profileImage;
        image.alt = "Foto de " + match.profile.name;
        card.appendChild(image);
    } else {
        var placeholder = document.createElement("div");
        placeholder.className = "candidate-placeholder";
        placeholder.textContent = match.profile.name.charAt(0).toUpperCase();
        card.appendChild(placeholder);
    }
    var title = document.createElement("h2");
    title.textContent = match.profile.name;
    card.appendChild(title);
    addText(card, "degree", match.profile.degree);
    addText(card, "compatibility", match.compatibility + "% de afinidad");
    addText(card, "interests", match.profile.interests.map(function (interest) {
        return interest.name;
    }).join(" / "));
    addText(card, "shared", match.sharedInterests.length
        ? "En comun: " + match.sharedInterests.join(", ")
        : "Completa tus gustos para mejorar tus matches.");
    var likeButton = document.createElement("button");
    likeButton.className = "like-button";
    likeButton.type = "button";
    likeButton.setAttribute("aria-label", "Me interesa");
    likeButton.setAttribute("title", "Me interesa");
    likeButton.innerHTML = "&hearts;";
    likeButton.addEventListener("click", function () {
        sendLike(match.profile.id, likeButton, card);
    });
    var rejectButton = document.createElement("button");
    rejectButton.className = "reject-button";
    rejectButton.type = "button";
    rejectButton.setAttribute("aria-label", "Pasar");
    rejectButton.setAttribute("title", "Pasar");
    rejectButton.innerHTML = "&times;";
    rejectButton.addEventListener("click", function () { sendRejection(match.profile.id); });
    var actions = document.createElement("div");
    actions.className = "match-actions";
    actions.appendChild(likeButton);
    actions.appendChild(rejectButton);
    card.appendChild(actions);
    list.appendChild(card);
    attachSwipe(card, match.profile.id, likeButton);
}

function attachSwipe(card, profileId, likeButton) {
    var startX = null;
    card.addEventListener("pointerdown", function (event) { startX = event.clientX; });
    card.addEventListener("pointerup", function (event) {
        if (startX === null) { return; }
        var distance = event.clientX - startX;
        startX = null;
        if (Math.abs(distance) < 80) { return; }
        if (distance < 0) {
            sendLike(profileId, likeButton, card);
        } else {
            sendRejection(profileId);
        }
    });
}

async function sendLike(profileId, button, card) {
    button.disabled = true;
    try {
        var result = await api("/api/likes/" + profileId, {
            method: "POST",
            headers: { "X-XSRF-TOKEN": csrfHeader() }
        });
        button.innerHTML = result.mutual ? "&hearts;" : "&#10003;";
        if (result.mutual) {
            card.classList.add("mutual-match");
            document.getElementById("match-reaction-text").textContent = "Tu y " + card.querySelector("h2").textContent + " tienen interes mutuo.";
            document.getElementById("match-reaction").classList.remove("hidden");
        } else {
            setTimeout(loadDiscover, 350);
        }
    } catch (error) {
        button.disabled = false;
        button.textContent = "Intentar de nuevo";
    }
}

async function sendRejection(profileId) {
    try {
        await api("/api/rejections/" + profileId, {
            method: "POST",
            headers: { "X-XSRF-TOKEN": csrfHeader() }
        });
        loadDiscover();
    } catch (error) {
        document.getElementById("match-list").textContent = "No se pudo pasar el perfil.";
    }
}

async function loadDiscover() {
    try {
        var me = await api("/api/auth/me");
        document.getElementById("current-user-name").textContent = me.profile.name;
        document.getElementById("current-user-initial").textContent = me.profile.name.charAt(0).toUpperCase();
        if (me.profile.profileImage) {
            var currentImage = document.getElementById("current-user-image");
            currentImage.src = me.profile.profileImage;
            currentImage.classList.remove("hidden");
            document.getElementById("current-user-initial").classList.add("hidden");
        }
        var matches = await api("/api/profiles/matches");
        renderMatches(matches);
    } catch (error) {
        if (error.message !== "Sesion requerida") {
            document.getElementById("match-list").textContent = error.message;
        }
    }
}

document.getElementById("logout-button").addEventListener("click", async function () {
    try {
        await api("/api/auth/logout", {
            method: "POST",
            headers: { "X-XSRF-TOKEN": csrfHeader() }
        });
        window.location.assign("/access");
    } catch (error) {
        window.location.assign("/access");
    }
});
document.getElementById("close-match-reaction").addEventListener("click", function () {
    document.getElementById("match-reaction").classList.add("hidden");
    loadDiscover();
});

loadDiscover();
