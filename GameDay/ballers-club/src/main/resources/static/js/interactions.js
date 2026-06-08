(function () {
    function setupPressFeedback() {
        const targets = document.querySelectorAll(
            ".bc-mark, .bc-brand img, .bc-badge, .bc-primary, .bc-score-form button, .bc-poll-options button, .bc-link-button, .bc-details-button, .bc-row-button"
        );

        targets.forEach(function (target) {
            const release = function () {
                target.classList.remove("bc-pressing");
            };

            target.addEventListener("pointerdown", function () {
                target.classList.add("bc-pressing");
            });
            target.addEventListener("pointerup", release);
            target.addEventListener("pointercancel", release);
            target.addEventListener("pointerleave", release);
            target.addEventListener("blur", release);
            target.addEventListener("keydown", function (event) {
                if (event.key === " " || event.key === "Enter") {
                    target.classList.add("bc-pressing");
                }
            });
            target.addEventListener("keyup", release);
        });
    }

    function setupMatchModal() {
        const modal = document.querySelector("[data-match-modal]");
        if (!modal) {
            return;
        }

        const title = modal.querySelector("[data-match-modal-title]");
        const meta = modal.querySelector("[data-match-modal-meta]");
        const status = modal.querySelector("[data-match-modal-status]");
        const closeButtons = modal.querySelectorAll("[data-match-modal-close]");

        function close() {
            modal.close();
        }

        document.querySelectorAll("[data-match-details]").forEach(function (button) {
            button.addEventListener("click", function () {
                title.textContent = button.dataset.matchTitle || "Match details";
                meta.textContent = button.dataset.matchMeta || "";
                status.textContent = button.dataset.matchStatus || "";
                modal.showModal();
            });
        });

        closeButtons.forEach(function (button) {
            button.addEventListener("click", close);
        });

        modal.addEventListener("click", function (event) {
            if (event.target === modal) {
                close();
            }
        });
    }

    function setupUserModals() {
        document.querySelectorAll("[data-user-profile]").forEach(function (button) {
            button.addEventListener("click", function () {
                const modal = document.getElementById(button.dataset.userProfile);
                if (modal) {
                    modal.showModal();
                }
            });
        });

        document.querySelectorAll(".bc-user-modal").forEach(function (modal) {
            modal.querySelectorAll("[data-user-modal-close]").forEach(function (button) {
                button.addEventListener("click", function () {
                    modal.close();
                });
            });

            modal.addEventListener("click", function (event) {
                if (event.target === modal) {
                    modal.close();
                }
            });
        });
    }

    function setupCountryModal() {
        const modal = document.querySelector("[data-country-modal]");
        if (!modal) {
            return;
        }

        const title = modal.querySelector("[data-country-modal-title]");
        const meta = modal.querySelector("[data-country-modal-meta]");
        const note = modal.querySelector("[data-country-modal-note]");
        const players = modal.querySelector("[data-country-modal-players]");

        function close() {
            modal.close();
        }

        document.querySelectorAll("[data-country-details]").forEach(function (button) {
            button.addEventListener("click", function () {
                title.textContent = button.dataset.countryName || "Land";
                meta.textContent = button.dataset.countryMeta || "";
                note.textContent = button.dataset.countryNote || "Trupforslag og nøglespillere kan kobles på senere.";
                players.innerHTML = "";
                (button.dataset.countryPlayers || "Trupforslag kommer snart | Officiel trup kan ændre sig op til kampstart").split("|").forEach(function (player) {
                    if (!player.trim()) {
                        return;
                    }
                    const item = document.createElement("span");
                    item.textContent = player.trim();
                    players.appendChild(item);
                });
                modal.showModal();
            });
        });

        modal.querySelectorAll("[data-country-modal-close]").forEach(function (button) {
            button.addEventListener("click", close);
        });

        modal.addEventListener("click", function (event) {
            if (event.target === modal) {
                close();
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        setupPressFeedback();
        setupMatchModal();
        setupUserModals();
        setupCountryModal();
    });
}());
