import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class SiteNode implements Comparable<SiteNode> {

    @Getter
    private final Set<SiteNode> childNodes = new TreeSet<>();

    @Getter
    private final URL url;

    @Getter
    private final int level;

    public SiteNode(int level, URL url) {
        this.level = level;
        this.url = url;
    }

    public void addChild(SiteNode child) {
        synchronized (childNodes) {
            childNodes.add(child);
        }
    }

    public void addChild(URL url) {
        SiteNode child = new SiteNode(level + 1, url);
        addChild(child);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\t".repeat(level)).append(url.toString());
        if (childNodes.size() > 0) {
            childNodes.forEach(c -> buffer.append(System.lineSeparator()).append(c));
        }
        return buffer.toString();
    }

    @Override
    public int compareTo(SiteNode o) {
        return url.toString().compareTo(o.url.toString());
    }
}
