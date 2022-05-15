package flynn.tdd.di;

import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@Nested
public class ContextTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    class TypeBinding {

        //TODO could get Provider<T> from context context
        @Test
        public void should_retrieve_bind_type_as_provider() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);

            Context context = config.getContext();

            ParameterizedType type = new TypeLiteral<Provider<Component>>() {
            }.getType();
            assertEquals(Provider.class, type.getRawType());
            assertEquals(Component.class, type.getActualTypeArguments()[0]);

            Provider<Component> provider = (Provider<Component>) context.get(type).get();
            assertSame(instance, provider.get());
        }

        @Test
        public void should_not_retrieve_bind_type_as_unsupported_container() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);

            Context context = config.getContext();
            ParameterizedType type = new TypeLiteral<List<Component>>() {}.getType();
            assertFalse(context.get(type).isPresent());
        }

        static abstract class TypeLiteral<T> {
            public ParameterizedType getType() {
                return (ParameterizedType) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
        }
    }
}
