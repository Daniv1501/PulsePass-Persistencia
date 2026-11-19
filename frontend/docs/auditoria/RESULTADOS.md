# Resultado final de verificación

Fecha local: 16 de noviembre de 2026 (America/Bogota). PRD de frontend v1.0, 63 páginas.

| Comprobación | Resultado | Evidencia |
| --- | --- | --- |
| ESLint | Pasa, sin errores | `npm.cmd run lint` |
| TypeScript strict + build Vite | Pasa | `npm.cmd run build` |
| Vitest / Testing Library / MSW | **148 pruebas pasan, 0 fallan**, 9 archivos | [vitest.json](vitest.json) |
| Dependencias | **0 vulnerabilidades informadas** | [npm-audit.json](npm-audit.json) |
| Rutas | 20 páginas; 19 lazy y un Inicio inmediato | Prueba en `src/test/rutas.test.tsx` |
| Primera carga | No descarga páginas Admin/Staff | Network requests del informe Lighthouse |
| Axe y responsive | **92 comprobaciones, 0 violaciones y 0 desbordamientos de documento** | [axe-responsive.json](axe-responsive.json) |
| Lighthouse Performance | **81/100**, supera mínimo 80 | [Informe HTML](lighthouse-inicio.report.html) · [JSON](lighthouse-inicio.report.json) |
| Lighthouse Accessibility | **100/100** automático | Mismo informe |
| Lighthouse Best Practices | **100/100** | Mismo informe |
| Tamaño de componentes | 50 funciones revisadas mediante AST; ninguna supera 150 líneas | Código de producción TSX |

Lighthouse: perfil **móvil predeterminado**, simulación con CPU ×4, compilación de producción servida localmente y API real a través del proxy. LCP medido: 3,5 s; TBT: 390 ms. Las puntuaciones varían con la carga del equipo; el informe adjunto corresponde a la compilación final. Las fuentes y las imágenes críticas se precargan, el código de bibliotecas se distribuye en paquetes y las secciones de Inicio fuera de pantalla difieren su renderizado.

Axe: Chrome 157.0.8103.51 instalado, 20 pantallas × 2 anchos (360/1440 px) × 2 temas, más 12 comprobaciones de formularios inválidos (login, registro y nuevo evento). Datos ficticios, contratos simulados en navegador, sin compras reales ni cambios de PostgreSQL. Incluye reglas WCAG 2 A/AA y WCAG 2.1 AA. Las secciones diferidas se hacen visibles durante esta auditoría para comprobar también su contenido.

Capturas de Inicio:

- [360 px claro](inicio-360-light.png) · [360 px oscuro](inicio-360-dark.png)
- [1440 px claro](inicio-1440-light.png) · [1440 px oscuro](inicio-1440-dark.png)

Se corrigieron, entre otros, el retorno al evento después de login, detalles 400 ocultos, avisos de éxito sin vencimiento, tiempos de caché/reintentos, contraste en ambos temas y estados inválidos de formularios. Vitest se actualizó a la versión corregida que deja npm audit sin alertas.

**Límites explícitos:** la paleta océano/turquesa, el logo de onda y la portada de festival corresponden a las solicitudes posteriores del usuario; no se revierte a las capturas originales del PDF. No se ha ejecutado la matriz completa de las últimas dos versiones de Chrome, Edge, Firefox y Safari ni una evaluación humana completa WCAG con tecnologías de asistencia. El 100 de Lighthouse no equivale a esa certificación. La aceptación manual de Postman se mantiene en su propia bitácora.

Consulta la [matriz paso a paso](../CUMPLIMIENTO_FRONTEND.md) y las [instrucciones para reproducir](REPRODUCIR.md).
