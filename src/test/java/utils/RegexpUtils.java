package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpUtils {

    private static final String openUrlPatter = "((http|https)://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&amp;:/~+#-]*[\\w@?^=%&amp;/~+#-])?)";


    public static List<String> getUrlsFromMessage(String text) {
        String[] steps = text.split("\n");
        List<String> urls = new ArrayList<>();

        for (String step : steps) {
            Pattern p = Pattern.compile(openUrlPatter);
            Matcher m = p.matcher(step);
            if (m.find())
                urls.add(m.group(1));
        }

        return urls;
    }
}
