package xyz.grumpyfurrybear.youplayiguess;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class WordTest {
    public static void main(String[] args) throws IOException {
        File file = new File("word");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        Set<String> wordSet = new TreeSet<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] wordArray = line.split(",");
            for (String word : wordArray) {
                if (word.contains("，") || word.contains(" ") || word.contains(",")) {
                    System.out.println(word);
                }
                if (!StringUtils.isBlank(word) && !wordSet.contains(word)) {
                    wordSet.add(word);
                }
            }
        }
        System.out.println(wordSet.size());
        System.out.println(wordSet);
        System.out.println(buildAddWordSql(wordSet));
    }

    private static String buildAddWordSql(Set<String> words) {
        String sql = "insert into word (word) values ";
        for (String word : words) {
            sql += "('" + word + "'),";
        }
        return sql;
    }
}
