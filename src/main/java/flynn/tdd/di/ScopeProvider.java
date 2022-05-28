package flynn.tdd.di;

interface ScopeProvider {
    ComponentProvider<?> create(ComponentProvider<?> provider);
}
