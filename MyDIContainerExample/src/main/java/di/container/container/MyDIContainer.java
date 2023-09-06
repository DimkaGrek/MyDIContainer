package di.container.container;

import di.container.App;
import di.container.annotation.Autowired;
import di.container.annotation.Component;
import di.container.annotation.PostConstructor;
import di.container.annotation.Qualifier;
import di.container.postprocessors.AfterAdditionHandler;
import di.container.postprocessors.BeforeAdditionHandler;
import javassist.bytecode.SignatureAttribute;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyDIContainer {

    private boolean isInitialized = false;
    private final Logger logger = LoggerFactory.getLogger(MyDIContainer.class);

    private final Map<Class<?>, Object> beans = new ConcurrentHashMap();
    private final Reflections reflections;
    private final String rootPackageName; // определяем корневой пакет приложения

    private final List<BeforeAdditionHandler> beforeAdditionHandlers = new ArrayList<>();
    private final List<AfterAdditionHandler> afterAdditionHandlers = new ArrayList<>();

    private Deque<Class<?>> creationStack = new LinkedList<>();

    private static final ClassLoader DEFAULT_CLASS_LOADER = MyDIContainer.class.getClassLoader();

    public MyDIContainer(String rootPackage) {
        this.rootPackageName = rootPackage;
        // используем наш загрузчик классов для Reflections
        this.reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(rootPackageName, DEFAULT_CLASS_LOADER))
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .addClassLoaders(DEFAULT_CLASS_LOADER));
    }

    public void addBeforeAdditionHandler(BeforeAdditionHandler handler) {
        beforeAdditionHandlers.add(handler);
    }

    public void addAfterAdditionHandler(AfterAdditionHandler handler) {
        afterAdditionHandlers.add(handler);
    }

    // добавляем инициализацию контейнера
    public void initialize() {
        if (!isInitialized) {
            try {
                // Находим все классы с аннотацией @Component
                Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
                for (Class<?> componentClass : componentClasses) {
                    createBean(componentClass);
                }
            } catch (Exception e) {
                logger.error("Ошибка при создании экземпляра" + e);
            }
        }

        isInitialized = true;
    }

    private Class<?> findImplementation(Class<?> interfaceClass, String qualifierValue) {
        Set<Class<?>> allClasses = (Set<Class<?>>) reflections.getSubTypesOf(interfaceClass);

        // Если qualifierValue пустой или null, вернем первую доступную реализацию
        if (qualifierValue == null || qualifierValue.trim().isEmpty()) {
            return allClasses.iterator().next();
        }

        for (Class<?> clazz : allClasses) {
            Component componentAnnotation = clazz.getAnnotation(Component.class);
            if (componentAnnotation != null && componentAnnotation.value().equals(qualifierValue)) {
                return clazz;
            }
        }
        // Здесь мы просто возвращаем первую найденную реализацию.
        return allClasses.iterator().next();
    }


    private <T> T createBean(Class<T> clazz) throws Exception {

        // Проверка на круговую зависимость
        if (creationStack.contains(clazz)) {
            String errorMessage = "Циклическая зависимость обнаружена для класса: " + clazz.getName() + ". Стек: " + creationStack;
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        creationStack.push(clazz);

        //Если объект уже создан, возвращаем его
        if (beans.containsKey(clazz)) {
            return (T) beans.get(clazz);
        }

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> selectedConstructor = null;

        // Ищем конструктор с аннотацией @Autowired или просто берем первый доступный
        for (Constructor<?> constructor: constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                selectedConstructor = constructor;
                selectedConstructor.setAccessible(true);
                break;
            }
        }
        if (selectedConstructor == null) {
            selectedConstructor = constructors[0];
        }

        // Создаем параметры для конструктора
        Object[] args  = new Object[selectedConstructor.getParameterCount()];
        Parameter[] parameters = selectedConstructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();

            Qualifier qualifier = parameters[i].getAnnotation(Qualifier.class);
            String quilifierValue = (qualifier != null) ? qualifier.value() : null;

            if (type.isInterface()) {
                type = findImplementation(type, quilifierValue);
            }

            // Рекурсивно создаем бин для каждой зависимости
            if (!beans.containsKey(type)) {
                createBean(type);
            }
            args[i] = beans.get(type);
        }

        // Создаем экземпляр объекта
        T instance = (T) selectedConstructor.newInstance(args);

        // добавляем обработчики Before
        for (BeforeAdditionHandler handler : beforeAdditionHandlers) {
            handler.handle(instance);
        }

        beans.put(clazz, instance);

        // добавляем обработчики After
        for (AfterAdditionHandler handler : afterAdditionHandlers) {
            handler.handle(instance);
        }

        // Вызываем методы с аннотацией @PostConstructor
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(PostConstructor.class)) {
                method.invoke(instance);
            }
        }

        creationStack.pop();

        return instance;
    }

    public <T> T getBean(Class<T> clazz) {
        if (!isInitialized) {
            initialize();
        }

        return (T) beans.get(clazz);
    }
}

