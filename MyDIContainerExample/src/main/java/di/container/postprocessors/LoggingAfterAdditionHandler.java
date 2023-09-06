package di.container.postprocessors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAfterAdditionHandler implements AfterAdditionHandler{

    private final Logger logger = LoggerFactory.getLogger(LoggingAfterAdditionHandler.class);
    private final LoggingBeforeAdditionHandler beforeHandler;

    public LoggingAfterAdditionHandler(LoggingBeforeAdditionHandler beforeHandler) {
        this.beforeHandler = beforeHandler;
    }

    @Override
    public void handle(Object bean) {
        Long startTime = beforeHandler.getStartTime(bean);
        long elepsedTime = System.currentTimeMillis() - startTime;
        logger.info("Bean " + bean.getClass().getName() + "created in " + elepsedTime + "ms");
    }
}
