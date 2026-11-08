package no.course.company.minicontainer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/** En minimal «Spring-container»: skann -> opprett -> injiser via konstruktør. */
public class MiniContainer {

    private final Map<Class<?>, Object> beans = new LinkedHashMap<>();
    private final List<Class<?>> candidates = new ArrayList<>();
    private final Set<Class<?>> inCreation = new HashSet<>();

    public MiniContainer(String packageName) throws Exception {
        // 1. SKANN: finn alle klasser i pakken som har @MiniComponent
        File directory = new File(getClass().getClassLoader()
                .getResource(packageName.replace('.', '/')).toURI());
        File[] files = Objects.requireNonNull(directory.listFiles());
        Arrays.sort(files); // fast rekkefølge, så demoen skriver ut det samme hver gang
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> type = Class.forName(className);
                if (type.isAnnotationPresent(MiniComponent.class)) {
                    candidates.add(type);
                }
            }
        }
        // 2. OPPRETT: lag en instans av hver kandidat
        for (Class<?> type : candidates) {
            get(type);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        // Finn passende kandidater (selve klassen eller en implementasjon av et interface)
        List<Class<?>> matches = new ArrayList<>();
        for (Class<?> candidate : candidates) {
            if (type.isAssignableFrom(candidate)) matches.add(candidate);
        }
        if (matches.isEmpty()) {
            throw new IllegalStateException("No bean of type " + type.getSimpleName());
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple beans of type " + type.getSimpleName() + ": " + matches);
        }
        Class<?> beanClass = matches.get(0);
        if (beans.containsKey(beanClass)) {          // singleton: gjenbruk
            return (T) beans.get(beanClass);
        }
        if (!inCreation.add(beanClass)) {
            throw new IllegalStateException("Circular dependency at " + beanClass.getSimpleName());
        }
        try {
            // 3. INJISER: opprett avhengighetene til konstruktøren først
            Constructor<?> constructor = beanClass.getConstructors()[0];
            Object[] arguments = Arrays.stream(constructor.getParameterTypes())
                    .map(this::get).toArray();
            System.out.println("Creating " + beanClass.getSimpleName());
            Object bean = constructor.newInstance(arguments);
            beans.put(beanClass, bean);
            return (T) bean;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        } finally {
            inCreation.remove(beanClass);
        }
    }

    public Set<Class<?>> beanTypes() {
        return beans.keySet();
    }
}
