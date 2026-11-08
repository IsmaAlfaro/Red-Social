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
    matches.forEach(function (match) {
        var card = document.createElement("article");
        card.className = "match-card";
        var title = document.createElement("h2");
        title.textContent = match.profile.name;
        card.appendChild(title);
        addText(card, "degree", match.profile.degree);
        addText(card, "compatibility", match.compatibility + "% de afinidad");
        addText(card, "shared", match.sharedInterests.length
            ? "En comun: " + match.sharedInterests.join(", ")
            : "Completa tus gustos para mejorar tus matches.");
        list.appendChild(card);
    });
}

async function loadDiscover() {
    try {
        var me = await api("/api/auth/me");
        document.getElementById("welcome-title").textContent = "Hola, " + me.profile.name + ".";
        var matches = await api("/api/profiles/" + me.profile.id + "/matches");
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

loadDiscover();
