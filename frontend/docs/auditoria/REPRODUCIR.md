# Reproducir la auditoría

Desde `frontend/`, con Node 22.22, 24 o 26 compatible:

```powershell
npm.cmd ci
npm.cmd run lint
npm.cmd run build
npm.cmd test -- --reporter=default --reporter=json --outputFile=docs/auditoria/vitest.json
npm.cmd run preview -- --host 127.0.0.1 --port 5174
```

Mantener preview ejecutándose. Para Lighthouse, en otra terminal, con el backend disponible en el proxy de `.env`:

```powershell
$env:CHROME_PATH='C:\Program Files\Google\Chrome\Application\chrome.exe'
npm.cmd run audit:lighthouse
```

El comando usa el perfil móvil predeterminado de Lighthouse y genera JSON y HTML. Medir sin compilar o ejecutar pruebas en paralelo, pues la carga de CPU altera el resultado. No usa sesión autenticada ni guarda tokens.

Para axe/responsive, abrir un perfil de Chrome exclusivo de auditoría:

```powershell
Start-Process -FilePath 'C:\Program Files\Google\Chrome\Application\chrome.exe' -ArgumentList @('--headless=new','--disable-gpu','--hide-scrollbars','--no-first-run','--remote-debugging-address=127.0.0.1','--remote-debugging-port=9337',"--user-data-dir=$env:TEMP\pulsepass-auditoria-cdp",'about:blank') -WindowStyle Hidden
npm.cmd run audit:ui
```

Este script simula los contratos HTTP con datos ficticios, sin modificar PostgreSQL. Recorre las 20 páginas a 360/1440 px en claro y oscuro y envía formularios vacíos de login, registro y creación de evento para auditar también sus errores. Espera a la carga de página/fuentes y desactiva la caché. Hace visibles para axe las secciones cuyo renderizado se difiere fuera de pantalla. Conserva el informe JSON y cuatro capturas de Inicio.

La auditoría falla ante desbordamiento de documento o incidencias serious/critical. El resultado entregado además exige cero violaciones de cualquier impacto en las reglas WCAG 2 A/AA y WCAG 2.1 AA seleccionadas. No certifica todas las condiciones WCAG ni la matriz completa de navegadores.

Para comprobar dependencias:

```powershell
npm.cmd audit --json
```
