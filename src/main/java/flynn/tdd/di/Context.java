package flynn.tdd.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public interface Context {

    <ComponentType> Optional<ComponentType> get(Ref<ComponentType> ref);

    class Ref<ComponentType> {
        public static <ComponentType> Ref<ComponentType> of(Class<ComponentType> component) {
            return new Ref(component);
        }

        public static <ComponentType> Ref<ComponentType> of(Class<ComponentType> component, Annotation qualifier) {
            return new Ref(component,qualifier);
        }


        public static Ref of(Type type) {
            return new Ref(type, null);
        }

        private Type container;
        private Class<ComponentType> component;
        private Annotation qualifier;

        Ref(Type type, Annotation qualifier) {
            init(type);
            this.qualifier = qualifier;
        }

        Ref(Class<ComponentType> component) {
            init(component);
        }


        protected Ref() {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            init(type);
        }

        private void init(Type type) {
            if (type instanceof ParameterizedType container) {
                this.container = container.getRawType();
                this.component = (Class<ComponentType>) container.getActualTypeArguments()[0];
            } else
                this.component = ((Class<ComponentType>) type);
        }

        public Type getContainer() {
            return container;
        }

        public Class<?> getComponent() {
            return component;
        }

        public boolean isContainer() {
            return container != null;
        }

        public Annotation getQualifier() {
            return qualifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ref ref = (Ref) o;
            return Objects.equals(container, ref.container) && component.equals(ref.component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(container, component);
        }
    }
}
