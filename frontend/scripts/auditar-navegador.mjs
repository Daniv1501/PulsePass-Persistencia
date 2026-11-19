import fs from 'node:fs'
import { artista, evento, principal, sede, ticket, usuario } from '../src/test/fixtures.ts'

// Requiere Chrome con --remote-debugging-port=9337 y npm run preview en 5174.
// Datos ficticios: verifica la UI sin modificar la base de datos ni guardar JWT reales.
const base = process.env.AUDIT_URL || 'http://127.0.0.1:5174'
const tabs = await (await fetch('http://127.0.0.1:9337/json')).json()
const ws = new WebSocket(tabs.find((t) => t.type === 'page').webSocketDebuggerUrl)
await new Promise((resolve) => ws.addEventListener('open', resolve, { once: true }))
let id = 0
const pending = new Map()
ws.addEventListener('message', (event) => {
  const message = JSON.parse(event.data)
  const call = pending.get(message.id)
  if (call) {
    pending.delete(message.id)
    if (message.error) call.reject(new Error(message.error.message))
    else call.resolve(message.result)
  }
})
const send = (method, params = {}) =>
  new Promise((resolve, reject) => {
    const key = ++id
    pending.set(key, { resolve, reject })
    ws.send(JSON.stringify({ id: key, method, params }))
  })
const evalua = async (expression) =>
  (
    await send('Runtime.evaluate', {
      expression,
      awaitPromise: true,
      returnByValue: true,
    })
  ).result.value
await send('Page.enable')
await send('Network.enable')
await send('Network.setCacheDisabled', { cacheDisabled: true })
await send('Emulation.setEmulatedMedia', {
  features: [{ name: 'prefers-reduced-motion', value: 'reduce' }],
})
const datos = JSON.stringify({ artista, evento, principal, sede, ticket, usuario })
await send('Page.addScriptToEvaluateOnNewDocument', {
  source: `
  const datos = ${datos};
  if (['/login','/registro'].includes(location.pathname)) sessionStorage.removeItem('pulsepass.sesion');
  else sessionStorage.setItem('pulsepass.sesion', JSON.stringify({token:'fixture-sin-valor-real',expiraEn:Date.now()+900000}));
  const originalFetch = window.fetch.bind(window);
  window.fetch = async (input, init) => {
    const url = new URL(typeof input === 'string' ? input : input.url, location.href);
    if (!url.pathname.startsWith('/api/')) return originalFetch(input, init);
    const p = url.pathname.replace('/api', '');
    let data;
    if(p === '/auth/me') data={...datos.principal, roles:['USER','STAFF','ADMIN']};
    else if(p.startsWith('/users/')) data=datos.usuario;
    else if(p === '/events/published' || p === '/events/by-artist') data=[datos.evento];
    else if(p.endsWith('/tickets/paid') || p === '/tickets/by-user') data=[datos.ticket];
    else if(p.startsWith('/events/')) data={...datos.evento,status: p.includes('DRAFT')?'DRAFT':'PUBLISHED'};
    else if(p === '/artists/active') data=[datos.artista];
    else if(p.startsWith('/artists/')) data=datos.artista;
    else if(p === '/venues/active') data=[datos.sede];
    else if(p.startsWith('/venues/')) data=datos.sede;
    else if(p.startsWith('/tickets/')) data=datos.ticket;
    return new Response(JSON.stringify(data),{status:200,headers:{'Content-Type':'application/json'}});
  };
`,
})
const axe = fs.readFileSync(new URL('../node_modules/axe-core/axe.min.js', import.meta.url), 'utf8')
const paths = [
  '/',
  '/eventos',
  '/eventos/CMF-2026',
  '/artistas',
  '/artistas/1',
  '/sedes',
  '/sedes/VEN-SMR-01',
  '/cuenta',
  '/mis-tickets',
  '/mis-tickets/TKT-8F2A1C',
  '/compra/confirmacion/TKT-8F2A1C',
  '/staff/validacion',
  '/staff/eventos/CMF-2026/tickets',
  '/admin/eventos',
  '/admin/eventos/nuevo',
  '/admin/eventos/DRAFT',
  '/403',
  '/ruta-inexistente',
  '/login',
  '/registro',
]
const results = []
const browser = await send('Browser.getVersion')
fs.mkdirSync(new URL('../docs/auditoria/', import.meta.url), { recursive: true })
for (const width of [360, 1440]) {
  await send('Emulation.setDeviceMetricsOverride', {
    width,
    height: 1000,
    deviceScaleFactor: 1,
    mobile: width === 360,
  })
  for (const path of paths) {
    await send('Page.navigate', { url: base + path })
    await new Promise((resolve) => setTimeout(resolve, 450))
    const ready = await evalua(
      `(async()=>{for(let i=0;i<50&&!document.querySelector('main h1');i++)await new Promise(r=>setTimeout(r,100));await document.fonts.ready;return location.pathname===${JSON.stringify(path)}&&!!document.querySelector('main h1')})()`,
    )
    if (!ready) throw new Error(`La pantalla no se cargó: ${path}`)
    // Medir también las secciones que el navegador difiere fuera de pantalla.
    await evalua(
      `document.querySelectorAll('.home-section,.spotlight,.festival-cta').forEach(e=>e.style.contentVisibility='visible')`,
    )
    const states = ['/login', '/registro', '/admin/eventos/nuevo'].includes(path)
      ? ['inicial', 'invalido']
      : ['inicial']
    for (const state of states) {
      if (state === 'invalido') {
        await evalua(`document.querySelector('main form button:not([type="button"])')?.click()`)
        await new Promise((resolve) => setTimeout(resolve, 250))
      }
      for (const theme of ['light', 'dark']) {
        await evalua(`document.documentElement.classList.toggle('dark',${theme === 'dark'})`)
        await new Promise((resolve) => setTimeout(resolve, 250))
        await evalua(axe)
        const result = await evalua(
          `(async()=>{const r=await axe.run(document,{runOnly:{type:'tag',values:['wcag2a','wcag2aa','wcag21aa']}});return {url:location.pathname,width:innerWidth,pageWidth:document.documentElement.scrollWidth,violations:r.violations.map(v=>({id:v.id,impact:v.impact,description:v.description,nodes:v.nodes.map(n=>({target:n.target,summary:n.failureSummary}))}))}})()`,
        )
        results.push({ requestedPath: path, state, theme, ...result })
        if (path === '/') {
          const screenshot = await send('Page.captureScreenshot', {
            format: 'png',
            captureBeyondViewport: false,
          })
          fs.writeFileSync(
            new URL(`../docs/auditoria/inicio-${width}-${theme}.png`, import.meta.url),
            Buffer.from(screenshot.data, 'base64'),
          )
        }
        console.log(
          width,
          theme,
          path,
          state,
          'overflow',
          result.pageWidth > width,
          'violaciones',
          result.violations.length,
        )
      }
    }
  }
}
fs.mkdirSync(new URL('../docs/auditoria/', import.meta.url), { recursive: true })
fs.writeFileSync(
  new URL('../docs/auditoria/axe-responsive.json', import.meta.url),
  JSON.stringify(
    {
      date: new Date().toISOString(),
      source: 'UI con fixtures, Chrome CDP, axe-core WCAG2 A/AA y WCAG2.1 AA',
      browser,
      results,
    },
    null,
    2,
  ),
)
ws.close()
if (
  results.some(
    (r) =>
      r.pageWidth > r.width || r.violations.some((v) => ['critical', 'serious'].includes(v.impact)),
  )
)
  process.exitCode = 1
