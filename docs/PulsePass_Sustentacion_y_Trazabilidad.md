# PulsePass — Capa de Servicios: trazabilidad, Definition of Done y sustentación

## 1. Matriz de trazabilidad (PRD sección 49, completa)

| Requisito | Servicio | Reglas | Tests |
|---|---|---|---|
| FR-SVC-001 / 002 | `VenueService` | BR-VENUE-001, 002 | `VenueServiceImplTest` |
| FR-SVC-003 | `EventService.create` | BR-EVENT-001..006 | `EventServiceImplTest` (TEST-EVENT-003..006 + duplicado, edad, fecha nula, DRAFT inicial) |
| FR-SVC-004 / 005 / 008 | `EventService` consultas | — | `EventServiceImplTest` (TEST-EVENT-001, 002 + publicados + por artista) |
| FR-SVC-006 | `EventService.publish` | BR-EVENT-007..009 | `EventServiceImplTest` (TEST-EVENT-007, 008 + estados inválidos, fecha pasada, venue inactivo) |
| FR-SVC-007 | `EventService.addArtist` | BR-EVENT-010, 011 | `EventServiceImplTest` (éxito, tres artistas, duplicado, CANCELLED/FINISHED, evento y artista inexistentes) |
| FR-SVC-009 | `ArtistService` | BR-ARTIST-001, 002 | `ArtistServiceImplTest` |
| FR-SVC-010 | `UserService.register` | BR-USER-001..005 | `UserServiceImplTest` (TEST-USER-001..004 + activo/perfil, email case-insensitive) |
| FR-SVC-011 / 012 | `UserService` consultas | — | `UserServiceImplTest` |
| FR-SVC-013 | `TicketService.purchase` | BR-TICKET-001..009 | `TicketServiceImplTest` (TEST-TICKET-001..008 + bordes de edad, capacidad, precio) y `TicketPricingPolicyTest` |
| FR-SVC-014 / 015 / 016 | `TicketService` consultas | — | `TicketServiceImplTest` |
| FR-SVC-017 | `TicketService.cancel` | BR-TICKET-010..012 | `TicketServiceImplTest` (TEST-TICKET-009, 010 + CANCELLED, después del evento) |
| FR-SVC-018 | `TicketService.markAsUsed` | BR-TICKET-013, 014 | `TicketServiceImplTest` (TEST-TICKET-011, 012 + USED dos veces) |
| AC-001..AC-011 | los tres servicios de escritura | — | `PulsePassAcceptanceScenarioTest` |

## 2. Definition of Done (sección 50)

- [x] Interfaces Service e implementaciones `@Service` (`ServiceLayerRulesTest`)
- [x] Constructor injection, campos `final` (`ServiceLayerRulesTest`)
- [x] DTOs `record`, sin entidades JPA expuestas (`ServiceLayerRulesTest`)
- [x] MapStruct para Entity → DTO (`MapperTest`)
- [x] Excepciones personalizadas y `Optional` con `orElseThrow`
- [x] Reglas en Service; escrituras `@Transactional`; lecturas `readOnly` (`ServiceLayerRulesTest`)
- [x] Los cinco servicios implementados
- [x] La compra valida usuario, evento, edad y capacidad; `SOLD_OUT` se actualiza en la misma transacción
- [x] Unit tests con Mockito + AssertJ, sin PostgreSQL ni `@SpringBootTest`, con `verify(..., never())` en caminos inválidos
- [ ] `mvn clean test` — **hay que correrlo en tu máquina**: necesita descargar dependencias y Docker activo para `PersistenceIntegrationTest` (el resto de tests no necesita Docker).

## 3. Respuestas de sustentación (sección 51)

1. **Responsabilidad de Service:** aplicar reglas de negocio, coordinar repositories, controlar transacciones, transformar entidades a DTOs y producir errores de dominio claros.
2. **Service vs Repository:** el Repository solo accede a datos (¿existe este evento?); el Service decide qué se puede hacer con ellos (¿puede este usuario comprar esta entrada?).
3. **Por qué no devolver entidades JPA:** acopla la API al modelo de persistencia, filtra relaciones perezosas (`LazyInitializationException`), puede exponer campos internos y hace que un cambio de esquema rompa a los clientes.
4. **Ventaja de `record`:** inmutables, concisos, con `equals`/`hashCode`/`toString` automáticos y sin setters; ideales para transportar datos.
5. **Qué hace MapStruct:** genera en compilación el código de mapeo Entity ↔ DTO, sin reflexión en tiempo de ejecución y con errores detectados al compilar.
6. **Constructor injection:** dependencias `final` e inmutables, el objeto nunca queda a medio construir, es obvio qué necesita la clase y se instancia en tests sin Spring (`new Servicio(mock1, mock2)`).
7. **`ResourceNotFoundException` vs `BusinessRuleException`:** la primera indica que el recurso no existe; la segunda, que existe pero la operación no es válida en su estado actual (ej. evento no publicado). Una tercera, `DuplicateResourceException`, cubre los conflictos de unicidad.
8. **Por qué evitar `Optional.get()`:** lanza `NoSuchElementException` genérica si está vacío; `orElseThrow` obliga a decidir el error de dominio y con un mensaje significativo.
9. **`@Transactional(readOnly = true)`:** en consultas; permite optimizaciones (sin dirty checking, flush más barato) y documenta que el método no modifica datos.
10. **`@Transactional`:** en operaciones que escriben o que deben ser atómicas (`create`, `publish`, `addArtist`, `register`, `purchase`, `cancel`, `markAsUsed`).
11. **Por qué la compra es atómica:** involucra varios pasos (ticket + posible cambio a `SOLD_OUT`); si falla alguno debe revertirse todo para no dejar tickets huérfanos ni eventos mal marcados.
12. **Por qué la capacidad es regla de Service:** depende de datos de varias entidades (tickets pagados del evento y capacidad del venue) y es una decisión de negocio, no de acceso a datos.
13. **Por qué la edad es regla de negocio:** requiere reglas propias (se evalúa a la fecha del evento, no a hoy; 0 = sin restricción) que pueden cambiar según la política de la plataforma.
14. **Por qué `BigDecimal`:** `double` no representa decimales exactos y acumula errores de redondeo; el dinero requiere precisión y control de escala.
15. **Unit test vs integration test:** el unitario prueba una clase aislada con dependencias simuladas (rápido, sin infraestructura); el de integración prueba varias piezas reales juntas (por ejemplo JPA + PostgreSQL con Testcontainers).
16. **Por qué Mockito permite probar sin PostgreSQL:** reemplaza los repositories y mappers por dobles con comportamiento definido (`when/thenReturn`), de modo que solo se ejecuta la lógica del Service.
17. **Qué demuestra `verify(repository, never()).save(...)`:** que ante una regla violada no se persistió nada; no basta con que se lance la excepción.
18. **Por qué `SOLD_OUT` en la misma transacción:** para que ticket y estado del evento sean consistentes; si el ticket se guarda y el cambio de estado falla (o al revés) el sistema quedaría contradictorio.
19. **Por qué Ticket es entidad y no solo N:M:** tiene datos propios (código, tipo, precio, estado, fecha) y ciclo de vida (PAID → USED/CANCELLED); un `@ManyToMany` puro no puede cargar esos atributos.
20. **Qué moverías a componentes especializados:** `PricingService` (precios, promociones), `CapacityService` (cupos, reservas temporales), `PaymentService`, `NotificationService` y `RecommendationService`, como adelanta la sección 52.

## 4. Decisiones y límites a tener presentes

- **Concurrencia (sección 53):** contar tickets y luego guardar no es seguro ante compras simultáneas; queda fuera del alcance. La mejora natural es bloqueo optimista/pesimista o una restricción en base de datos.
- **Cancelar un ticket de un evento `SOLD_OUT`:** el PRD no define transición `SOLD_OUT → PUBLISHED`, así que el evento permanece `SOLD_OUT` aunque se libere un cupo. Si se quiere reabrir la venta, habría que añadir esa transición al ciclo de estados.
- **Endurecimiento añadido sobre el PRD:** tipo de ticket obligatorio, fecha de evento nula rechazada y guardia explícita de `price >= 0` (BR-TICKET-009).
