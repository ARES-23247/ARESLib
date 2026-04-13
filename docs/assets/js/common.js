/* ===== ARESLib Shared Navigation & Footer Injection ===== */

document.addEventListener('DOMContentLoaded', () => {
    injectHeader();
    injectFooter();
    injectStars();
});

function injectHeader() {
    const headerPlaceholder = document.getElementById('header-placeholder');
    if (!headerPlaceholder) return;

    const isSubdir = window.location.pathname.includes('/tutorials/');
    const basePath = isSubdir ? '../' : '';
    const editUrl = getEditUrl();
    
    const activePage = window.location.pathname.split('/').pop() || 'index.html';

    headerPlaceholder.innerHTML = `
        <header class="nav-header">
            <nav>
                <a href="${basePath}index.html" class="logo">ARESLib<span>2</span></a>
                <div class="nav-links">
                    <a href="${basePath}index.html" class="${activePage === 'index.html' ? 'active' : ''}">Home</a>
                    <a href="${basePath}standards.html" class="${activePage === 'standards.html' ? 'active' : ''}">Standards</a>
                    <a href="${basePath}tutorials/index.html" class="${isSubdir ? 'active' : ''}">Tutorials</a>
                    <a href="${basePath}javadoc/index.html">API</a>
                    <a href="${editUrl}" target="_blank" class="nav-edit">Edit Page</a>
                </div>
            </nav>
        </header>
    `;
}

function injectFooter() {
    const footerPlaceholder = document.getElementById('footer-placeholder');
    if (!footerPlaceholder) return;

    footerPlaceholder.innerHTML = `
        <footer>
            <p>&copy; 2026 ARESLib Framework - Built for Einstein. Inspired by MARSLib.</p>
            <div style="margin-top: 1rem; opacity: 0.6; font-size: 0.8rem;">
                <a href="https://github.com/ARES-23247/ARESLib" style="color: inherit; margin: 0 10px;">GitHub</a>
                <a href="${getEditUrl()}" style="color: inherit; margin: 0 10px;">Wiki Edit</a>
            </div>
        </footer>
    `;
}

function getEditUrl() {
    const repoRoot = "https://github.com/ARES-23247/ARESLib/edit/master/docs/";
    let path = window.location.pathname.split('/').pop();
    
    // Handle subdirectories
    const segments = window.location.pathname.split('/');
    const isTutorial = segments.includes('tutorials');
    
    if (!path || path === "" || path === "docs") {
        path = "index.html";
    }

    const finalPath = isTutorial ? `tutorials/${path}` : path;
    return repoRoot + finalPath;
}

function injectStars() {
    // Check if stars container already exists (from HTML)
    let starsContainer = document.getElementById('stars-container');
    if (!starsContainer) {
        starsContainer = document.createElement('div');
        starsContainer.id = 'stars-container';
        document.body.prepend(starsContainer);
    }

    const starCount = 150;
    for (let i = 0; i < starCount; i++) {
        const star = document.createElement('div');
        star.className = 'star';
        
        const x = Math.random() * 100;
        const y = Math.random() * 100;
        const duration = 2 + Math.random() * 3;
        const delay = Math.random() * 5;
        const size = 1 + Math.random() * 2;

        star.style.left = `${x}%`;
        star.style.top = `${y}%`;
        star.style.width = `${size}px`;
        star.style.height = `${size}px`;
        star.style.setProperty('--duration', `${duration}s`);
        star.style.animationDelay = `${delay}s`;

        starsContainer.appendChild(star);
    }
}
