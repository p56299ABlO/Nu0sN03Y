// 代码生成时间: 2025-10-11 17:05:20
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class RandomNumberGenerator {
    
    // 主函数，程序入口
    public static void main(String[] args) {
        // 设置Spark配置
        SparkConf conf = new SparkConf().setAppName("RandomNumberGenerator").setMaster("local[*]");
        // 创建JavaSparkContext
        JavaSparkContext sc = new JavaSparkContext(conf);

        try {
            // 从命令行参数获取要生成的随机数个数和随机范围
            if (args.length != 3) {
                System.err.println("Usage: RandomNumberGenerator <num> <start> <end>");
                System.exit(-1);
            }
            int num = Integer.parseInt(args[0]);
            int start = Integer.parseInt(args[1]);
            int end = Integer.parseInt(args[2]);

            // 生成随机数
            JavaRDD<Integer> randomNumbers = generateRandomNumbers(sc, num, start, end);
            // 收集并打印随机数
            for (Integer randomNumber : randomNumbers.collect()) {
                System.out.println(randomNumber);
            }
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        } finally {
            // 停止SparkContext
            sc.stop();
        }
    }

    // 函数：生成随机数
    private static JavaRDD<Integer> generateRandomNumbers(JavaSparkContext sc, int num, int start, int end) {
        Random random = new Random();
        return sc.parallelize(Arrays.asList(generateRandomNumbers(num, start, end, random))).cache();
    }

    // 函数：生成单个分区的随机数
    private static List<Integer> generateRandomNumbers(int num, int start, int end, Random random) {
        List<Integer> randomNumbers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            randomNumbers.add(random.nextInt(end - start + 1) + start);
        }
        return randomNumbers;
    }
}
