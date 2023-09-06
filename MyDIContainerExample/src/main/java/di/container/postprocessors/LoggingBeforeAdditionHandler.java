package di.container.postprocessors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingBeforeAdditionHandler implements BeforeAdditionHandler {

    private final Logger logger = LoggerFactory.getLogger(LoggingBeforeAdditionHandler.class);
    private final Map<Object, Long> startTimes = new ConcurrentHashMap<>();

    @Override
    public void handle(Object bean) {
        logger.info("Starting creation of bean: " + bean.getClass());
        startTimes.put(bean, System.currentTimeMillis());
    }

    public Long getStartTime(Object bean) {
        return startTimes.get(bean);
    }
}
