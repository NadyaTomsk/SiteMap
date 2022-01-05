import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class SiteParser extends RecursiveTask<SiteNode> {

    private static final Logger LOGGER = LogManager.getLogger(SiteParser.class);

    @Getter
    private final URL url;
    private final int level;
    private final String host;
    @Getter
    private SiteNode mainNode;

    public SiteParser(URL url, int level) {
        this.url = url;
        this.level = level;
        host = url.getHost().replaceAll("www\\.", "");
    }

    private Set<String> getChildes(URL parent) {
        Set<String> childes = new TreeSet<>();
        Connection connection = Jsoup.connect(parent.toString()).maxBodySize(0);
        try {
            Thread.sleep((long) (Math.random() * 50 + 100));
            Document doc = connection.get();
            Elements elements = doc.select("a[href]");
            for (Element element : elements) {
                String attr = element.attr("abs:href");
                if (attr.matches(".*#$") || attr.contains("javascript") ||
                        !attr.contains(url.toString()) || attr.equals(url.toString())) {
                    continue;
                }
                childListAdd(childes, attr);
            }
        } catch (Exception e) {
            LOGGER.error("{}\n{}", e.getMessage(), e.getStackTrace());
        }

        return childes;
    }

    @Override
    protected SiteNode compute() {
        Set<String> childes = getChildes(url);
        if (childes.size() == 0) {
            return new SiteNode(level, url);
        } else {
            SiteNode node = new SiteNode(level, url);
            try {
                List<SiteParser> taskList = new ArrayList<>();
                for (String child : childes) {
                    SiteParser task = new SiteParser(new URL(child), level + 1);
                    task.fork();
                    taskList.add(task);
                }
                for (SiteParser task : taskList) {
                    SiteNode child = task.join();
                    node.addChild(child);
                    LOGGER.info("next child node level {}", child.getLevel());
                }
            } catch (Exception exception) {
                LOGGER.error("{} \n{}", exception.getMessage(), exception.getStackTrace());
            }
            return node;
        }
    }

    private void childListAdd(Set<String> childes, String child) {
        Optional<String> isChild = childes.stream().filter(child::contains).findFirst();
        if (isChild.isEmpty()) {
            try {
                URL childURL = new URL(child);
                if (!childURL.getHost().replaceAll("www\\.", "").equals(host)) {
                    return;
                }
            } catch (Exception exception) {
                LOGGER.error("{} \n {} \n{}",child, exception.getMessage(), exception.getStackTrace());
            }
            if (childes.size() > 0) {
                childes.removeIf(c -> c.contains(child));
            }
            childes.add(child);
        }
    }

}
