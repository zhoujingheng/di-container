package flynn.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class ContainerTest {

    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    public class DependenciesSelection {
        @Nested
        public class ProviderType {

        }

        @Nested
        public class Qualifier {

        }
    }

    @Nested
    public class LifecycleManagement {

    }

    @Nested
    public class ComponentConstruction {

        @Test
        public void should_return_empty_if_component_not_define() {
            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

    }
}

interface TestComponent {
    default Dependency dependency() {
        return null;
    }
}

interface Dependency {
}

interface AnotherDependency {
}

class ComponentWithDefaultConstructor implements TestComponent {
    public ComponentWithDefaultConstructor() {
    }
}

class ComponentWithInjectConstructor implements TestComponent {
    private Dependency dependency;

    @Inject
    public ComponentWithInjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}


class ComponentWithMultiInjectConstructors implements TestComponent {
    @Inject
    public ComponentWithMultiInjectConstructors(String name, double value) {
    }

    @Inject
    public ComponentWithMultiInjectConstructors(String name) {
    }
}

class ComponentWithNoInjectNorDefaultConstructor implements TestComponent {
    public ComponentWithNoInjectNorDefaultConstructor(String name) {
    }
}

class DependencyWithInjectConstructor implements Dependency {
    private String dependency;

    @Inject
    public DependencyWithInjectConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}

class DependencyDependedOnComponent implements Dependency {

    private TestComponent component;

    @Inject
    public DependencyDependedOnComponent(TestComponent component) {
        this.component = component;
    }
}

class AnotherDependencyDependedOnComponent implements AnotherDependency {
    private TestComponent component;

    @Inject
    public AnotherDependencyDependedOnComponent(TestComponent component) {
        this.component = component;
    }
}

class DependencyDependedOnAnotherDependency implements Dependency {
    private AnotherDependency anotherDependency;

    @Inject
    public DependencyDependedOnAnotherDependency(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }
}

