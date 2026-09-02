import base64

icon_b64 = open('config/javadoc/icon_base64.txt').read().strip()

js_code = """/**
 * LITIENGINE Javadoc Enhancements
 * Constructs 2-level top navigation matching official docs:
 * Level 1: Official brand logo (icon.png), Theme toggle (Moon/Sun), Search bar (Ctrl+K), GitHub link
 * Level 2: Nav tabs (with underline indicator), Sponsor button
 * Right TOC: "On this page" with full-width filter capsule placed directly below the title.
 * Sticky A-Z Index Navigation: Clean, non-duplicated frosted-glass sticky jump bar on index-all.html.
 * Clean Hierarchy Trees: Streamline repetitive package namespaces into neat package chips.
 * Page Search Enhancement: Search capsule matching header design on search.html.
 * Footer: Social links (Open Collective, GitHub, Discord) matching official docs.
 * Favicons: official favicon.ico, favicon-32x32.png, favicon-16x16.png.
 */
(function () {
  'use strict';

  function getRoot() {
    if (typeof pathtoroot !== 'undefined' && pathtoroot) return pathtoroot;
    if (window.pathtoroot) return window.pathtoroot;
    const script = document.querySelector('script[src*="litiengine.js"]');
    if (script) {
      const src = script.getAttribute('src');
      return src.substring(0, src.indexOf('script-files/litiengine.js'));
    }
    return '';
  }

  function getStoredTheme() {
    try {
      return localStorage.getItem('liti-theme');
    } catch (e) {
      return null;
    }
  }

  function setStoredTheme(val) {
    try {
      localStorage.setItem('liti-theme', val);
    } catch (e) {}
  }

  // Apply saved theme immediately
  const savedTheme = getStoredTheme();
  if (savedTheme) {
    document.documentElement.setAttribute('data-theme', savedTheme);
  }

  const MOON_SVG = `<svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`;
  const SUN_SVG = `<svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>`;
  const SEARCH_SVG = `<svg class="search-icon-svg" viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>`;
  const GITHUB_SVG = `<svg class="github-icon" viewBox="0 0 24 24" width="19" height="19" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>`;
  const HEART_SVG = `<svg class="sponsor-heart-icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>`;
  
  const OFFICIAL_LOGO_SRC = 'data:image/png;base64,""" + icon_b64 + """';

  function isDarkMode() {
    const current = document.documentElement.getAttribute('data-theme');
    if (current === 'dark') return true;
    if (current === 'light') return false;
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  function loadGoogleFonts() {
    if (document.querySelector('link[href*="fonts.googleapis.com"]')) return;
    const head = document.head || document.getElementsByTagName('head')[0];
    if (!head) return;
    const preconnect1 = document.createElement('link');
    preconnect1.rel = 'preconnect';
    preconnect1.href = 'https://fonts.googleapis.com';
    head.appendChild(preconnect1);

    const preconnect2 = document.createElement('link');
    preconnect2.rel = 'preconnect';
    preconnect2.href = 'https://fonts.gstatic.com';
    preconnect2.crossOrigin = 'anonymous';
    head.appendChild(preconnect2);

    const fontLink = document.createElement('link');
    fontLink.rel = 'stylesheet';
    fontLink.href = 'https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&family=JetBrains+Mono:ital,wght@0,100..800;1,100..800&display=swap';
    fontLink.media = 'all';
    head.appendChild(fontLink);
  }

  loadGoogleFonts();

  function setupFavicons() {
    const r = getRoot();
    const head = document.head || document.getElementsByTagName('head')[0];
    if (!head || document.querySelector('link[rel*="icon"]')) return;

    const faviconTypes = [
      { rel: 'icon', type: 'image/png', sizes: '32x32', href: r + 'assets/favicon-32x32.png' },
      { rel: 'icon', type: 'image/png', sizes: '16x16', href: r + 'assets/favicon-16x16.png' },
      { rel: 'shortcut icon', href: r + 'assets/favicon.ico' },
      { rel: 'apple-touch-icon', sizes: '48x48', href: r + 'assets/favicon-48x48.png' }
    ];

    faviconTypes.forEach(fav => {
      const link = document.createElement('link');
      link.rel = fav.rel;
      if (fav.type) link.type = fav.type;
      if (fav.sizes) link.sizes = fav.sizes;
      link.href = fav.href;
      head.appendChild(link);
    });
  }

  function setupTwoLevelNavigation() {
    try {
      const header = document.querySelector('header[role="banner"]');
      if (!header || document.querySelector('.liti-header-level1')) return;

      const navList = document.querySelector('ul.nav-list');
      const navListSearch = document.querySelector('.nav-list-search');
      if (!navList || !navListSearch) return;

      // Create 2-Level Header Structure
      const level1 = document.createElement('div');
      level1.className = 'liti-header-level1';

      const level1Inner = document.createElement('div');
      level1Inner.className = 'liti-header-container';

      // Brand Link (Exact 28.79px logo from DevTools)
      const brand = document.createElement('a');
      brand.className = 'liti-brand-link';
      brand.href = 'https://gurkenlabs.github.io/litiengine-docs/';
      brand.innerHTML = `<img class="liti-brand-logo" src="${OFFICIAL_LOGO_SRC}" alt="LITIENGINE Logo" width="28.79" height="28.79"> <span class="liti-brand-text"><strong>LITIENGINE Docs</strong> <span class="api-tag">API</span></span>`;
      level1Inner.appendChild(brand);

      // Right tools (Theme switch, Search capsule, GitHub link)
      const level1Right = document.createElement('div');
      level1Right.className = 'liti-header-level1-right';

      // 1. Theme toggle button (Dark -> Moon icon, Light -> Sun icon)
      const themeBtn = document.createElement('button');
      themeBtn.type = 'button';
      themeBtn.className = 'liti-theme-toggle';
      themeBtn.title = 'Toggle dark / light mode';
      themeBtn.setAttribute('aria-label', 'Toggle theme');

      function updateToggleIcon() {
        themeBtn.innerHTML = isDarkMode() ? MOON_SVG : SUN_SVG;
      }

      themeBtn.addEventListener('click', function () {
        const nextTheme = isDarkMode() ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', nextTheme);
        setStoredTheme(nextTheme);
        updateToggleIcon();
      });

      updateToggleIcon();
      level1Right.appendChild(themeBtn);

    // 2. Search capsule
    if (navListSearch) {
      const searchInput = navListSearch.querySelector('#search-input');
      if (searchInput) {
        searchInput.setAttribute('placeholder', 'Search');
      }

      if (!navListSearch.querySelector('.search-icon-svg')) {
        const iconWrapper = document.createElement('span');
        iconWrapper.className = 'search-icon-wrapper';
        iconWrapper.innerHTML = SEARCH_SVG;
        navListSearch.insertBefore(iconWrapper, navListSearch.firstChild);
      }

      if (!navListSearch.querySelector('.search-kbd')) {
        const kbd = document.createElement('kbd');
        kbd.className = 'search-kbd';
        const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
        kbd.textContent = isMac ? '⌘K' : 'Ctrl+K';
        navListSearch.appendChild(kbd);
      }

      level1Right.appendChild(navListSearch);
    }

    // 3. GitHub Repo Link (clean & simplified)
    const githubLink = document.createElement('a');
    githubLink.href = 'https://github.com/gurkenlabs/litiengine';
    githubLink.target = '_blank';
    githubLink.rel = 'noopener noreferrer';
    githubLink.className = 'liti-github-link';
    githubLink.innerHTML = `${GITHUB_SVG} <span>gurkenlabs/litiengine</span>`;
    level1Right.appendChild(githubLink);

    level1Inner.appendChild(level1Right);
    level1.appendChild(level1Inner);

    // Level 2 (Tabs & Sponsor Button)
    const level2 = document.createElement('div');
    level2.className = 'liti-header-level2';

    const level2Inner = document.createElement('div');
    level2Inner.className = 'liti-header-container';

    // Move navList into level 2
    if (navList) {
      level2Inner.appendChild(navList);
    }

    // Sponsor button on far right of Level 2 with pure SVG heart icon
    const sponsorBtn = document.createElement('a');
    sponsorBtn.href = 'https://opencollective.com/litiengine';
    sponsorBtn.target = '_blank';
    sponsorBtn.rel = 'noopener noreferrer';
    sponsorBtn.className = 'md-tabs__link--sponsor';
    sponsorBtn.innerHTML = `${HEART_SVG} <span>Sponsor</span>`;
    level2Inner.appendChild(sponsorBtn);

    level2.appendChild(level2Inner);

    // Insert into header
    const topNav = document.querySelector('.top-nav');
    if (topNav) {
      topNav.innerHTML = '';
      topNav.appendChild(level1);
      topNav.appendChild(level2);
    }
  } catch (e) {}
  }

  function setupPageSearchInput() {
    const pageInput = document.getElementById('page-search-input');
    if (!pageInput || pageInput.parentElement.classList.contains('page-search-wrapper')) return;

    const parent = pageInput.parentElement;
    const wrapper = document.createElement('div');
    wrapper.className = 'page-search-wrapper';

    const iconSpan = document.createElement('span');
    iconSpan.className = 'page-search-icon';
    iconSpan.innerHTML = SEARCH_SVG;

    parent.insertBefore(wrapper, pageInput);
    wrapper.appendChild(iconSpan);
    wrapper.appendChild(pageInput);

    pageInput.setAttribute('placeholder', 'Search documentation...');

    const resetBtn = parent.querySelector('#page-search-reset');
    if (resetBtn) {
      wrapper.appendChild(resetBtn);
    }
  }

  function setupStickyIndexNav() {
    if (!document.body.classList.contains('index-page')) return;
    const main = document.querySelector('body.index-page main');
    if (!main || main.querySelector('.index-sticky-bar')) return;

    const firstHeading = main.querySelector('h2.title');
    if (!firstHeading) return;

    const header = main.querySelector('.header');
    const nodesToMove = [];
    let curr = header ? header.nextSibling : main.firstChild;
    while (curr && curr !== firstHeading) {
      const next = curr.nextSibling;
      nodesToMove.push(curr);
      curr = next;
    }

    const letterLinks = [];
    const secondaryLinks = [];

    nodesToMove.forEach(node => {
      if (node.nodeType === Node.ELEMENT_NODE && node.tagName === 'A') {
        const href = node.getAttribute('href') || '';
        if (href.startsWith('#I:')) {
          letterLinks.push(node);
        } else if (href.includes('-index.html') || href.includes('constant-values.html') || href.includes('serialized-form.html')) {
          secondaryLinks.push(node);
        }
      }
    });

    nodesToMove.forEach(n => {
      if (n.parentNode) n.parentNode.removeChild(n);
    });

    // Remove redundant bottom duplicate jump bar links after the last dl.index
    const dls = main.querySelectorAll('dl.index');
    if (dls.length) {
      const lastDl = dls[dls.length - 1];
      let bottomNode = lastDl.nextSibling;
      const footer = main.querySelector('footer');
      while (bottomNode && bottomNode !== footer) {
        const next = bottomNode.nextSibling;
        if (bottomNode.nodeType === Node.ELEMENT_NODE && bottomNode.tagName === 'A') {
          bottomNode.remove();
        } else if (bottomNode.nodeType === Node.TEXT_NODE || (bottomNode.nodeType === Node.ELEMENT_NODE && bottomNode.tagName === 'BR')) {
          bottomNode.remove();
        }
        bottomNode = next;
      }
    }

    const stickyBar = document.createElement('div');
    stickyBar.className = 'index-sticky-bar';

    const letterContainer = document.createElement('div');
    letterContainer.className = 'index-letters-container';
    letterLinks.forEach(link => {
      link.textContent = link.textContent.trim();
      letterContainer.appendChild(link);
    });
    stickyBar.appendChild(letterContainer);

    if (secondaryLinks.length) {
      const secondaryContainer = document.createElement('div');
      secondaryContainer.className = 'index-secondary-container';
      secondaryLinks.forEach(link => {
        secondaryContainer.appendChild(link);
      });
      stickyBar.appendChild(secondaryContainer);
    }

    if (header) {
      header.insertAdjacentElement('afterend', stickyBar);
    } else {
      main.insertBefore(stickyBar, firstHeading);
    }
  }

  function cleanHierarchyTree() {
    if (!document.body.classList.contains('tree-page') && !document.body.classList.contains('package-tree-page')) return;

    // 1. Unwrap java.lang.Object root nodes so all direct classes start directly at the top level
    const rootNodes = Array.from(document.querySelectorAll('section.hierarchy > ul > li.circle'));
    rootNodes.forEach(li => {
      const link = li.querySelector('a[href*="java.lang/Object.html"], a[title*="java.lang.Object"]');
      const directText = li.childNodes.length ? li.childNodes[0].textContent : '';
      if ((link && link.textContent.trim() === 'Object') || directText.includes('java.lang.Object') || directText.trim() === 'java.lang.') {
        const childUl = li.querySelector('ul');
        if (childUl && li.parentNode) {
          const parentUl = li.parentNode;
          const children = Array.from(childUl.children);
          children.forEach(child => {
            parentUl.insertBefore(child, li);
          });
          parentUl.removeChild(li);
        }
      }
    });

    const treeNodes = document.querySelectorAll('section.hierarchy li.circle');
    if (!treeNodes.length) return;

    function parseTreeText(rawText) {
      const fragment = document.createDocumentFragment();
      let text = rawText;

      // Handle standalone closing paren
      if (text.trim() === ')') {
        return fragment;
      }

      // 1. Optional leading type parameter (e.g. "<T>" or "<E>")
      const typeParamMatch = text.match(/^(\\s*<[^>]+>\\s*)/);
      if (typeParamMatch) {
        const span = document.createElement('span');
        span.className = 'tree-type-param';
        span.textContent = typeParamMatch[1].trim();
        fragment.appendChild(span);
        text = text.slice(typeParamMatch[0].length);
      }

      // 2. Relation tag: "(implements ", "(also extends ", "(extends ", or comma separator
      if (text.includes('(implements ')) {
        const span = document.createElement('span');
        span.className = 'tree-impl-tag';
        span.textContent = 'implements';
        fragment.appendChild(span);
        text = text.replace(/\\s*\\(implements\\s*/, '');
      } else if (text.includes('(also extends ')) {
        const span = document.createElement('span');
        span.className = 'tree-impl-tag';
        span.textContent = 'extends';
        fragment.appendChild(span);
        text = text.replace(/\\s*\\(also extends\\s*/, '');
      } else if (text.includes('(extends ')) {
        const span = document.createElement('span');
        span.className = 'tree-impl-tag';
        span.textContent = 'extends';
        fragment.appendChild(span);
        text = text.replace(/\\s*\\(extends\\s*/, '');
      } else if (/^\\s*,\\s*/.test(text)) {
        const span = document.createElement('span');
        span.className = 'tree-impl-sep';
        span.textContent = ',';
        fragment.appendChild(span);
        text = text.replace(/^\\s*,\\s*/, '');
      }

      // 3. Clean trailing paren or whitespace
      text = text.replace(/\\)\\s*$/, '').trim();

      // 4. Package namespace badge
      if (text.length > 0) {
        let pkg = text;
        if (pkg.endsWith('.')) pkg = pkg.slice(0, -1);
        if (pkg.length > 0) {
          let displayPkg = pkg;
          if (pkg.startsWith('de.gurkenlabs.litiengine.')) {
            displayPkg = pkg.replace('de.gurkenlabs.litiengine.', '');
          } else if (pkg === 'de.gurkenlabs.litiengine') {
            displayPkg = 'litiengine';
          }

          const span = document.createElement('span');
          span.className = 'tree-pkg-badge';
          span.title = pkg;
          span.textContent = displayPkg;
          fragment.appendChild(span);
        }
      }

      return fragment;
    }

    treeNodes.forEach(li => {
      Array.from(li.childNodes).forEach(node => {
        if (node.nodeType === Node.TEXT_NODE) {
          const raw = node.textContent;
          if (!raw.trim()) return;

          const fragment = parseTreeText(raw);
          li.replaceChild(fragment, node);
        }
      });
    });
  }

  function setupFooter() {
    const footer = document.querySelector('footer[role="contentinfo"]') || document.querySelector('footer');
    if (!footer || footer.querySelector('.md-social')) return;

    const socialDiv = document.createElement('div');
    socialDiv.className = 'md-social';
    socialDiv.innerHTML = `
      <a href="https://opencollective.com/litiengine" target="_blank" rel="noopener" title="Sponsor on Open Collective" class="md-social__link">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><title>Open Collective</title><path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12c2.54 0 4.894-.79 6.834-2.135l-3.107-3.109a7.715 7.715 0 1 1 0-13.512l3.107-3.109A11.94 11.94 0 0 0 12 0m9.865 5.166-3.109 3.107A7.7 7.7 0 0 1 19.715 12a7.7 7.7 0 0 1-.959 3.727l3.109 3.107A11.94 11.94 0 0 0 24 12c0-2.54-.79-4.894-2.135-6.834"></path></svg>
      </a>
      <a href="https://github.com/gurkenlabs/litiengine" target="_blank" rel="noopener" title="GitHub" class="md-social__link">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><title>GitHub</title><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"></path></svg>
      </a>
      <a href="https://discord.gg/9TqCq9C" target="_blank" rel="noopener" title="Discord Community" class="md-social__link">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><title>Discord</title><path d="M20.317 4.37a19.8 19.8 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.3 18.3 0 0 0-5.487 0 13 13 0 0 0-.617-1.25.08.08 0 0 0-.079-.037A19.7 19.7 0 0 0 3.677 4.37a.1.1 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.08.08 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.08.08 0 0 0 .084-.028c.462-.63.874-1.295 1.226-1.994a.076.076 0 0 0-.041-.106 13 13 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10 10 0 0 0 .372-.292.07.07 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.07.07 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.3 12.3 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.08.08 0 0 0 .084.028 19.8 19.8 0 0 0 6.002-3.03.08.08 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.06.06 0 0 0-.031-.03M8.02 15.33c-1.182 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418m7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418"></path></svg>
      </a>
    `;
    footer.insertBefore(socialDiv, footer.firstChild);
  }

  function initShortcuts() {
    document.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        const searchInput = document.getElementById('search-input') || document.getElementById('page-search-input');
        if (searchInput) {
          searchInput.focus();
          searchInput.select();
        }
      }
    });
  }

  function updateTocHeader() {
    document.querySelectorAll('nav.toc').forEach(tocNav => {
      const filterInput = tocNav.querySelector('input.filter-input');
      if (filterInput) {
        filterInput.setAttribute('placeholder', 'Filter');
      }
    });
  }

  function initTocHighlight() {
    updateTocHeader();

    const tocLinks = document.querySelectorAll('ol.toc-list a');
    if (!tocLinks.length) return;

    const headings = [];
    tocLinks.forEach(link => {
      const href = link.getAttribute('href');
      if (href && href.startsWith('#') && href.length > 1) {
        const target = document.getElementById(href.substring(1));
        if (target) {
          headings.push({ link, target });
        }
      }
    });

    if (!headings.length) return;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          tocLinks.forEach(l => l.classList.remove('active-toc'));
          const match = headings.find(h => h.target === entry.target);
          if (match) {
            match.link.classList.add('active-toc');
          }
        }
      });
    }, {
      rootMargin: '-80px 0px -70% 0px'
    });

    headings.forEach(h => observer.observe(h.target));
  }

  function cleanInheritanceTree() {
    const rootInheritance = document.querySelector('main > div.inheritance');
    if (!rootInheritance) return;

    // Collect chain of nodes from root down to leaf
    const items = [];
    let current = rootInheritance;
    while (current) {
      const link = Array.from(current.children).find(el => el.tagName === 'A');
      if (link) {
        const fullText = link.textContent.trim();
        const isObject = link.href.includes('Object.html') || fullText === 'java.lang.Object' || fullText === 'Object';
        const lastDot = fullText.lastIndexOf('.');
        const pkg = lastDot !== -1 ? fullText.substring(0, lastDot) : '';
        const name = lastDot !== -1 ? fullText.substring(lastDot + 1) : fullText;

        items.push({
          isObject,
          isLink: true,
          pkg,
          name,
          href: link.getAttribute('href'),
          title: link.getAttribute('title') || ''
        });
      } else {
        let raw = '';
        current.childNodes.forEach(n => {
          if (n.nodeType === Node.TEXT_NODE) raw += n.textContent;
        });
        raw = raw.trim();
        if (raw) {
          const isObject = raw.includes('java.lang.Object') || raw === 'Object';
          const lastDot = raw.lastIndexOf('.');
          const pkg = lastDot !== -1 ? raw.substring(0, lastDot) : '';
          const name = lastDot !== -1 ? raw.substring(lastDot + 1) : raw;

          items.push({
            isObject,
            isLink: false,
            pkg,
            name
          });
        }
      }

      current = Array.from(current.children).find(el => el.classList.contains('inheritance'));
    }

    // Filter out Object nodes
    const validItems = items.filter(item => !item.isObject);

    // If only the class itself is left (0 custom superclasses), remove the inheritance container
    if (validItems.length <= 1) {
      rootInheritance.remove();
      return;
    }

    function formatPkg(pkg) {
      if (pkg.startsWith('de.gurkenlabs.litiengine.')) {
        return pkg.replace('de.gurkenlabs.litiengine.', '');
      } else if (pkg === 'de.gurkenlabs.litiengine') {
        return 'litiengine';
      }
      return pkg;
    }

    const treeRoot = document.createElement('ul');
    treeRoot.className = 'inheritance-tree';

    let currentParentUl = treeRoot;
    validItems.forEach((item, idx) => {
      const li = document.createElement('li');
      li.className = 'tree-node';

      if (item.pkg) {
        const badge = document.createElement('span');
        badge.className = 'tree-pkg-badge';
        badge.title = item.pkg;
        badge.textContent = formatPkg(item.pkg);
        li.appendChild(badge);
      }

      if (item.isLink) {
        const a = document.createElement('a');
        a.href = item.href;
        if (item.title) a.title = item.title;
        a.textContent = item.name;
        li.appendChild(a);
      } else {
        const label = document.createElement('span');
        label.className = 'type-name-label';
        label.textContent = item.name;
        li.appendChild(label);
      }

      currentParentUl.appendChild(li);

      // If not the last item, prepare nested <ul> for the next level
      if (idx < validItems.length - 1) {
        const nestedUl = document.createElement('ul');
        li.appendChild(nestedUl);
        currentParentUl = nestedUl;
      }
    });

    rootInheritance.parentNode.replaceChild(treeRoot, rootInheritance);
  }

  function enhanceBreadcrumbs() {
    const subNavList = document.querySelector('ol.sub-nav-list');
    if (!subNavList) return;

    const items = Array.from(subNavList.querySelectorAll('li'));
    if (!items.length) return;

    let typeChar = 'C';
    const titleEl = document.querySelector('.header .title') || document.querySelector('h1.title');
    if (titleEl) {
      const titleText = titleEl.textContent.trim();
      if (titleText.startsWith('Interface ')) typeChar = 'I';
      else if (titleText.startsWith('Enum ')) typeChar = 'E';
      else if (titleText.startsWith('Record ')) typeChar = 'R';
      else if (titleText.startsWith('Annotation ')) typeChar = '@';
    }

    const cubeSvg = `<svg class="breadcrumb-icon pkg-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m7.5 4.27 9 5.15"/><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/></svg>`;
    const folderSvg = `<svg class="breadcrumb-icon folder-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.93a2 2 0 0 1-1.66-.9l-.82-1.2A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13c0 1.1.9 2 2 2Z"/></svg>`;

    items.forEach((li, index) => {
      if (li.querySelector('.breadcrumb-icon') || li.querySelector('.breadcrumb-type-badge')) return;

      const link = li.querySelector('a');
      const isCurrent = li.classList.contains('current-selection') || (link && link.classList.contains('current-selection')) || index === items.length - 1;

      if (index === 0) {
        // Package level -> Cube icon
        const iconSpan = document.createElement('span');
        iconSpan.innerHTML = cubeSvg;
        li.insertBefore(iconSpan.firstChild, li.firstChild);
      } else if (isCurrent) {
        // Leaf type level -> Type badge [C], [I], etc.
        const badge = document.createElement('span');
        badge.className = 'breadcrumb-type-badge';
        badge.textContent = typeChar;
        li.insertBefore(badge, li.firstChild);
      } else {
        // Intermediate level (enclosing class or module) -> Folder icon
        const iconSpan = document.createElement('span');
        iconSpan.innerHTML = folderSvg;
        li.insertBefore(iconSpan.firstChild, li.firstChild);
      }
    });
  }

  function setupSeoAndGeo() {
    const head = document.head || document.getElementsByTagName('head')[0];
    if (!head) return;

    const pageTitle = document.title || 'LITIENGINE API';
    const headingEl = document.querySelector('h1.title, .header h1, h1');
    const headingText = headingEl ? headingEl.textContent.trim() : pageTitle.replace(/\\s*\\(.*?\\)\\s*$/, '');

    // Extract first doc block description if present
    const descEl = document.querySelector('.class-description .block, .package-description .block, .module-description .block, main .block');
    let pageDesc = descEl ? descEl.textContent.trim().replace(/\\s+/g, ' ') : '';
    if (pageDesc.length > 200) {
      pageDesc = pageDesc.substring(0, 197) + '...';
    }
    if (!pageDesc) {
      pageDesc = `Official Java API reference documentation for ${headingText} in the LITIENGINE 2D Game Engine. Classes, methods, and architecture.`;
    }

    function setMeta(name, content, isProperty = false) {
      if (!content) return;
      const attr = isProperty ? 'property' : 'name';
      let meta = head.querySelector(`meta[${attr}="${name}"]`);
      if (!meta) {
        meta = document.createElement('meta');
        meta.setAttribute(attr, name);
        head.appendChild(meta);
      }
      meta.setAttribute('content', content);
    }

    // Standard SEO Meta Tags
    setMeta('description', pageDesc);
    setMeta('keywords', `LITIENGINE, Java 2D Game Engine, Game Development, ${headingText}, JavaDoc, API Reference, Gurkenlabs, Java Game Engine`);
    setMeta('author', 'GURKENLABS');
    setMeta('robots', 'index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1');
    setMeta('generator', 'Java 25 Javadoc with LITIENGINE Theme');
    setMeta('application-name', 'LITIENGINE Docs');

    // Theme color
    setMeta('theme-color', '#0b0c0f');

    // Open Graph
    setMeta('og:title', `${headingText} | LITIENGINE API`, true);
    setMeta('og:description', pageDesc, true);
    setMeta('og:type', 'article', true);
    setMeta('og:site_name', 'LITIENGINE API Documentation', true);
    setMeta('og:image', 'https://gurkenlabs.github.io/litiengine-docs/assets/logo.png', true);

    // Twitter Card
    setMeta('twitter:card', 'summary');
    setMeta('twitter:title', `${headingText} | LITIENGINE API`);
    setMeta('twitter:description', pageDesc);
    setMeta('twitter:image', 'https://gurkenlabs.github.io/litiengine-docs/assets/logo.png');
    setMeta('twitter:site', '@gurkenlabs');

    // Canonical link
    if (!head.querySelector('link[rel="canonical"]')) {
      const canonical = document.createElement('link');
      canonical.rel = 'canonical';
      canonical.href = window.location.href.split('#')[0].split('?')[0];
      head.appendChild(canonical);
    }

    // Enhanced Semantic Landmarks & ARIA
    const main = document.querySelector('main');
    if (main && !main.id) {
      main.id = 'main-content';
    }
    const breadcrumbNav = document.querySelector('ol.sub-nav-list');
    if (breadcrumbNav && !breadcrumbNav.getAttribute('aria-label')) {
      breadcrumbNav.setAttribute('aria-label', 'Breadcrumbs');
    }

    // Structured Data (JSON-LD) for Search Engines & Generative Engine Optimization (GEO)
    let jsonLdScript = document.getElementById('liti-schema-jsonld');
    if (!jsonLdScript) {
      jsonLdScript = document.createElement('script');
      jsonLdScript.id = 'liti-schema-jsonld';
      jsonLdScript.type = 'application/ld+json';
      head.appendChild(jsonLdScript);
    }

    const breadcrumbItems = [];
    const breadcrumbLis = document.querySelectorAll('ol.sub-nav-list li');
    breadcrumbLis.forEach((li, index) => {
      const a = li.querySelector('a');
      const name = li.textContent.replace(/[›\s]+/g, ' ').trim();
      if (name) {
        breadcrumbItems.push({
          '@type': 'ListItem',
          'position': index + 1,
          'name': name,
          'item': a ? a.href : window.location.href
        });
      }
    });

    const schemaGraph = [
      {
        '@type': 'WebSite',
        '@id': 'https://litiengine.com/#website',
        'url': 'https://litiengine.com/',
        'name': 'LITIENGINE',
        'description': 'Free and open source 2D Java Game Engine',
        'publisher': {
          '@type': 'Organization',
          'name': 'GURKENLABS',
          'url': 'https://litiengine.com/'
        }
      },
      {
        '@type': 'TechArticle',
        '@id': window.location.href + '#article',
        'headline': headingText,
        'description': pageDesc,
        'inLanguage': 'en-US',
        'mainEntityOfPage': window.location.href,
        'keywords': ['LITIENGINE', 'Java', 'Game Engine', '2D Games', headingText],
        'author': {
          '@type': 'Organization',
          'name': 'GURKENLABS',
          'url': 'https://litiengine.com/'
        },
        'publisher': {
          '@type': 'Organization',
          'name': 'GURKENLABS',
          'url': 'https://litiengine.com/'
        },
        'about': {
          '@type': 'SoftwareSourceCode',
          'name': 'LITIENGINE',
          'programmingLanguage': 'Java',
          'codeRepository': 'https://github.com/gurkenlabs/litiengine'
        }
      }
    ];

    if (breadcrumbItems.length > 0) {
      schemaGraph.push({
        '@type': 'BreadcrumbList',
        '@id': window.location.href + '#breadcrumb',
        'itemListElement': breadcrumbItems
      });
    }

    jsonLdScript.textContent = JSON.stringify({
      '@context': 'https://schema.org',
      '@graph': schemaGraph
    }, null, 2);
  }

  function setupPageTags() {
    try {
      const main = document.querySelector('main');
      if (!main || main.querySelector('.md-tags')) return;

      const header = main.querySelector('.header, div.header');
      const tags = new Set();
      const titleEl = header ? header.querySelector('h1') : null;
      const titleText = titleEl ? titleEl.textContent.trim() : (document.title || '');

      // 1. Detect Type Category
      if (document.body.classList.contains('class-declaration-page')) {
        if (titleText.startsWith('Interface ')) tags.add('interface');
        else if (titleText.startsWith('Enum Class ') || titleText.startsWith('Enum ')) tags.add('enum');
        else if (titleText.startsWith('Record Class ') || titleText.startsWith('Record ')) tags.add('record');
        else if (titleText.startsWith('Annotation Interface ')) tags.add('annotation');
        else tags.add('class');
      } else if (document.body.classList.contains('package-declaration-page')) {
        tags.add('package');
      } else if (document.body.classList.contains('tree-page') || document.body.classList.contains('package-tree-page')) {
        tags.add('hierarchy');
        tags.add('tree');
      } else if (document.body.classList.contains('index-page')) {
        tags.add('index');
        tags.add('api');
      } else if (document.body.classList.contains('search-page')) {
        tags.add('search');
      } else if (document.body.classList.contains('all-classes-index-page')) {
        tags.add('all-classes');
      } else if (document.body.classList.contains('all-packages-index-page')) {
        tags.add('all-packages');
      }

      // 2. Detect Package Subsystem
      const subNavPackageLink = document.querySelector('ol.sub-nav-list li a[href*="package-summary.html"]');
      if (subNavPackageLink) {
        const pkgText = subNavPackageLink.textContent.trim();
        const pkgParts = pkgText.split('.');
        const subPkg = pkgParts[pkgParts.length - 1];
        if (subPkg && subPkg !== 'litiengine') {
          tags.add(subPkg);
        }
      }

      // 3. Domain-specific tags based on class name & exact package directory
      const path = window.location.pathname.toLowerCase();
      if (titleText.endsWith('Listener')) tags.add('listener');
      if (titleText.endsWith('Event')) tags.add('event');
      if (titleText.endsWith('Controller')) tags.add('controller');
      if (path.includes('/sound/') || titleText.includes('Sound') || titleText.includes('Track')) tags.add('audio');
      if (path.includes('/graphics/') || titleText.includes('Sprite') || titleText.includes('Render') || titleText.includes('Emitter')) tags.add('graphics');
      if (path.includes('/physics/') || titleText.includes('Collision') || titleText.includes('Raycast')) tags.add('physics');
      if (path.includes('/input/') || titleText.includes('Gamepad') || titleText.includes('Keyboard') || titleText.includes('Mouse')) tags.add('input');
      if (path.includes('/gui/') || titleText.includes('Menu') || titleText.includes('Component') || titleText.includes('Gui')) tags.add('gui');
      if (path.includes('/abilities/') || titleText.includes('Ability') || titleText.includes('Effect')) tags.add('abilities');
      if (path.includes('/tweening/') || titleText.includes('Tween')) tags.add('animation');
      if (path.includes('/entities/') || titleText.includes('Creature') || titleText.includes('Entity')) tags.add('entities');
      if (path.includes('/environment/') || titleText.includes('Map') || titleText.includes('Tile')) tags.add('maps');
      if (path.includes('/configuration/') || titleText.includes('Config')) tags.add('configuration');

      // 4. Deprecated tag
      if (document.querySelector('.deprecation-block, .deprecated-label, .deprecated')) {
        tags.add('deprecated');
      }

      // 5. Core identity tags
      if (tags.size < 4) {
        tags.add('api');
      }
      if (tags.size < 5) {
        tags.add('java');
      }

      if (!tags.size) return;

      const nav = document.createElement('nav');
      nav.className = 'md-tags';
      nav.setAttribute('aria-label', 'Tags');

      tags.forEach(t => {
        const span = document.createElement('span');
        span.className = t === 'deprecated' ? 'md-tag md-tag--deprecated' : 'md-tag';
        span.textContent = t;
        nav.appendChild(span);
      });

      main.appendChild(nav);
    } catch (e) {}
  }

  function init() {
    try {
      setupFavicons();
      setupSeoAndGeo();
      setupTwoLevelNavigation();
      initShortcuts();
      initTocHighlight();
      setupPageTags();
      setupStickyIndexNav();
      setupPageSearchInput();
      cleanHierarchyTree();
      cleanInheritanceTree();
      enhanceBreadcrumbs();
      setupFooter();
    } catch (e) {
      console.warn('LITIENGINE Javadoc theme init error:', e);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
"""

open('config/javadoc/litiengine.js', 'w', encoding='utf-8').write(js_code)
print("Updated config/javadoc/litiengine.js successfully")
