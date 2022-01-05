import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static final String URL = "https://skillbox.ru/";
    public static final String DESTINATION = "src/main/Data";
    public static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            LOGGER.info("Start parse into {}: {}", DESTINATION, URL);
            SiteParser parser = new SiteParser(new URL(URL), 0);
            SiteNode main = new ForkJoinPool(14).invoke(parser);
            LOGGER.info(System.lineSeparator() + main);
        } catch (Exception exception) {
            LOGGER.error("{}: {}", exception.getMessage(), exception.getStackTrace());
        }
    }

}
