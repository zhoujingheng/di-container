package flynn.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Nested
public class InjectTest {

    private Dependency dependency = mock(Dependency.class);
    private Context context = mock(Context.class);

    @BeforeEach
    public void setup() {
        when(context.get(eq(Dependency.class))).thenReturn(Optional.of(dependency));
    }

    @Nested
    public class ConstructorInjection {

        abstract class AbstractComponent implements Component {

            @Inject
            public AbstractComponent() {
            }
        }

        @Test
        public void should_throw_exception_if_component_is_abstract() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(AbstractComponent.class));
        }

        @Test
        public void should_throw_exception_if_component_is_interface() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class));
        }

        @Test
        public void should_bind_type_to_a_class_with_default_constructor() {
            ComponentWithDefaultConstructor instance = new ConstructorInjectionProvider<>(ComponentWithDefaultConstructor.class).get(context);

            assertNotNull(instance);
        }

        @Test
        public void should_bind_type_to_a_class_with_inject_constructor() {
            ComponentWithInjectConstructor instance = new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class).get(context);

            assertNotNull(instance);
            assertSame(dependency, instance.getDependency());
        }

        @Test
        public void should_throw_exception_if_multi_inject_constructors_provided() {
            assertThrows(IllegalComponentException.class, () -> {
                new ConstructorInjectionProvider<>(ComponentWithMultiInjectConstructors.class);
            });
        }

        @Test
        public void should_throw_exception_if_no_inject_nor_default_constructor_provided() {
            assertThrows(IllegalComponentException.class, () -> {
                new ConstructorInjectionProvider<>(ComponentWithNoInjectNorDefaultConstructor.class);
            });
        }

        public void should_include_dependency_from_inject_constructor() {
            ConstructorInjectionProvider<ComponentWithInjectConstructor> provider = new ConstructorInjectionProvider<>(ComponentWithInjectConstructor.class);
            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
        }
    }

    @Nested
    public class FiledInjection {
        static class ComponentWithFiledInjection {
            @Inject
            Dependency dependency;
        }

        static class SubclassWithFieldInjection extends FiledInjection.ComponentWithFiledInjection {
        }

        @Test
        public void should_inject_dependency_via_filed() {
            FiledInjection.ComponentWithFiledInjection component = new ConstructorInjectionProvider<>(ComponentWithFiledInjection.class).get(context);
            assertSame(dependency, component.dependency);
        }

        @Test
        public void should_inject_dependency_via_superclass_inject_filed() {
            FiledInjection.SubclassWithFieldInjection component = new ConstructorInjectionProvider<>(SubclassWithFieldInjection.class).get(context);
            assertSame(dependency, component.dependency);
        }

        //TODO throw exception if dependency not found

        //TODO throw exception if filed is final
        static class FinalInjectFiled {

            @Inject
            final Dependency dependency = null;
        }

        @Test
        public void should_throw_exception_if_inject_field_is_final() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(FiledInjection.FinalInjectFiled.class));
        }

        @Test
        public void should_include_field_dependency_in_dependencies() {
            ConstructorInjectionProvider<FiledInjection.ComponentWithFiledInjection> provider = new ConstructorInjectionProvider<>(FiledInjection.ComponentWithFiledInjection.class);
            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
        }

        //TODO throw excepiton if cyclic dependency
    }

    @Nested
    public class MethodInjection {
        static class InjectMethodWithNoDependency {
            boolean called = false;

            @Inject
            void install() {
                this.called = true;
            }
        }

        @Test
        public void should_call_inject_method_even_if_no_dependency_declared() {

            InjectMethodWithNoDependency component = new ConstructorInjectionProvider<>(InjectMethodWithNoDependency.class).get(context);
            ;
            assertTrue(component.called);
        }

        static class InjectMethodWithDependency {
            Dependency dependency;

            @Inject
            void install(Dependency dependency) {
                this.dependency = dependency;
            }
        }

        @Test
        public void should_inject_dependency_via_inject_method() {
            InjectMethodWithDependency component = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class).get(context);
            ;
            assertSame(dependency, component.dependency);
        }

        //TODO inject method with dependencies will be injected
        //TODO override inject method from superclass


        static class SuperClassWithInjectMethod {
            int superCalled = 0;

            @Inject
            void install() {
                superCalled++;
            }
        }

        static class SubclassWithInjectMethod extends MethodInjection.SuperClassWithInjectMethod {
            int subCalled = 0;

            @Inject
            void installAnother() {
                subCalled = superCalled + 1;
            }
        }

        @Test
        public void should_inject_dependencies_via_inject_method_from_superclass() {

            SubclassWithInjectMethod component = new ConstructorInjectionProvider<>(SubclassWithInjectMethod.class).get(context);
            assertEquals(1, component.superCalled);
            assertEquals(2, component.subCalled);
        }

        static class SubclassOverrideSuperClassWithInject extends MethodInjection.SuperClassWithInjectMethod {

            @Inject
            void install() {
                super.install();
            }
        }

        @Test
        public void should_only_call_once_if_subclass_override_inject_method_with_inject() {
            SubclassOverrideSuperClassWithInject component = new ConstructorInjectionProvider<>(SubclassOverrideSuperClassWithInject.class).get(context);
            ;
            assertEquals(1, component.superCalled);
        }

        static class SubclassOverrideSuperClassWithNoInject extends MethodInjection.SuperClassWithInjectMethod {

            void install() {
                super.install();
            }
        }

        @Test
        public void should_not_call_inject_method_if_override_with_no_inject() {

            SubclassOverrideSuperClassWithNoInject component = new ConstructorInjectionProvider<>(SubclassOverrideSuperClassWithNoInject.class).get(context);
            assertEquals(0, component.superCalled);
        }

        //TODO include dependencies from inject methods
        @Test
        public void should_include_dependencies_from_inject_method() {
            ConstructorInjectionProvider<MethodInjection.InjectMethodWithDependency> provider = new ConstructorInjectionProvider<>(MethodInjection.InjectMethodWithDependency.class);
            assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
        }

        //TODO throw exception if type parameter defined
        static class InjectMethodWithTypeParameter {

            @Inject
            <T> void install() {

            }
        }

        @Test
        public void should_throw_exception_if_inject_method_has_type_parameter() {
            assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(MethodInjection.InjectMethodWithTypeParameter.class));
        }
    }
}
