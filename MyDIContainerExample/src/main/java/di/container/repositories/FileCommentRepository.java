package di.container.repositories;

import di.container.annotation.Component;
import di.container.model.Comment;

@Component("file")
public class FileCommentRepository implements CommentRepository{
    @Override
    public void storeComment(Comment comment) {
        System.out.println("Storing to FILE comment: " + comment.getText());
    }
}
