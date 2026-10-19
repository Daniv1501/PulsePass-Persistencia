package com.pulsepass.architecture;

import com.pulsepass.controller.ArtistController;
import com.pulsepass.controller.EventController;
import com.pulsepass.controller.TicketController;
import com.pulsepass.controller.UserController;
import com.pulsepass.controller.VenueController;
import com.pulsepass.dto.response.ErrorResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;
import com.pulsepass.service.UserService;
import com.pulsepass.service.VenueService;

import jakarta.validation.Valid;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica por reflexión, sin levantar Spring, los principios CTRL-001..010, AC-CTRL-015/016
 * y la Definition of Done (sección 30) del PRD de Controladores.
 */
class ControllerLayerRulesTest {

    private static final Pattern ENTITY_TYPE = Pattern.compile(
            "com\\.pulsepass\\.domain\\.(Venue|Event|Artist|User|UserProfile|Ticket)\\b");

    private static final List<Class<?>> CONTROLLERS = List.of(
            VenueController.class, EventController.class, ArtistController.class,
            UserController.class, TicketController.class);

    private static final Set<Class<?>> SERVICES = Set.of(
            VenueService.class, EventService.class, ArtistService.class,
            UserService.class, TicketService.class);

    // Fase 4: UserController delega el registro atómico en AuthService (PRD Seguridad §9.3).
    private static final Set<Class<?>> ALLOWED_DEPENDENCIES = Set.of(
            VenueService.class, EventService.class, ArtistService.class,
            UserService.class, TicketService.class, AuthService.class);

    /**
     * Contrato HTTP de la sección 8: los 20 métodos de Service de la Fase 2 (sección 9)
     * deben poder invocarse por exactamente estos endpoints.
     */
    private static final Set<String> HTTP_CONTRACT = Set.of(
            // VenueService (2)
            "GET /api/venues/{code}",
            "GET /api/venues/active",
            // EventService (6)
            "POST /api/events",
            "GET /api/events/{eventCode}",
            "GET /api/events/published",
            "PATCH /api/events/{eventCode}/publish",
            "POST /api/events/{eventCode}/artists/{artistId}",
            "GET /api/events/by-artist",
            // ArtistService (3)
            "GET /api/artists/{id}",
            "GET /api/artists/by-stage-name",
            "GET /api/artists/active",
            // UserService (3)
            "POST /api/users",
            "GET /api/users/by-email",
            "GET /api/users/by-username",
            // TicketService (6)
            "POST /api/tickets",
            "GET /api/tickets/{ticketCode}",
            "GET /api/tickets/by-user",
            "GET /api/events/{eventCode}/tickets/paid",
            "PATCH /api/tickets/{ticketCode}/cancel",
            "PATCH /api/tickets/{ticketCode}/use");

    // DoD: existen los cinco controllers anotados con @RestController y @RequestMapping
    @Test
    void controllersAreRestControllersUnderApi() {
        for (Class<?> controller : CONTROLLERS) {
            assertThat(controller.isAnnotationPresent(RestController.class))
                    .as("%s is @RestController", controller.getSimpleName()).isTrue();
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            assertThat(mapping).as("%s has @RequestMapping", controller.getSimpleName()).isNotNull();
            assertThat(mapping.value()[0]).startsWith("/api/");
        }
    }

    // CTRL-002 / AC-CTRL-015 / NFR-CTRL-004: dependencias exclusivamente de interfaces Service
    @Test
    void controllersDependOnlyOnServiceInterfaces() {
        for (Class<?> controller : CONTROLLERS) {
            var fields = Arrays.stream(controller.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    .toList();

            assertThat(fields).as("%s has dependencies", controller.getSimpleName()).isNotEmpty();
            for (var field : fields) {
                assertThat(ALLOWED_DEPENDENCIES).as("%s.%s is a Service", controller.getSimpleName(), field.getName())
                        .contains(field.getType());
                assertThat(field.getType().getName()).doesNotContain(".repository.");
                assertThat(Modifier.isFinal(field.getModifiers())).isTrue();
                assertThat(field.isAnnotationPresent(Autowired.class)).isFalse();
            }
            assertThat(controller.getDeclaredConstructors()).hasSize(1);
            for (Class<?> parameterType : controller.getDeclaredConstructors()[0].getParameterTypes()) {
                assertThat(ALLOWED_DEPENDENCIES).contains(parameterType);
            }
        }
    }

    // DoD: todos responden ResponseEntity / CTRL-003 / AC-CTRL-016: ninguno expone entidades JPA
    @Test
    void endpointsReturnResponseEntityOfDtosOnly() {
        for (Class<?> controller : CONTROLLERS) {
            for (Method method : endpointMethods(controller)) {
                assertThat(method.getReturnType())
                        .as("%s.%s returns ResponseEntity", controller.getSimpleName(), method.getName())
                        .isEqualTo(ResponseEntity.class);
                String returnType = method.getGenericReturnType().getTypeName();
                assertThat(ENTITY_TYPE.matcher(returnType).find())
                        .as("%s.%s does not expose entities: %s", controller.getSimpleName(), method.getName(), returnType)
                        .isFalse();
                assertThat(returnType).contains("com.pulsepass.dto.response.");
                for (Parameter parameter : method.getParameters()) {
                    assertThat(ENTITY_TYPE.matcher(parameter.getParameterizedType().getTypeName()).find())
                            .as("%s.%s parameter %s", controller.getSimpleName(), method.getName(), parameter.getName())
                            .isFalse();
                }
            }
        }
    }

    // CTRL-005: todo @RequestBody se valida con @Valid antes de llegar al Service
    @Test
    void requestBodiesAreValidated() {
        for (Class<?> controller : CONTROLLERS) {
            for (Method method : endpointMethods(controller)) {
                for (Parameter parameter : method.getParameters()) {
                    if (parameter.isAnnotationPresent(RequestBody.class)) {
                        assertThat(parameter.isAnnotationPresent(Valid.class))
                                .as("%s.%s @RequestBody has @Valid", controller.getSimpleName(), method.getName())
                                .isTrue();
                        assertThat(parameter.getType().getName()).startsWith("com.pulsepass.dto.request.");
                    }
                }
            }
        }
    }

    // Sección 9 / DoD: los 20 métodos de Service tienen exactamente un endpoint
    @Test
    void everyServiceOperationIsExposedOverHttp() {
        Set<String> exposed = new HashSet<>();
        int endpoints = 0;
        for (Class<?> controller : CONTROLLERS) {
            String base = controller.getAnnotation(RequestMapping.class).value()[0];
            for (Method method : endpointMethods(controller)) {
                exposed.add(route(base, method));
                endpoints++;
            }
        }

        assertThat(endpoints).isEqualTo(20);
        assertThat(exposed).containsExactlyInAnyOrderElementsOf(HTTP_CONTRACT);

        int serviceOperations = SERVICES.stream().mapToInt(s -> s.getDeclaredMethods().length).sum();
        assertThat(serviceOperations).isEqualTo(20);
    }

    // CTRL-007 / sección 15: un único @RestControllerAdvice con handlers ResponseEntity<ErrorResponse>
    @Test
    void globalExceptionHandlerReturnsErrorResponseForEveryHandler() {
        assertThat(GlobalExceptionHandler.class.isAnnotationPresent(RestControllerAdvice.class)).isTrue();

        Set<Class<?>> handled = new HashSet<>();
        for (Method method : GlobalExceptionHandler.class.getDeclaredMethods()) {
            ExceptionHandler handler = method.getAnnotation(ExceptionHandler.class);
            if (handler == null) {
                continue;
            }
            handled.addAll(Arrays.asList(handler.value()));
            assertThat(method.getReturnType()).isEqualTo(ResponseEntity.class);
            var generic = (ParameterizedType) method.getGenericReturnType();
            assertThat(generic.getActualTypeArguments()[0])
                    .as("%s returns ResponseEntity<ErrorResponse>", method.getName())
                    .isEqualTo(ErrorResponse.class);
        }

        assertThat(handled).contains(
                MethodArgumentNotValidException.class,
                ResourceNotFoundException.class,
                DuplicateResourceException.class,
                BusinessRuleException.class,
                Exception.class);
    }

    // Sección 13: ErrorResponse es un record con el contrato exacto
    @Test
    void errorResponseMatchesContract() {
        assertThat(ErrorResponse.class.isRecord()).isTrue();
        Map<String, Class<?>> expected = Map.of(
                "timestamp", LocalDateTime.class,
                "status", int.class,
                "error", String.class,
                "message", String.class,
                "details", Map.class);

        RecordComponent[] components = ErrorResponse.class.getRecordComponents();
        assertThat(components).hasSize(5);
        assertThat(Arrays.stream(components).map(RecordComponent::getName))
                .containsExactly("timestamp", "status", "error", "message", "details");
        for (RecordComponent component : components) {
            assertThat(component.getType()).isEqualTo(expected.get(component.getName()));
        }
    }

    private static List<Method> endpointMethods(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.isAnnotationPresent(GetMapping.class)
                        || m.isAnnotationPresent(PostMapping.class)
                        || m.isAnnotationPresent(PatchMapping.class))
                .toList();
    }

    private static String route(String base, Method method) {
        String verb;
        String[] paths;
        if (method.isAnnotationPresent(GetMapping.class)) {
            verb = "GET";
            paths = method.getAnnotation(GetMapping.class).value();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            verb = "POST";
            paths = method.getAnnotation(PostMapping.class).value();
        } else {
            verb = "PATCH";
            paths = method.getAnnotation(PatchMapping.class).value();
        }
        String path = paths.length == 0 ? "" : paths[0];
        return verb + " " + base + path;
    }
}
