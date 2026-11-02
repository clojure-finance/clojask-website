// Small enhancements for the Clojask docs. The pages work without this file.
(function () {
  "use strict";

  var doc = document.documentElement;
  var body = document.body;

  // ---- Mobile menu ----
  var toggle = document.querySelector(".menu-toggle");
  var scrim = document.querySelector(".scrim");

  function setMenu(open) {
    body.classList.toggle("nav-open", open);
    if (toggle) toggle.setAttribute("aria-expanded", String(open));
    if (scrim) scrim.hidden = !open;
  }
  if (toggle) toggle.addEventListener("click", function () {
    setMenu(!body.classList.contains("nav-open"));
  });
  if (scrim) scrim.addEventListener("click", function () { setMenu(false); });
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && body.classList.contains("nav-open")) {
      setMenu(false);
      if (toggle) toggle.focus();
    }
  });

  // ---- Search page titles and headings from search.json, loaded on first use ----
  var search = document.querySelector(".doc-search");
  if (search && window.fetch) {
    var searchInput = search.querySelector("input");
    var searchResults = search.querySelector(".search-results");
    var searchStatus = search.querySelector(".search-status");
    var entries = null;
    var loading = null;
    function normalize(text) {
      return text.toLowerCase().replace(/[^a-z0-9]+/g, " ").trim();
    }
    function loadIndex() {
      if (!loading) {
        loading = fetch(search.getAttribute("data-index"))
          .then(function (response) {
            if (!response.ok) throw new Error(response.statusText);
            return response.json();
          })
          .then(function (data) {
            entries = data.map(function (d) {
              // "Page › Section" tells apart headings that repeat on a page.
              var context = d.section ? d.page + " \u203a " + d.section : d.page;
              return {
                uri: d.uri, label: d.title, context: context,
                title: normalize(d.title),
                text: normalize(d.title + " " + context)
              };
            });
          })
          .catch(function () {
            loading = null;  // try again on the next keystroke
            searchStatus.textContent = "Search could not be loaded. Use the menu below.";
          });
      }
      return loading;
    }
    function resultItem(entry) {
      var item = document.createElement("li");
      var link = document.createElement("a");
      var title = document.createElement("span");
      var context = document.createElement("span");
      link.href = entry.uri;
      title.className = "search-title";
      title.textContent = entry.label;
      context.className = "search-page";
      context.textContent = entry.context;
      link.appendChild(title);
      link.appendChild(context);
      item.appendChild(link);
      return item;
    }
    function updateSearch() {
      var query = normalize(searchInput.value);
      var terms = query.split(" ");
      searchResults.textContent = "";
      searchResults.hidden = true;
      if (!query) { searchStatus.textContent = ""; return; }
      if (!entries) return;
      var matches = entries.filter(function (entry) {
        return terms.every(function (term) { return entry.text.includes(term); });
      });
      function rank(entry) {
        if (entry.title === query) return 0;
        if (entry.title.startsWith(query)) return 1;
        if (terms.every(function (term) { return entry.title.includes(term); })) return 2;
        return 3;
      }
      matches.sort(function (a, b) { return rank(a) - rank(b); });
      matches.slice(0, 8).forEach(function (entry) {
        searchResults.appendChild(resultItem(entry));
      });
      searchResults.hidden = !matches.length;
      searchStatus.textContent = matches.length
        ? (matches.length > 8 ? "Showing 8 of " + matches.length : String(matches.length)) +
          (matches.length === 1 ? " result." : " results.")
        : "No results. Try a function or page name.";
      searchResults.scrollTop = 0;
    }
    searchInput.addEventListener("focus", loadIndex);
    searchInput.addEventListener("input", function () {
      loadIndex().then(updateSearch);
    });
    searchInput.addEventListener("keydown", function (e) {
      if (e.key === "ArrowDown") {
        var first = searchResults.querySelector("a");
        if (first) { e.preventDefault(); first.focus(); }
      }
    });
    search.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && searchInput.value) {
        e.stopPropagation();
        searchInput.value = "";
        updateSearch();
        searchInput.focus();
      }
    });
    searchResults.addEventListener("click", function (e) {
      var link = e.target.closest("a");
      if (link && !e.ctrlKey && !e.metaKey && !e.shiftKey && !e.altKey) {
        setMenu(false);
        // A same-page result should move keyboard focus into the document too.
        if (link.pathname === window.location.pathname && link.hash) {
          var heading = document.getElementById(decodeURIComponent(link.hash.slice(1)));
          if (heading) {
            heading.setAttribute("tabindex", "-1");
            heading.focus();
          }
        }
      }
    });
    // "/" jumps to the search box, opening the menu first on small screens.
    document.addEventListener("keydown", function (e) {
      if (e.key !== "/" || e.ctrlKey || e.metaKey || e.altKey || e.defaultPrevented) return;
      var target = e.target;
      if (target.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName)) return;
      e.preventDefault();
      if (toggle && toggle.offsetParent) setMenu(true);
      searchInput.focus();
    });
    search.hidden = false;
  }

  // ---- Light / dark mode ----
  var themeButton = document.querySelector(".theme-toggle");
  function isDark() {
    var t = doc.getAttribute("data-theme");
    if (t) return t === "dark";
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  }
  if (themeButton) themeButton.addEventListener("click", function () {
    var next = isDark() ? "light" : "dark";
    doc.setAttribute("data-theme", next);
    try { localStorage.setItem("theme", next); } catch (e) {}
  });

  var content = document.querySelector(".doc-body, .home-body");
  if (!content) return;

  // ---- Tables scroll sideways on small screens ----
  content.querySelectorAll("table").forEach(function (table) {
    if (table.parentElement.classList.contains("table-wrap")) return;
    var wrap = document.createElement("div");
    wrap.className = "table-wrap";
    table.parentNode.insertBefore(wrap, table);
    wrap.appendChild(table);
  });

  // ---- Responsive videos ----
  content.querySelectorAll("iframe").forEach(function (frame) {
    if (frame.parentElement.classList.contains("video")) return;
    var wrap = document.createElement("div");
    wrap.className = "video";
    frame.parentNode.insertBefore(wrap, frame);
    wrap.appendChild(frame);
  });

  // ---- Callouts: "> **Tip** ..." and "> **Warning** ..." ----
  content.querySelectorAll("blockquote").forEach(function (q) {
    var first = q.querySelector("p > strong:first-child");
    if (!first) return;
    var word = first.textContent.trim().toLowerCase();
    if (/^tip/.test(word)) q.classList.add("tip");
    else if (/^(warning|careful|caution|watch out)/.test(word)) q.classList.add("warning");
  });

  // ---- Syntax highlighting and copy buttons ----
  if (window.hljs) {
    document.querySelectorAll("pre code").forEach(function (block) {
      if (!block.className) block.classList.add("language-plaintext");
      window.hljs.highlightElement(block);
    });
  }

  function addCopyButton(container, code, label) {
    if (!navigator.clipboard) return;
    var button = document.createElement("button");
    button.type = "button";
    button.className = "copy-btn";
    button.textContent = "Copy";
    button.setAttribute("aria-label", label);
    button.setAttribute("aria-live", "polite");
    var reset;
    button.addEventListener("click", function () {
      clearTimeout(reset);
      navigator.clipboard.writeText(code.textContent.replace(/\n$/, "")).then(function () {
        button.textContent = "Copied";
        button.setAttribute("aria-label", "Copied to clipboard");
      }).catch(function () {
        button.textContent = "Copy failed";
        button.setAttribute("aria-label", "Copy failed. Select and copy the code manually.");
      }).finally(function () {
        reset = setTimeout(function () {
          button.textContent = "Copy";
          button.setAttribute("aria-label", label);
        }, 2500);
      });
    });
    container.appendChild(button);
  }

  document.querySelectorAll(".main pre").forEach(function (pre) {
    if (pre.parentElement.classList.contains("code-block")) return;
    var wrap = document.createElement("div");
    wrap.className = "code-block";
    pre.parentNode.insertBefore(wrap, pre);
    wrap.appendChild(pre);

    addCopyButton(wrap, pre, "Copy code");
  });
  document.querySelectorAll(".install-row").forEach(function (row) {
    addCopyButton(row, row.querySelector("code"),
      "Copy " + row.querySelector(".install-label").textContent + " dependency");
  });

  // ---- Heading anchors ----
  content.querySelectorAll("h2[id], h3[id], h4[id]").forEach(function (h) {
    var a = document.createElement("a");
    a.className = "anchor";
    a.href = "#" + h.id;
    a.setAttribute("aria-label", "Link to this section");
    a.textContent = "#";
    h.insertBefore(a, h.firstChild);
  });

  // ---- Highlight the current section in "On this page" ----
  var tocLinks = Array.prototype.slice.call(document.querySelectorAll(".toc-side a[href^='#']"));
  if (!tocLinks.length || !("IntersectionObserver" in window)) return;

  var byId = {};
  var headings = [];
  tocLinks.forEach(function (link) {
    var id = decodeURIComponent(link.getAttribute("href").slice(1));
    var h = document.getElementById(id);
    if (h) { byId[id] = link; headings.push(h); }
  });

  var visible = {};
  function update() {
    var current = null;
    for (var i = 0; i < headings.length; i++) {
      if (headings[i].getBoundingClientRect().top < 120) current = headings[i];
      else break;
    }
    if (!current && headings.length) current = headings[0];
    tocLinks.forEach(function (l) { l.classList.remove("current"); });
    if (current && byId[current.id]) {
      var link = byId[current.id];
      link.classList.add("current");
      var box = link.closest(".toc-side");
      var top = link.offsetTop - box.offsetTop;
      if (top < box.scrollTop || top > box.scrollTop + box.clientHeight - 40) {
        box.scrollTop = top - box.clientHeight / 3;
      }
    }
  }
  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (e) { visible[e.target.id] = e.isIntersecting; });
    update();
  }, { rootMargin: "0px 0px -60% 0px" });
  headings.forEach(function (h) { observer.observe(h); });
  window.addEventListener("scroll", function () { window.requestAnimationFrame(update); }, { passive: true });
  update();
})();
