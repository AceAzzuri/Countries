(function () {
    function setupPressFeedback() {
        const targets = document.querySelectorAll(
            ".bc-mark, .bc-brand img, .bc-badge, .bc-primary, .bc-score-form button, .bc-poll-options button, .bc-link-button, .bc-details-button, .bc-row-button, .bc-chat-reaction, .bc-chat-summary"
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

    function setupRulesModal() {
        const modal = document.querySelector("[data-rules-modal]");
        const openButton = document.querySelector("[data-rules-modal-open]");
        if (!modal || !openButton) {
            return;
        }

        openButton.addEventListener("click", function () {
            modal.showModal();
        });

        modal.querySelectorAll("[data-rules-modal-close]").forEach(function (button) {
            button.addEventListener("click", function () {
                modal.close();
            });
        });

        modal.addEventListener("click", function (event) {
            if (event.target === modal) {
                modal.close();
            }
        });
    }

    function setupScrollMemory() {
        const forms = document.querySelectorAll("form[data-preserve-scroll]");
        if (!forms.length) {
            return;
        }

        const storageKey = "bc-scroll:" + window.location.pathname;

        if ("scrollRestoration" in window.history) {
            window.history.scrollRestoration = "manual";
        }

        function restoreScroll() {
            const stored = window.sessionStorage.getItem(storageKey);
            if (!stored) {
                return;
            }

            window.sessionStorage.removeItem(storageKey);
            const scrollY = Number(stored);
            if (!Number.isFinite(scrollY) || scrollY < 0) {
                return;
            }

            window.requestAnimationFrame(function () {
                window.scrollTo({ top: scrollY, behavior: "auto" });
                window.requestAnimationFrame(function () {
                    window.scrollTo({ top: scrollY, behavior: "auto" });
                });
            });
        }

        forms.forEach(function (form) {
            form.addEventListener("submit", function () {
                window.sessionStorage.setItem(storageKey, String(window.scrollY || window.pageYOffset || 0));
            });
        });

        restoreScroll();
        window.addEventListener("pageshow", restoreScroll);
    }

    function setupMatchSectionMemory() {
        const sections = Array.from(document.querySelectorAll("[data-match-section-id]"));
        if (!sections.length) {
            return;
        }

        const storageKey = "bc-match-sections:v3:" + window.location.pathname;

        function readState() {
            try {
                return JSON.parse(window.sessionStorage.getItem(storageKey) || "{}");
            } catch (error) {
                return {};
            }
        }

        function writeState() {
            const state = {};
            sections.forEach(function (section) {
                state[section.dataset.matchSectionId] = section.open;
            });
            window.sessionStorage.setItem(storageKey, JSON.stringify(state));
        }

        const storedState = readState();
        sections.forEach(function (section) {
            if (Object.prototype.hasOwnProperty.call(storedState, section.dataset.matchSectionId)) {
                section.open = Boolean(storedState[section.dataset.matchSectionId]);
            }

            section.addEventListener("toggle", writeState);
        });

        if (window.location.hash) {
            const target = document.getElementById(window.location.hash.slice(1));
            if (target && target.matches("[data-match-section-id]")) {
                target.open = true;
                window.requestAnimationFrame(function () {
                    target.scrollIntoView({ block: "start", behavior: "auto" });
                });
            }
        }
    }

    function setupSaveAllPredictions() {
        const button = document.querySelector("[data-save-all-predictions]");
        if (!button) {
            return;
        }

        button.addEventListener("click", function () {
            const predictionForms = Array.from(document.querySelectorAll(".bc-score-form"));
            const form = document.createElement("form");
            form.method = "post";
            form.action = "/arena/predictions/all";
            form.dataset.preserveScroll = "";

            predictionForms.forEach(function (predictionForm) {
                ["matchId", "homeGoals", "awayGoals"].forEach(function (name) {
                    const source = predictionForm.querySelector("[name='" + name + "']");
                    if (!source) {
                        return;
                    }
                    const input = document.createElement("input");
                    input.type = "hidden";
                    input.name = name === "matchId" ? "matchIds" : name;
                    input.value = source.value;
                    form.appendChild(input);
                });
            });

            window.sessionStorage.setItem("bc-scroll:" + window.location.pathname, String(window.scrollY || window.pageYOffset || 0));
            document.body.appendChild(form);
            form.submit();
        });
    }

    function setupSaveAllResults() {
        const button = document.querySelector("[data-save-all-results]");
        if (!button) {
            return;
        }

        button.addEventListener("click", function () {
            const resultForms = Array.from(document.querySelectorAll(".bc-result-form"));
            const form = document.createElement("form");
            form.method = "post";
            form.action = "/admin/results/all";
            form.dataset.preserveScroll = "";

            resultForms.forEach(function (resultForm) {
                ["matchId", "homeScore", "awayScore"].forEach(function (name) {
                    const source = resultForm.querySelector("[name='" + name + "']");
                    if (!source) {
                        return;
                    }
                    const input = document.createElement("input");
                    input.type = "hidden";
                    input.name = name === "matchId" ? "matchIds" : name + "s";
                    input.value = source.value;
                    form.appendChild(input);
                });
            });

            window.sessionStorage.setItem("bc-scroll:" + window.location.pathname, String(window.scrollY || window.pageYOffset || 0));
            document.body.appendChild(form);
            form.submit();
        });
    }

    function setupMentionSuggestions() {
        document.querySelectorAll("[data-mention-textarea]").forEach(function (textarea) {
            const suggestions = textarea.parentElement.querySelector("[data-mention-suggestions]");
            if (!suggestions) {
                return;
            }

            const buttons = Array.from(suggestions.querySelectorAll("[data-mention-value]"));

            function currentMentionToken() {
                const cursor = textarea.selectionStart || 0;
                const before = textarea.value.slice(0, cursor);
                const match = before.match(/(^|\s)(@[^\s]*)$/);
                if (!match) {
                    return null;
                }
                return {
                    start: cursor - match[2].length,
                    end: cursor,
                    value: match[2].toLowerCase()
                };
            }

            function updateSuggestions() {
                const token = currentMentionToken();
                if (!token) {
                    suggestions.hidden = true;
                    return;
                }

                let visibleCount = 0;
                buttons.forEach(function (button) {
                    const value = (button.dataset.mentionValue || "").toLowerCase();
                    const visible = value.startsWith(token.value);
                    button.hidden = !visible;
                    if (visible) {
                        visibleCount++;
                    }
                });
                suggestions.hidden = visibleCount === 0;
            }

            function insertMention(mention) {
                const token = currentMentionToken();
                if (!token) {
                    return;
                }
                const before = textarea.value.slice(0, token.start);
                const after = textarea.value.slice(token.end);
                const suffix = after && !/^\s/.test(after) ? " " : " ";
                textarea.value = before + mention + suffix + after;
                const cursor = before.length + mention.length + suffix.length;
                textarea.focus();
                textarea.setSelectionRange(cursor, cursor);
                suggestions.hidden = true;
            }

            textarea.addEventListener("input", updateSuggestions);
            textarea.addEventListener("keyup", updateSuggestions);
            textarea.addEventListener("click", updateSuggestions);

            buttons.forEach(function (button) {
                button.addEventListener("click", function () {
                    insertMention(button.dataset.mentionValue || button.textContent.trim());
                });
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        setupPressFeedback();
        setupMatchModal();
        setupUserModals();
        setupCountryModal();
        setupRulesModal();
        setupMatchSectionMemory();
        setupScrollMemory();
        setupSaveAllPredictions();
        setupSaveAllResults();
        setupMentionSuggestions();
    });
}());
