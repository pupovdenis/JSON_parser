import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @author Pupov
 */
public class App {
    public static void main(String[] args) throws IOException {
        Map<Integer, Integer> mapResult = new HashMap<Integer, Integer>();
        long time = System.currentTimeMillis();

        //На входе путь в директорию
//        String dir = args[0];
        String dir = "C:\\Users\\Denis\\Desktop\\forJavaStudy";

        File file = new File(dir);

        if (!file.exists() || !file.canRead()) {
            System.out.println("Файл не может быть прочитан");
            return;
        }

        //Парсим JSON
        System.out.println("Программа выполняется...");
        for (File fileWork : file.listFiles()) {

            FileInputStream in = new FileInputStream(fileWork);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn);
            ArchiveEntry entry = null;

            while ((entry = tarIn.getNextEntry()) != null) {
                if (entry.getSize() < 1) {
                    continue;
                }
                parser(tarIn, mapResult);
            }
            tarIn.close();
        }

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
        System.out.println("Количество групп: " + max + "\nID(s): " + builder.substring(0, builder.length()-2));
        time = System.currentTimeMillis() - time;
        System.out.printf("Время выполнения %,9.3f сек\n", time/1000.0);
    }

    public static void parser(TarArchiveInputStream tarIn, Map<Integer, Integer> mapResult) throws IOException {
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


//        System.out.println(user.gid + ": " + user.uids);

    }

    @JsonAutoDetect
    static class User {
        public int gid;

        @JsonDeserialize(as = HashSet.class)
        public Set<Integer> uids;

        User() {
        }
    }
}

//Есть коллекция JSON документов, описывающих состав участников групп социальной сети.
// Нужно найти и вывести ID участника, принимающего участие в наибольшем количестве групп.
