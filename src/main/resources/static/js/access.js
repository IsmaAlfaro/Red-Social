function readCookie(name) {
    return document.cookie.split("; ").reduce(function (value, item) {
        var parts = item.split("=");
        return parts[0] === name ? decodeURIComponent(parts[1]) : value;
    }, null);
}

async function loadCsrfToken() {
    await fetch("/api/auth/csrf", { credentials: "same-origin" });
}

async function submitForm(event, path) {
    event.preventDefault();
    var form = event.currentTarget;
    var fields = new FormData(form);
    var payload = {};
    fields.forEach(function (value, key) {
        if (key === "profileImageFile") {
            return;
        }
        if (key === "interestIds") {
            payload.interestIds = payload.interestIds || [];
            payload.interestIds.push(Number(value));
            return;
        }
        payload[key] = value;
    });
    var message = document.getElementById("auth-message");

    var image = form.elements.profileImageFile.files[0];
    if (image) {
        if (image.size > 2000000) {
            message.textContent = "La foto debe pesar menos de 2 MB.";
            return;
        }
        payload.profileImage = await new Promise(function (resolve, reject) {
            var reader = new FileReader();
            reader.onload = function () { resolve(reader.result); };
            reader.onerror = reject;
            reader.readAsDataURL(image);
        });
    }

    if (path === "/api/auth/register" && !payload.interestIds) {
        message.textContent = "Elige al menos un gusto para crear tu perfil.";
        return;
    }

    var response = await fetch(path, {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            "X-XSRF-TOKEN": readCookie("XSRF-TOKEN")
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        message.textContent = "No fue posible completar la solicitud. Revisa los datos e intentalo de nuevo.";
        return;
    }

    message.textContent = "Acceso correcto. Cargando perfiles compatibles...";
    window.location.assign("/discover");
}

async function loadInterests() {
    var container = document.getElementById("interest-list");
    try {
        var response = await fetch("/api/interests");
        var interests = await response.json();
        container.textContent = "";
        ["MUSIC", "MOVIE", "SERIES"].forEach(function (type) {
            var groupInterests = interests.filter(function (interest) { return interest.type === type; });
            if (!groupInterests.length) {
                return;
            }
            var group = document.createElement("div");
            group.className = "interest-group";
            var title = document.createElement("p");
            title.textContent = { MUSIC: "Musica", MOVIE: "Peliculas", SERIES: "Series" }[type];
            group.appendChild(title);
            var options = document.createElement("div");
            options.className = "interest-options";
            groupInterests.forEach(function (interest) {
                var label = document.createElement("label");
                label.className = "interest-option";
                var input = document.createElement("input");
                input.type = "checkbox";
                input.name = "interestIds";
                input.value = interest.id;
                label.appendChild(input);
                var name = document.createElement("span");
                name.textContent = interest.name;
                label.appendChild(name);
                options.appendChild(label);
            });
            group.appendChild(options);
            container.appendChild(group);
        });
    } catch (error) {
        container.textContent = "No se pudieron cargar los intereses.";
    }
}

document.querySelectorAll(".tab").forEach(function (tab) {
    tab.addEventListener("click", function () {
        document.querySelectorAll(".tab").forEach(function (item) { item.classList.remove("active"); });
        document.querySelectorAll(".auth-form").forEach(function (form) { form.classList.add("hidden"); });
        tab.classList.add("active");
        document.getElementById(tab.dataset.form).classList.remove("hidden");
    });
});

document.getElementById("login-form").addEventListener("submit", function (event) {
    submitForm(event, "/api/auth/login");
});
document.getElementById("register-form").addEventListener("submit", function (event) {
    submitForm(event, "/api/auth/register");
});
document.getElementById("profile-image-input").addEventListener("change", function (event) {
    var image = event.target.files[0];
    var preview = document.getElementById("profile-image-preview");
    if (!image) {
        preview.classList.add("hidden");
        return;
    }
    preview.src = URL.createObjectURL(image);
    preview.classList.remove("hidden");
});
loadCsrfToken();
loadInterests();
