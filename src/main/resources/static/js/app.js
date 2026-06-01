/* ClientServe — Global JS */

// ── Toast ──────────────────────────────────────────────────────────────────
function showToast(msg, type='ok') {
  const icons = {
    ok:   `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>`,
    err:  `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>`,
    info: `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`
  };
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.innerHTML = icons[type] + `<span>${msg}</span>`;
  document.body.appendChild(t);
  setTimeout(() => { t.style.opacity='0'; t.style.transform='translateY(-10px)'; t.style.transition='all .4s'; setTimeout(()=>t.remove(),400); }, 3500);
}

// Auto-dismiss server-rendered toasts
document.querySelectorAll('.toast').forEach(t => {
  setTimeout(() => { t.style.opacity='0'; t.style.transform='translateY(-10px)'; t.style.transition='all .4s'; setTimeout(()=>t.remove(),400); }, 3500);
});

// ── Notification bell ──────────────────────────────────────────────────────
function initBell() {
  const btn = document.getElementById('bellBtn');
  const panel = document.getElementById('notifPanel');
  if (!btn || !panel) return;
  btn.addEventListener('click', e => { e.stopPropagation(); panel.classList.toggle('open'); });
  document.addEventListener('click', e => { if (!panel.contains(e.target) && e.target !== btn) panel.classList.remove('open'); });
}

// ── Filter tabs ────────────────────────────────────────────────────────────
function initFilterTabs(tableId) {
  let cur = 'ALL';
  window.setFilter = function(s, btn) {
    cur = s;
    document.querySelectorAll('.ftab').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    applyFilter();
  };
  window.applyFilter = function() {
    const q = (document.getElementById('tableSearch')?.value || '').toLowerCase();
    document.querySelectorAll(`#${tableId} tbody tr, #${tableId} .tc`).forEach(row => {
      const stOk = cur === 'ALL' || row.dataset.status === cur || (cur === 'UNASSIGNED' && row.dataset.assigned === 'no');
      const srOk = (row.dataset.search || '').toLowerCase().includes(q);
      row.style.display = stOk && srOk ? '' : 'none';
    });
  };
}

// ── Sidebar mobile toggle ──────────────────────────────────────────────────
function initSidebar() {
  const toggle = document.getElementById('sidebarToggle');
  const sidebar = document.getElementById('sidebar');
  if (!toggle || !sidebar) return;
  toggle.addEventListener('click', () => sidebar.classList.toggle('open'));
  document.addEventListener('click', e => {
    if (sidebar.classList.contains('open') && !sidebar.contains(e.target) && e.target !== toggle)
      sidebar.classList.remove('open');
  });
}

// ── Modal ──────────────────────────────────────────────────────────────────
function openModal(id)  { document.getElementById(id)?.classList.add('open'); }
function closeModal(id) { document.getElementById(id)?.classList.remove('open'); }
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('open');
});

// ── Star rating ────────────────────────────────────────────────────────────
function initStars() {
  document.querySelectorAll('.star').forEach(s => {
    s.addEventListener('click',       () => { document.getElementById('ratingVal').value = s.dataset.v; setStars(parseInt(s.dataset.v)); });
    s.addEventListener('mouseenter',  () => { document.querySelectorAll('.star').forEach(x => x.classList.toggle('hov', parseInt(x.dataset.v) <= parseInt(s.dataset.v))); });
    s.addEventListener('mouseleave',  () => document.querySelectorAll('.star').forEach(x => x.classList.remove('hov')));
  });
}
function setStars(v) { document.querySelectorAll('.star').forEach(s => s.classList.toggle('on', parseInt(s.dataset.v) <= v)); }

// ── Char counter ───────────────────────────────────────────────────────────
function initCharCount(textareaId, counterId, max) {
  const ta = document.getElementById(textareaId);
  const ct = document.getElementById(counterId);
  if (!ta || !ct) return;
  ct.textContent = `0/${max}`;
  ta.addEventListener('input', () => ct.textContent = `${ta.value.length}/${max}`);
}

// ── Init all ───────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  initBell();
  initSidebar();
  initStars();
});
