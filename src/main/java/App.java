import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;


/**
 * @author Pupov
 */
public class App {
    public static void main(String[] args) throws IOException {
        Map<Integer, Integer> mapResult = new HashMap<Integer, Integer>();
        long time = System.currentTimeMillis();

        //На входе путь в директорию
        String dir = "http://dl.seuslab.ru/tz/junior2.tar.bz2";
        URL url = new URL(dir);

        File file = File.createTempFile("temp", null);
        FileUtils.copyURLToFile(url, file);

        if (!file.exists() || !file.canRead()) {
            System.out.println("Файл не может быть прочитан");
            return;
        }

        //Парсим JSON
        jsonParser(mapResult, file);

        //Ищем участников
        int max = 0;
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : mapResult.entrySet()) {
            if (entry.getValue() > max) {
                builder = new StringBuilder();
                builder.append(entry.getKey() + ", ");
                max = entry.getValue();
            }
            else if (entry.getValue() == max)
                builder.append(entry.getKey() + ", ");
        }

        //Выводим результат
        printResult(time, max, builder);
    }

    private static void jsonParser(Map<Integer, Integer> mapResult, File file) throws IOException {
        System.out.println("Программа выполняется...");
            FileInputStream in = new FileInputStream(file);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn);
            ArchiveEntry entry = null;

            while ((entry = tarIn.getNextEntry()) != null) {
                if (entry.getSize() < 1) {
                    continue;
                }
                makeParse(tarIn, mapResult);
            }
            tarIn.close();
    }

    private static void makeParse(TarArchiveInputStream tarIn, Map<Integer, Integer> mapResult) throws IOException {
        StringBuilder builder = new StringBuilder();

        while (tarIn.available() > 0) {
            builder.append((char) tarIn.read());
        }

        StringReader reader = new StringReader(builder.toString());
        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(reader, User.class);

        for (Integer a : user.uids) {
            if (mapResult.containsKey(a)) mapResult.put(a, mapResult.get(a) + 1);
            else mapResult.put(a, 1);
        }
    }

    private static void printResult(long time, int max, StringBuilder builder) {
        System.out.println("Количество групп: " + max + "\nID(s): " + builder.substring(0, builder.length()-2));
        time = System.currentTimeMillis() - time;
        System.out.printf("Время выполнения %,9.3f сек\n", time/1000.0);
    }

    @JsonAutoDetect
    static class User {
        public int gid;

        @JsonDeserialize(as = HashSet.class)
        Set<Integer> uids;

        User() {
        }
    }
}

