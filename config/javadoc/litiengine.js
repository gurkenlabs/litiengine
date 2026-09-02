/**
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
  
  const OFFICIAL_LOGO_SRC = 'data:image/png;base64,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';

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
      if (!navList) return;

      const navListSearch = document.querySelector('.nav-list-search');

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
          const isMac = navigator.platform && navigator.platform.toUpperCase().indexOf('MAC') >= 0;
          kbd.textContent = isMac ? '⌘K' : 'Ctrl+K';
          navListSearch.appendChild(kbd);
        }

        level1Right.appendChild(navListSearch);
      } else {
        const searchCapsule = document.createElement('div');
        searchCapsule.className = 'nav-list-search';
        const isMac = navigator.platform && navigator.platform.toUpperCase().indexOf('MAC') >= 0;
        searchCapsule.innerHTML = `<span class="search-icon-wrapper">${SEARCH_SVG}</span><input type="text" id="search-input" placeholder="Search"><kbd class="search-kbd">${isMac ? '⌘K' : 'Ctrl+K'}</kbd>`;
        const headerInput = searchCapsule.querySelector('#search-input');
        if (headerInput) {
          headerInput.addEventListener('focus', function () {
            const pageInput = document.getElementById('page-search-input');
            if (pageInput) {
              pageInput.focus();
              pageInput.select();
            }
          });
        }
        level1Right.appendChild(searchCapsule);
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
      const typeParamMatch = text.match(/^(\s*<[^>]+>\s*)/);
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
        text = text.replace(/\s*\(implements\s*/, '');
      } else if (text.includes('(also extends ')) {
        const span = document.createElement('span');
        span.className = 'tree-impl-tag';
        span.textContent = 'extends';
        fragment.appendChild(span);
        text = text.replace(/\s*\(also extends\s*/, '');
      } else if (text.includes('(extends ')) {
        const span = document.createElement('span');
        span.className = 'tree-impl-tag';
        span.textContent = 'extends';
        fragment.appendChild(span);
        text = text.replace(/\s*\(extends\s*/, '');
      } else if (/^\s*,\s*/.test(text)) {
        const span = document.createElement('span');
        span.className = 'tree-impl-sep';
        span.textContent = ',';
        fragment.appendChild(span);
        text = text.replace(/^\s*,\s*/, '');
      }

      // 3. Clean trailing paren or whitespace
      text = text.replace(/\)\s*$/, '').trim();

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
        const searchInput = document.getElementById('page-search-input') || document.getElementById('search-input');
        if (searchInput) {
          searchInput.focus();
          searchInput.select();
        }
      }
    });
  }

  function updateTocHeader() {
    try {
      document.querySelectorAll('nav.toc').forEach(tocNav => {
        const tocHeader = tocNav.querySelector('.toc-header');
        if (tocHeader && !tocHeader.querySelector('.toc-filter-wrapper')) {
          const filterInput = tocHeader.querySelector('input.filter-input');
          if (filterInput) {
            filterInput.setAttribute('placeholder', 'Filter');

            const wrapper = document.createElement('div');
            wrapper.className = 'toc-filter-wrapper';

            const iconSpan = document.createElement('span');
            iconSpan.innerHTML = SEARCH_SVG;

            tocHeader.appendChild(wrapper);
            wrapper.appendChild(iconSpan.firstChild);
            wrapper.appendChild(filterInput);

            const resetBtn = tocHeader.querySelector('input.reset-filter');
            if (resetBtn) {
              wrapper.appendChild(resetBtn);
            }
          }
        }
      });
    } catch (e) {}
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

  function init() {
    try {
      setupTwoLevelNavigation();
      initShortcuts();
      initTocHighlight();
      setupStickyIndexNav();
      setupPageSearchInput();
      cleanHierarchyTree();
      cleanInheritanceTree();
      enhanceBreadcrumbs();
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
