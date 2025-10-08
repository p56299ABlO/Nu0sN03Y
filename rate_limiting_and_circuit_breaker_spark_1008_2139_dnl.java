// 代码生成时间: 2025-10-08 21:39:51
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.Durations;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import scala.Tuple2;

/**
 * Rate Limiting and Circuit Breaker using Java and Spark framework
 * This class demonstrates how to implement rate limiting and circuit breaker in a Spark streaming application.
 */
public class RateLimitingAndCircuitBreakerSpark {

    private static final int MAX_CONCURRENT_REQUESTS = 5;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int TIMEOUT = 5; // seconds
    private static final int ERROR_THRESHOLD = 5; // error threshold to trip circuit
    private static final int SUCCESSFUL_THRESHOLD = 3; // successful requests to open circuit
    private static int errorCount = 0; // error counter
    private static int successCount = 0; // successful request counter
    private static boolean isCircuitOpen = false; // circuit breaker state
    private static ExecutorService threadPool;

    public static void main(String[] args) throws InterruptedException {
        // Set up Spark configuration and context
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("RateLimitingAndCircuitBreaker");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(1));

        // Create a thread pool for rate limiting
        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Define the DStream for receiving data
        JavaDStream<String> lines = jsc.socketTextStream("localhost", 9999);

        // Process the stream with rate limiting and circuit breaker
        JavaDStream<String> processedStream = lines.transformToPair(
                rdd -> {
                    JavaRDD<Tuple2<String, Integer>> mappedRDD = rdd.mapToPair(s -> new Tuple2<>(s, 1));
                    return mappedRDD;
                })
                .reduceByKey((a, b) -> a + b)
                .mapPartitionsToPair(partition -> {
                    return new RateLimitingCircuitBreaker().call(partition, threadPool, MAX_CONCURRENT_REQUESTS, TIMEOUT);
                });

        // Print the processed stream
        processedStream.print();

        // Start the streaming computation
        jsc.start();
        jsc.awaitTermination();
    }

    // Hystrix command implementation for rate limiting and circuit breaker
    private static class RateLimitingCircuitBreaker extends HystrixCommand<JavaPairRDD<String, Integer>> {
        private final JavaPairRDD<String, Integer> inputRDD;
        private final ExecutorService threadPool;
        private final int maxConcurrentRequests;
        private final int timeout;

        public RateLimitingCircuitBreaker() {
            super(
                    HystrixCommandGroupKey.Factory.asKey("RateLimitingGroup"),
                    HystrixCommandKey.Factory.asKey("RateLimitingCommand"),
                    HystrixThreadPoolKey.Factory.asKey("RateLimitingPool"),
                    HystrixThreadPoolProperties.Setter().withCoreSize(THREAD_POOL_SIZE)
            );
            this.inputRDD = null;
            this.threadPool = null;
            this.maxConcurrentRequests = MAX_CONCURRENT_REQUESTS;
            this.timeout = TIMEOUT;
        }

        public RateLimitingCircuitBreaker(JavaPairRDD<String, Integer> inputRDD, ExecutorService threadPool, int maxConcurrentRequests, int timeout) {
            super(
                    HystrixCommandGroupKey.Factory.asKey("RateLimitingGroup"),
                    HystrixCommandKey.Factory.asKey("RateLimitingCommand"),
                    HystrixThreadPoolKey.Factory.asKey("RateLimitingPool"),
                    HystrixThreadPoolProperties.Setter().withCoreSize(THREAD_POOL_SIZE)
            );
            this.inputRDD = inputRDD;
            this.threadPool = threadPool;
            this.maxConcurrentRequests = maxConcurrentRequests;
            this.timeout = timeout;
        }

        @Override
        protected JavaPairRDD<String, Integer> run() throws Exception {
            if (isCircuitOpen) {
                throw new RuntimeException("Circuit is open.");
            }

            JavaPairRDD<String, Integer> outputRDD = inputRDD.mapPartitionsToPair(s -> {
                return new RateLimitingPartition(s, threadPool, maxConcurrentRequests, timeout).call();
            });

            if (outputRDD.isEmpty()) {
                errorCount++;
                if (errorCount >= ERROR_THRESHOLD) {
                    isCircuitOpen = true;
                }
            } else {
                successCount++;
                if (successCount >= SUCCESSFUL_THRESHOLD) {
                    if (isCircuitOpen) {
                        isCircuitOpen = false;
                    successCount = 0;
                } else {
                    successCount = 0;
                }
            }
            return outputRDD;
        }
    }

    // Rate limiting partition implementation
    private static class RateLimitingPartition extends HystrixCommand<Iterator<Tuple2<String, Integer>>> {
        private final Iterator<Tuple2<String, Integer>> inputPartition;
        private final ExecutorService threadPool;
        private final int maxConcurrentRequests;
        private final int timeout;

        public RateLimitingPartition(Iterator<Tuple2<String, Integer>> inputPartition, ExecutorService threadPool, int maxConcurrentRequests, int timeout) {
            super(
                    HystrixCommandGroupKey.Factory.asKey("RateLimitingGroup"),
                    HystrixCommandKey.Factory.asKey("RateLimitingPartitionCommand"));
            this.inputPartition = inputPartition;
            this.threadPool = threadPool;
            this.maxConcurrentRequests = maxConcurrentRequests;
            this.timeout = timeout;
        }

        @Override
        protected Iterator<Tuple2<String, Integer>> run() throws Exception {
            return inputPartition;
        }
    }
}
