package com.pulsepass.architecture;

import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;
import com.pulsepass.service.UserService;
import com.pulsepass.service.VenueService;
import com.pulsepass.service.impl.ArtistServiceImpl;
import com.pulsepass.service.impl.EventServiceImpl;
import com.pulsepass.service.impl.TicketServiceImpl;
import com.pulsepass.service.impl.UserServiceImpl;
import com.pulsepass.service.impl.VenueServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprueba por reflexión los puntos de la Definition of Done (sección 50) y los
 * principios SRV-001..005 / NFR-002, NFR-003 y NFR-006 del PRD, sin levantar Spring.
 */
class ServiceLayerRulesTest {

    private static final Pattern ENTITY_TYPE = Pattern.compile(
            "com\\.pulsepass\\.domain\\.(Venue|Event|Artist|User|UserProfile|Ticket)\\b");

    private static final List<Class<?>> SERVICE_INTERFACES = List.of(
            VenueService.class, EventService.class, ArtistService.class,
            UserService.class, TicketService.class);

    private static final List<Class<?>> SERVICE_IMPLS = List.of(
            VenueServiceImpl.class, EventServiceImpl.class, ArtistServiceImpl.class,
            UserServiceImpl.class, TicketServiceImpl.class);

    private static final List<Class<?>> DTOS = List.of(
            CreateEventRequest.class, RegisterUserRequest.class, PurchaseTicketRequest.class,
            VenueResponse.class, EventResponse.class, EventSummaryResponse.class,
            ArtistResponse.class, UserResponse.class, TicketResponse.class);

    private static final Set<String> WRITE_METHODS = Set.of(
            "create", "publish", "addArtist", "register", "purchase", "cancel", "markAsUsed");

    // SRV-001: los contratos públicos no exponen entidades JPA
    @Test
    void serviceContractsNeverExposeJpaEntities() {
        for (Class<?> contract : SERVICE_INTERFACES) {
            for (Method method : contract.getDeclaredMethods()) {
                assertThat(ENTITY_TYPE.matcher(method.getGenericReturnType().getTypeName()).find())
                        .as("%s.%s return type", contract.getSimpleName(), method.getName())
                        .isFalse();
                for (var param : method.getGenericParameterTypes()) {
                    assertThat(ENTITY_TYPE.matcher(param.getTypeName()).find())
                            .as("%s.%s parameter", contract.getSimpleName(), method.getName())
                            .isFalse();
                }
            }
        }
    }

    // NFR-003: DTOs inmutables como record y sin entidades dentro
    @Test
    void dtosAreRecordsWithoutEntities() {
        for (Class<?> dto : DTOS) {
            assertThat(dto.isRecord()).as("%s is a record", dto.getSimpleName()).isTrue();
            for (RecordComponent component : dto.getRecordComponents()) {
                assertThat(ENTITY_TYPE.matcher(component.getGenericType().getTypeName()).find())
                        .as("%s.%s", dto.getSimpleName(), component.getName())
                        .isFalse();
            }
        }
    }

    // SRV-003: cada servicio tiene interfaz e implementación
    @Test
    void everyImplementationImplementsItsContract() {
        for (int i = 0; i < SERVICE_INTERFACES.size(); i++) {
            assertThat(SERVICE_INTERFACES.get(i).isInterface()).isTrue();
            assertThat(SERVICE_INTERFACES.get(i)).isAssignableFrom(SERVICE_IMPLS.get(i));
        }
    }

    // DoD: implementaciones @Service
    @Test
    void implementationsAreSpringServices() {
        for (Class<?> impl : SERVICE_IMPLS) {
            assertThat(impl.isAnnotationPresent(Service.class)).as(impl.getSimpleName()).isTrue();
        }
    }

    // SRV-002 / NFR-002: inyección por constructor, dependencias final
    @Test
    void implementationsUseConstructorInjectionWithFinalFields() {
        for (Class<?> impl : SERVICE_IMPLS) {
            var instanceFields = Arrays.stream(impl.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    .toList();

            assertThat(instanceFields).as("%s has dependencies", impl.getSimpleName()).isNotEmpty();
            for (var field : instanceFields) {
                assertThat(Modifier.isFinal(field.getModifiers()))
                        .as("%s.%s is final", impl.getSimpleName(), field.getName()).isTrue();
                assertThat(field.isAnnotationPresent(Autowired.class))
                        .as("%s.%s has no @Autowired", impl.getSimpleName(), field.getName()).isFalse();
            }

            assertThat(impl.getDeclaredConstructors()).hasSize(1);
            assertThat(impl.getDeclaredConstructors()[0].getParameterCount())
                    .as("%s constructor receives every dependency", impl.getSimpleName())
                    .isEqualTo(instanceFields.size());
        }
    }

    // SRV-004: lecturas readOnly a nivel de clase, escrituras con @Transactional de escritura
    @Test
    void transactionsFollowReadWriteConvention() {
        for (Class<?> impl : SERVICE_IMPLS) {
            Transactional classLevel = impl.getAnnotation(Transactional.class);
            assertThat(classLevel).as("%s class-level @Transactional", impl.getSimpleName()).isNotNull();
            assertThat(classLevel.readOnly()).as("%s class-level readOnly", impl.getSimpleName()).isTrue();

            for (Method method : impl.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                if (WRITE_METHODS.contains(method.getName())) {
                    Transactional writeTx = method.getAnnotation(Transactional.class);
                    assertThat(writeTx)
                            .as("%s.%s must be @Transactional", impl.getSimpleName(), method.getName())
                            .isNotNull();
                    assertThat(writeTx.readOnly())
                            .as("%s.%s must not be readOnly", impl.getSimpleName(), method.getName())
                            .isFalse();
                } else {
                    assertThat(method.getAnnotation(Transactional.class))
                            .as("%s.%s inherits readOnly from the class", impl.getSimpleName(), method.getName())
                            .isNull();
                }
            }
        }
    }

    // Sección 35: excepciones personalizadas de dominio
    @Test
    void customExceptionsAreUncheckedAndDistinct() {
        List<Class<? extends RuntimeException>> exceptions = List.of(
                ResourceNotFoundException.class, BusinessRuleException.class, DuplicateResourceException.class);

        assertThat(exceptions).doesNotHaveDuplicates();
        assertThat(exceptions).allSatisfy(type ->
                assertThat(RuntimeException.class).isAssignableFrom(type));
    }
}
