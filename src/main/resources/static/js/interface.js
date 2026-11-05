(() => {
    const menu = document.querySelector(".bouton-menu");
    const navigation = document.querySelector(".barre-laterale");
    const mobile = window.matchMedia("(max-width: 1000px)");
    const fermerMenu = () => {
        document.body.classList.remove("menu-ouvert");
        menu?.setAttribute("aria-expanded", "false");
    };
    menu?.addEventListener("click", () => {
        const ouvert = document.body.classList.toggle("menu-ouvert");
        menu.setAttribute("aria-expanded", String(ouvert));
        if (ouvert) navigation?.querySelector("a")?.focus();
    });
    document.querySelector(".voile-navigation")?.addEventListener("click", fermerMenu);
    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && document.body.classList.contains("menu-ouvert")) {
            fermerMenu();
            menu?.focus();
        }
        if (event.key === "Tab" && mobile.matches && document.body.classList.contains("menu-ouvert")) {
            const liens = [...navigation.querySelectorAll("a")];
            const premier = liens[0], dernier = liens[liens.length - 1];
            if (event.shiftKey && document.activeElement === premier) {
                event.preventDefault(); dernier.focus();
            } else if (!event.shiftKey && document.activeElement === dernier) {
                event.preventDefault(); premier.focus();
            }
        }
    });
    mobile.addEventListener("change", fermerMenu);
    const liens = [...document.querySelectorAll("[data-navigation]")];
    const chemin = window.location.pathname;
    let actif = liens.filter(lien => chemin === new URL(lien.href).pathname
            || chemin.startsWith(new URL(lien.href).pathname + "/"))
        .sort((a, b) => b.pathname.length - a.pathname.length)[0];
    if (!actif && /\/(equipements|incidents)\//.test(chemin)) {
        actif = liens.find(lien => lien.pathname.endsWith("/stations"));
    }
    actif?.setAttribute("aria-current", "page");
    const titre = document.querySelector("[data-titre-page]");
    if (titre) titre.textContent = document.title.split("·")[0].trim();

    const normaliser = valeur => valeur.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLocaleLowerCase("fr");
    document.querySelectorAll(".table-scroll").forEach((zone, index) => {
        const table = zone.querySelector("table");
        const lignes = [...(table?.querySelectorAll("tbody tr") || [])];
        if (!lignes.length) return;
        const barre = document.createElement("div");
        barre.className = "recherche-tableau";
        const groupe = document.createElement("div");
        const label = document.createElement("label");
        label.htmlFor = "recherche-tableau-" + index;
        label.textContent = "Rechercher dans cette liste";
        const champ = document.createElement("input");
        champ.type = "search";
        champ.id = label.htmlFor;
        champ.placeholder = "Description, station, équipement…";
        table.id ||= "liste-" + index;
        champ.setAttribute("aria-controls", table.id);
        const compteur = document.createElement("span");
        compteur.setAttribute("role", "status");
        const vide = document.createElement("p");
        vide.className = "aucun-resultat";
        vide.textContent = "Aucun résultat. Essayez un autre terme ou effacez la recherche.";
        vide.hidden = true;
        const textes = lignes.map(ligne => normaliser(ligne.textContent));
        const filtrer = () => {
            const mots = normaliser(champ.value.trim()).split(/\s+/).filter(Boolean);
            let visibles = 0;
            lignes.forEach((ligne, i) => {
                ligne.hidden = !mots.every(mot => textes[i].includes(mot));
                if (!ligne.hidden) visibles++;
            });
            compteur.textContent = visibles + " / " + lignes.length + " résultat(s)";
            vide.hidden = visibles !== 0;
        };
        champ.addEventListener("input", filtrer);
        groupe.append(label, champ);
        barre.append(groupe, compteur);
        zone.prepend(barre);
        zone.append(vide);
        filtrer();
    });
})();
