// 代码生成时间: 2025-10-05 17:41:32
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BulkFileRenamer {

    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\.[^.\s]+\$");

    /**
     * Main method to run the program.
     * @param args Arguments passed to the program.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: BulkFileRenamer <source_dir> <new_prefix>");
            return;
        }

        String sourceDir = args[0];
        String newPrefix = args[1];

        JavaSparkContext sc = new JavaSparkContext();
        try {
            List<File> files = Arrays.asList(new File(sourceDir).listFiles());
            if (files == null || files.isEmpty()) {
                System.err.println("No files found in directory: " + sourceDir);
                return;
            }

            JavaRDD<File> fileRDD = sc.parallelize(files);
            JavaRDD<String> renamedFiles = fileRDD.map(file -> renameFile(file, newPrefix));

            renamedFiles.collect().forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }

    /**
     * Renames a single file by adding a new prefix before the extension.
     * @param file The file to rename.
     * @param newPrefix The prefix to add to the file name.
     * @return The new file path.
     */
    private static String renameFile(File file, String newPrefix) {
        String fileName = file.getName();
        String extension = EXTENSION_PATTERN.matcher(fileName).replaceAll("");
        String newName = newPrefix + extension;
        File newFile = new File(file.getParent(), newName);
        boolean success = file.renameTo(newFile);
        if (!success) {
            throw new RuntimeException("Failed to rename file: " + file.getAbsolutePath());
        }
        return newFile.getAbsolutePath();
    }
}
