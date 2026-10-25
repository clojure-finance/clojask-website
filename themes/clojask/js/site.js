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
    if (e.key === "Escape") setMenu(false);
  });

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

  content.querySelectorAll("pre").forEach(function (pre) {
    if (pre.parentElement.classList.contains("code-block")) return;
    var wrap = document.createElement("div");
    wrap.className = "code-block";
    pre.parentNode.insertBefore(wrap, pre);
    wrap.appendChild(pre);

    if (!navigator.clipboard) return;
    var button = document.createElement("button");
    button.type = "button";
    button.className = "copy-btn";
    button.textContent = "Copy";
    button.addEventListener("click", function () {
      navigator.clipboard.writeText(pre.innerText.replace(/\n$/, "")).then(function () {
        button.textContent = "Copied";
        setTimeout(function () { button.textContent = "Copy"; }, 1500);
      });
    });
    wrap.appendChild(button);
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
