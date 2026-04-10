---
name: frontend-page
description: Generate clean, modern frontend pages with HTML/CSS/JS. Supports dashboards, landing pages, forms, admin panels, and API testing consoles. Use when user says "写前端页面", "create frontend", "build UI", "生成页面", or wants to create web interfaces.
argument-hint: [page-description-or-requirements]
allowed-tools: Bash(*), Read, Write, Edit, Glob, Grep, WebSearch, WebFetch
---

# Frontend Page Generator

Generate clean, modern, and responsive frontend pages based on user requirements.

## Scope: What This Skill Can and Cannot Do

| Category | Can auto-generate? | Examples |
|----------|-------------------|----------|
| **Dashboard pages** | ✅ Yes | Admin dashboards, analytics panels, monitoring consoles |
| **API testing pages** | ✅ Yes | REST API testers, GraphQL playgrounds, WebSocket consoles |
| **Landing pages** | ✅ Yes | Product landing, marketing pages, hero sections |
| **Form pages** | ✅ Yes | Contact forms, login/register, multi-step wizards |
| **Data tables** | ✅ Yes | CRUD interfaces, sortable tables, data grids |
| **Documentation pages** | ✅ Yes | API docs, component showcases, style guides |
| **Complex SPAs** | ❌ No — use framework | React/Vue/Angular applications with routing/state |
| **Backend logic** | ❌ No | Server-side code, database operations, authentication |
| **Mobile apps** | ❌ No | React Native, Flutter, native iOS/Android |

**In practice:** This skill generates standalone HTML pages with embedded CSS/JS. For complex applications, use this as a starting point and integrate with your preferred framework.

## Constants

- **STYLE = `modern`** — Visual style preset. Options: `modern` (clean, minimal), `dark` (dark theme), `corporate` (professional), `playful` (colorful)
- **FRAMEWORK = `vanilla`** — No external framework. Pure HTML/CSS/JS
- **CSS_APPROACH = `embedded`** — CSS embedded in `<style>` tag. Options: `embedded`, `tailwind-cdn`, `external-file`
- **JS_APPROACH = `embedded`** — JS embedded in `<script>` tag. Options: `embedded`, `external-file`
- **FONT = `Inter`** — Default font family from Google Fonts
- **ICON_SET = `emoji`** — Icon approach. Options: `emoji`, `heroicons`, `lucide`, `font-awesome`

## Design Principles

### 1. Clean & Minimal
- Generous whitespace (padding: 16-24px)
- Clear visual hierarchy
- Consistent spacing (4px grid system)
- Maximum 3-4 colors per page

### 2. Modern Aesthetics
- Subtle gradients (not rainbow)
- Soft shadows (0 4px 12px rgba)
- Rounded corners (8-12px)
- Smooth transitions (200-300ms)

### 3. Responsive Design
- Mobile-first approach
- Breakpoints: 640px, 768px, 1024px, 1280px
- Flexible grids (CSS Grid, Flexbox)
- Touch-friendly targets (min 44px)

### 4. Accessibility
- Color contrast 4.5:1 minimum
- Focus states visible
- Semantic HTML elements
- ARIA labels where needed

## Color Palettes

### Modern Dark (Default)
```css
--primary: #6366f1;      /* Indigo */
--secondary: #8b5cf6;    /* Purple */
--success: #10b981;      /* Emerald */
--warning: #f59e0b;      /* Amber */
--error: #ef4444;        /* Red */
--bg: #0f172a;           /* Dark blue */
--bg-card: #1e293b;      /* Card background */
--text: #f1f5f9;         /* Light text */
--text-muted: #94a3b8;   /* Muted text */
```

### Modern Light
```css
--primary: #6366f1;
--secondary: #8b5cf6;
--success: #10b981;
--warning: #f59e0b;
--error: #ef4444;
--bg: #f8fafc;
--bg-card: #ffffff;
--text: #1e293b;
--text-muted: #64748b;
```

### Corporate
```css
--primary: #2563eb;      /* Blue */
--secondary: #0ea5e9;    /* Sky */
--success: #059669;      /* Green */
--bg: #f1f5f9;
--bg-card: #ffffff;
--text: #0f172a;
```

## Page Types Reference

| Type | Structure | Key Components |
|------|-----------|----------------|
| **Dashboard** | Sidebar + Main content | Stats cards, charts, tables |
| **API Console** | Sidebar + API cards | Method badges, forms, response panels |
| **Landing Page** | Hero + Features + CTA | Gradient hero, feature grid, testimonials |
| **Form Page** | Form container | Input groups, validation, submit button |
| **Data Table** | Header + Table + Pagination | Search, filters, sortable columns |
| **Documentation** | Sidebar + Content | Navigation, code blocks, examples |

## Workflow

### Step 1: Understand Requirements

Parse the input: **$ARGUMENTS**

1. Identify page type (dashboard, landing, form, API console, etc.)
2. Determine style preference (dark/light, modern/corporate)
3. List required components (forms, tables, cards, etc.)
4. Identify data sources (API endpoints, static data)
5. Plan responsive behavior

### Step 2: Select Page Structure

Based on page type, select appropriate structure:

**Dashboard:**
```
┌─────────────────────────────────────────────┐
│  Header (logo, search, user menu)           │
├──────────┬──────────────────────────────────┤
│ Sidebar  │  Stats Cards (row)               │
│ - Nav 1  │  ┌────────┐ ┌────────┐          │
│ - Nav 2  │  │ Card 1 │ │ Card 2 │          │
│ - Nav 3  │  └────────┘ └────────┘          │
│          │  Main Content Area               │
│          │  (charts, tables, etc.)          │
└──────────┴──────────────────────────────────┘
```

**API Console:**
```
┌─────────────────────────────────────────────┐
│  Header (title, actions)                    │
├──────────┬──────────────────────────────────┤
│ Sidebar  │  API Card Grid                   │
│ - Group 1│  ┌─────────────────────────────┐ │
│ - Group 2│  │ [GET] /api/endpoint         │ │
│ - Group 3│  │ Form inputs...              │ │
│          │  │ [Execute] [Clear]           │ │
│          │  │ Response panel              │ │
│          │  └─────────────────────────────┘ │
└──────────┴──────────────────────────────────┘
```

**Landing Page:**
```
┌─────────────────────────────────────────────┐
│  Navigation (logo, links, CTA button)       │
├─────────────────────────────────────────────┤
│  Hero Section                               │
│  (gradient background, headline, CTA)       │
├─────────────────────────────────────────────┤
│  Features Grid                              │
│  ┌────────┐ ┌────────┐ ┌────────┐          │
│  │Feature1│ │Feature2│ │Feature3│          │
│  └────────┘ └────────┘ └────────┘          │
├─────────────────────────────────────────────┤
│  CTA Section / Footer                       │
└─────────────────────────────────────────────┘
```

### Step 3: Generate HTML Structure

Create semantic HTML with proper structure:

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Page Title</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        /* CSS Variables */
        :root {
            --primary: #6366f1;
            /* ... */
        }
        
        /* Base styles */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; }
        
        /* Component styles */
        /* ... */
    </style>
</head>
<body>
    <!-- Page structure -->
    
    <script>
        // JavaScript functionality
    </script>
</body>
</html>
```

### Step 4: Add CSS Styling

Apply modern styling with CSS variables:

```css
/* Card component */
.card {
    background: var(--bg-card);
    border-radius: 12px;
    border: 1px solid var(--border);
    padding: 20px;
    transition: all 0.2s;
}

.card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

/* Button component */
.btn {
    padding: 10px 20px;
    border-radius: 8px;
    border: none;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
}

.btn-primary {
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
}

.btn-primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}
```

### Step 5: Add JavaScript Functionality

Implement interactive features:

```javascript
// API fetch wrapper
async function fetchApi(url, options = {}) {
    try {
        const response = await fetch(url, options);
        const data = await response.json();
        return { success: true, data, status: response.status };
    } catch (error) {
        return { success: false, error: error.message };
    }
}

// Toast notification
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => toast.classList.remove('show'), 3000);
}

// Form handling
function handleFormSubmit(formId, callback) {
    document.getElementById(formId).addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        await callback(Object.fromEntries(formData));
    });
}
```

### Step 6: Add Responsive Design

Include responsive breakpoints:

```css
/* Mobile-first base styles */
.sidebar {
    display: none;
}

/* Tablet and up */
@media (min-width: 768px) {
    .sidebar {
        display: block;
        width: 240px;
    }
}

/* Desktop */
@media (min-width: 1024px) {
    .sidebar {
        width: 280px;
    }
    
    .api-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}
```

### Step 7: Quality Checklist

Before finishing, verify:

- [ ] Page renders correctly in browser
- [ ] Responsive at 375px, 768px, 1024px, 1440px
- [ ] All interactive elements have hover/focus states
- [ ] Color contrast meets WCAG AA (4.5:1)
- [ ] No console errors
- [ ] Forms have proper labels and placeholders
- [ ] Buttons have cursor: pointer
- [ ] Transitions are smooth (200-300ms)
- [ ] No hardcoded colors (use CSS variables)
- [ ] Semantic HTML elements used

## Output

```
output/
├── index.html          # Main page file (self-contained)
└── README.md           # Usage instructions (optional)
```

## Common Patterns

### API Testing Card
```html
<div class="api-card">
    <div class="api-card-header">
        <span class="method-badge get">GET</span>
        <h3>Endpoint Name</h3>
        <span class="endpoint">/api/path</span>
    </div>
    <div class="api-card-body">
        <div class="form-group">
            <label>Parameter</label>
            <input type="text" id="param1" value="default">
        </div>
    </div>
    <div class="api-card-footer">
        <button class="btn-execute" onclick="executeApi()">Execute</button>
    </div>
    <div class="response-panel" id="response" style="display:none;">
        <pre></pre>
    </div>
</div>
```

### Stats Card
```html
<div class="stat-card">
    <div class="label">Metric Name</div>
    <div class="value">1,234</div>
    <div class="change positive">+12% from last week</div>
</div>
```

### Navigation Item
```html
<div class="nav-item active" onclick="showSection('dashboard')">
    <span class="icon">📊</span>
    <span>Dashboard</span>
</div>
```

## Key Rules

1. **Single HTML file** — All CSS and JS embedded for portability
2. **CSS variables** — Define colors, spacing in `:root`
3. **Semantic HTML** — Use `<header>`, `<nav>`, `<main>`, `<section>`, `<aside>`
4. **Accessible forms** — Labels, placeholders, error messages
5. **Responsive grids** — Use CSS Grid for layouts, Flexbox for alignment
6. **No inline styles** — Except for dynamic values from JS
7. **Smooth transitions** — All interactive elements should have transitions
8. **Loading states** — Show loading indicators for async operations
9. **Error handling** — Graceful error messages, not alerts
10. **Toast notifications** — For success/error feedback

## Example Prompts

```
"创建一个API测试控制台，用于测试REST API"
"Build a dark theme dashboard with stats cards"
"生成一个登录页面，包含用户名和密码输入"
"Create a landing page for a SaaS product"
"写一个数据表格页面，支持搜索和排序"
```

---

User requirements: $ARGUMENTS
