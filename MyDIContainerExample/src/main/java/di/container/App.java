package di.container;

import di.container.container.MyDIContainer;
import di.container.model.Comment;
import di.container.postprocessors.LoggingAfterAdditionHandler;
import di.container.postprocessors.LoggingBeforeAdditionHandler;
import di.container.services.CommentService;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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
