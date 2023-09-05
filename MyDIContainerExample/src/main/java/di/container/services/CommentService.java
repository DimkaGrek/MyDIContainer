package di.container.services;

import di.container.annotation.Autowired;
import di.container.annotation.Component;
import di.container.annotation.PostConstructor;
import di.container.annotation.Qualifier;
import di.container.model.Comment;
import di.container.proxies.CommentNotificationProxy;
import di.container.proxies.EmailCommentNotificationProxy;
import di.container.repositories.CommentRepository;
import di.container.repositories.DBCommentRepository;

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

    @PostConstructor
    public void recommendationToSendByEmail() {
        System.out.println("This is recommendation to send notification by Email ...");
    }

    public void publishComment(Comment comment) {
        commentRepository.storeComment(comment);
        commentNotificationProxy.sendComment(comment);
    }
}
