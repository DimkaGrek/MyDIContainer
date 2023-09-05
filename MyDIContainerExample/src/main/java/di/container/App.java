package di.container;

import di.container.container.MyDIContainer;
import di.container.model.Comment;
import di.container.services.CommentService;

public class App
{
    public static void main( String[] args )
    {

        MyDIContainer context = new MyDIContainer(App.class.getPackageName());

        Comment comment = new Comment();
        comment.setAuthor("Dmytro");
        comment.setText("Demo comment");

        CommentService commentService = context.getBean(CommentService.class);
        commentService.publishComment(comment);
    }
}
