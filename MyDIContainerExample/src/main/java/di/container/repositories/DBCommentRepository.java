package di.container.repositories;

import di.container.annotation.Component;
import di.container.model.Comment;

@Component("db")
public class DBCommentRepository implements CommentRepository {
    @Override
    public void storeComment(Comment comment) {
        System.out.println("Storing comment: " + comment.getText());
    }
}
