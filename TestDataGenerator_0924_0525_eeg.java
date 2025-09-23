// 代码生成时间: 2025-09-24 05:25:13
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import scala.Tuple2;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class TestDataGenerator {

    // Configuration for Apache Spark
    private SparkConf conf;
    private JavaSparkContext sc;

    // Constructor to initialize the Spark configuration and context
    public TestDataGenerator() {
        this.conf = new SparkConf().setAppName("TestDataGenerator").setMaster("local[*]");
        this.sc = new JavaSparkContext(conf);
    }

    // Method to generate test data
    public JavaRDD<String> generateTestData(int numberOfRecords) {
        // Error handling: Check if the number of records is positive
        if (numberOfRecords <= 0) {
            throw new IllegalArgumentException("Number of records must be positive.");
        }

        // Generate the test data using a lambda function
        return sc.parallelize(1).flatMap(x -> {
            List<String> testData = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < numberOfRecords; i++) {
                // Generate a random test record
                String record = "Record " + i + ": " + random.nextInt(100);
                testData.add(record);
            }
            return testData.iterator();
        });
    }

    // Method to stop the Spark context
    public void stop() {
        if (sc != null) {
            sc.stop();
        }
    }

    // Main method to run the test data generator
    public static void main(String[] args) {
        try {
            TestDataGenerator generator = new TestDataGenerator();
            int numberOfRecords = 100; // Number of records to generate
            JavaRDD<String> testData = generator.generateTestData(numberOfRecords);

            // Collect and print the test data
            testData.collect().forEach(System.out::println);

            // Stop the Spark context
            generator.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
