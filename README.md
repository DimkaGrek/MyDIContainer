# MyDIContainer
Реализация простого контейнера зависимостей ("а-ля" как в Spring) на аннотациях.

*__MyDIContainer-1.0-SNAPSHOT.jar__* - подключаемый модуль. Можно добавить ввиде библиотеки и использовать функционал.

В папке *__MyDIContainerExample__* - пример использования контейнера.
В примере контейнер зависимостей находится в пакетах annotation, container, postprocessors.

## Как работает
Для конфигурации используются аннотации (смотри раздел "Как пользоваться" ниже). При инициализации контейнера, с помощью библиотеки Reflections, 
сканируется весь корневой пакет, который необходимо указать при создании контейнера. Все классы, указанные аннотацией @Component попадают в контейнер.

Если необходимо дополнительно добавить обработчики перед добавлением класса или после добавления, используются 
соответсвующие методы *addBeforeAdditionHandler()*, *addAfterAdditionHandler()*. Для обработчиков необходимо реализовать интерфейсы
*BeforeAdditionHandler.java*, *AfterAdditionHandler.java*  

**Круговая зависимость**: В контейнере добавлена реализация защиты от круговой зависимости. В примере в пакете cycledependency находяться
два класса с круговой зависимостью (но почему-то я пока не смог добиться, чтобы выскочило исключение, оба класса регистрируются нормально.)  

**ClassLoader:** чтобы разные загрузчики не ломали реализацию, я использовал загрузчик по умолчанию 
(_private static final ClassLoader DEFAULT_CLASS_LOADER = MyDIContainer.class.getClassLoader();_) и потом при подключении Reflections использую
этот загрузчик.

## Как пользоваться  
Для настройки зависимостей используются аннотации.
  
> @Component - Класс, отмеченный этой аннотацией, регистрируется в контейнере зависимостей.  
  
Поставьте аннотацию **@Component**, как указано в примере:  
```java
@Component  
public class CommentService {
private CommentRepository commentRepository;  
  
// ... [остальной код]  
}  
```
  
> @Autowired - эта аннотация применяется к конструктору. Дает возможность отметить, какой конструктор нужно использовать, если их несколько.
Если не указана, тогда используется первый доступный конструктор.  
  
Поставьте аннотацию @Autowired, как указано в примере:
```java
    @Component
    public class CommentService {

        private CommentRepository commentRepository;
    
        private CommentNotificationProxy commentNotificationProxy;

        public CommentService() {
            this.commentRepository = new DBCommentRepository();
            this.commentNotificationProxy = new EmailCommentNotificationProxy();
        }
    
        @Autowired
        public CommentService(@Qualifier("file") CommentRepository commentRepository, CommentNotificationProxy commentNotificationProxy) {
            this.commentRepository = commentRepository;
            this.commentNotificationProxy = commentNotificationProxy;
        }
    
       // ... другие методы
    }
```  
  
> @Qualifier - применяется к параметрам конструктора, на тот случай если нужно добавить другой экземпляр класса. Идентифицирует параметр по строке.  
  
Чтобы настроить эту аннотацию, необходимо над классами, которые реализуют один и тот же интерфейс, в аннотации @Component("") указать строковый
маркер, который будет использоваться в аннотации @Qualifier(""). Ниже пример, как использовать.
```java
@Component("db")
public class DBCommentRepository implements CommentRepository {
    @Override
    public void storeComment(Comment comment) {
        System.out.println("Storing comment: " + comment.getText());
    }
}
```
```java
@Component("file")
public class FileCommentRepository implements CommentRepository{
    @Override
    public void storeComment(Comment comment) {
        System.out.println("Storing to FILE comment: " + comment.getText());
    }
}
```
```java
@Component
public class CommentService {

    // ... [код]
    
    public CommentService() {
        this.commentRepository = new DBCommentRepository();
        this.commentNotificationProxy = new EmailCommentNotificationProxy();
    }

    @Autowired
    public CommentService(@Qualifier("file") CommentRepository commentRepository, CommentNotificationProxy commentNotificationProxy) {
        this.commentRepository = commentRepository;
        this.commentNotificationProxy = commentNotificationProxy;
    }
    
    // ... [код]
}
```
> @PostConstructor - эта аннотация ставиться над методом, который нужно вызвать сразу после создания инстанса  
  
Ниже пример, как использовать:
```java
@Component
public class CommentService {

    // ... [код]

    @PostConstructor
    public void recommendationToSendByEmail() {
        System.out.println("This is recommendation to send notification by Email ...");
    }

    public void publishComment(Comment comment) {
        commentRepository.storeComment(comment);
        commentNotificationProxy.sendComment(comment);
    }
}
```
- Для подключения контейнера необходимо создать экземпляр, указав корневой пакет.   
- Перед инициализацией возможно также добавить обработчики перед и после добавления класса в контейнер. Для этого необходимо создать реализации
интерфейсов *BeforeAdditionHandler.java*, *AfterAdditionHandler.java* 
и вызвать соответствующие методы *addBeforeAdditionHandler()*, *addAfterAdditionHandler()*
- далее проинициализировать контейнер, запустив метод *.initialize()*
- для получения экземпляра класса из контейнера используется метод *.getBean(ClassName.class)*  

Ниже показан пример:
```java
public class App
{
    public static void main( String[] args )
    {
        // Получаем имя корневого пакета
        String packageName = App.class.getPackageName();

        // Создаем di container
        MyDIContainer context = new MyDIContainer(packageName);

        // добавляем обработчики
        LoggingBeforeAdditionHandler beforeHandler = new LoggingBeforeAdditionHandler();
        LoggingAfterAdditionHandler afterHandler = new LoggingAfterAdditionHandler(beforeHandler);
        context.addBeforeAdditionHandler(beforeHandler);
        context.addAfterAdditionHandler(afterHandler);

        // инициализируем контейнер
        context.initialize();

        Comment comment = new Comment();
        comment.setAuthor("Dmytro");
        comment.setText("Demo comment");

        CommentService commentService = context.getBean(CommentService.class);
        commentService.publishComment(comment);
    }
}
```
