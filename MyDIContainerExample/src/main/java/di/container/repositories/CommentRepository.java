package di.container.repositories;

import di.container.model.Comment;

public interface CommentRepository {

    void storeComment(Comment comment);
}
