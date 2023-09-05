package di.container.proxies;

import di.container.model.Comment;

public interface CommentNotificationProxy {

    void sendComment(Comment comment);
}
