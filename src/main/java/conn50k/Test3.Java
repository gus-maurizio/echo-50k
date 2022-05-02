import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test3 {

    // time that tick() was called
    static long tickTime;

    // called at start of operation, for timing
    static void tick () {
        tickTime = System.nanoTime();
    }

    // called at end of operation, prints message and time since tick().
    static void tock (String action) {
        long mstime = (System.nanoTime() - tickTime) / 1000000;
        System.out.println(action + ": " + mstime + "ms");
    }

    // generate random strings of form AAAABBBCCCCC; a random 
    // number of characters each randomly repeated.
    static List<String> generateData (int itemCount) {

        Random random = new Random();
        List<String> items = new ArrayList<String>();
        long mean = 0;

        for (int n = 0; n < itemCount; ++ n) {
            StringBuilder s = new StringBuilder();
            int characters = random.nextInt(7) + 1;
            for (int k = 0; k < characters; ++ k) {
                char c = (char)(random.nextInt('Z' - 'A') + 'A');
                int rep = random.nextInt(95) + 5;
                for (int j = 0; j < rep; ++ j)
                    s.append(c);
                mean += rep;
            }
            items.add(s.toString());
        }

        mean /= itemCount;
        System.out.println("generated data, average length: " + mean);

        return items;

    }

    // match all strings in items to regexStr, do not precompile.
    static void regexTestUncompiled (List<String> items, String regexStr) {

        tick();

        int matched = 0, unmatched = 0;

        for (String item:items) {
            if (item.matches(regexStr))
                ++ matched;
            else
                ++ unmatched;
        }

        tock("uncompiled: regex=" + regexStr + " matched=" + matched + 
             " unmatched=" + unmatched);

    }

    // match all strings in items to regexStr, precompile.
    static void regexTestCompiled (List<String> items, String regexStr) {

        tick();

        Matcher matcher = Pattern.compile(regexStr).matcher("");
        int matched = 0, unmatched = 0;

        for (String item:items) {
            if (matcher.reset(item).matches())
                ++ matched;
            else
                ++ unmatched;
        }

        tock("compiled: regex=" + regexStr + " matched=" + matched + 
             " unmatched=" + unmatched);

    }

    // test all strings in items against regexStr.
    static void regexTest (List<String> items, String regexStr) {

        regexTestUncompiled(items, regexStr);
        regexTestCompiled(items, regexStr);

    }

    // generate data and run some basic tests
    public static void main (String[] args) {

        List<String> items = generateData(1000000);
        regexTest(items, "A*");
        regexTest(items, "A*B*C*");
        regexTest(items, "E*C*W*F*");

    }

}
