// 代码生成时间: 2025-11-01 21:22:26
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.SparkConf;
import scala.Tuple2;

import java.util.List;
import java.util.Arrays;

public class ContentRecommendationApp {

    public static void main(String[] args) {
        // Set up Spark configuration and context
        SparkConf conf = new SparkConf().setAppName("ContentRecommendationApp");
        JavaSparkContext sc = new JavaSparkContext(conf);

        try {
            // Load and parse the data
            JavaRDD<String> data = sc.textFile("path_to_ratings_data");
            JavaRDD<Rating> ratings = data.map(s -> {
                String[] sarray = s.split(":");
                return new Rating(Integer.parseInt(sarray[0]), Integer.parseInt(sarray[1]), Double.parseDouble(sarray[2]));
            });

            // Build the recommendation model using ALS
            int rank = 10;
            int numIterations = 10;
            double lambda = 0.01;
            MatrixFactorizationModel model = ALS.train(ratings.rdd(), rank, numIterations, lambda);

            // Evaluate the model on training data (optional)
            // JavaRDD<Tuple2<Object, Object>> userProducts = ratings.map(r -> new Tuple2<>(r.user(), r.product()));
            // JavaPairRDD<Tuple2<Integer, Integer>, Double> predictions = JavaPairRDD.fromJavaRDD(userProducts).join(
            //     JavaPairRDD.fromJavaRDD(model.predictions().map(r -> new Tuple2<>(r.product(), r.rating())).mapToPair(r -> new Tuple2<>(r.product(), new Tuple2<>(r.user(), r.rating()))))
            // );
            // long totalMSE = predictions.mapToDouble(r -> Math.pow(r._1._2 - r._2, 2)).sum();
            // System.out.println("Mean Squared Error = " + totalMSE / predictions.count());

            // Make predictions and display
            // Example: Get top 5 recommended items for user 1
            JavaRDD<Integer> user = sc.parallelize(Arrays.asList(1));
            JavaRDD<Rating> userRatings = ratings.filter(r -> user.contains(r.user()));
            JavaRDD<Rating> userProducts = userRatings.map(r -> new Rating(r.user(), r.product(), r.rating()));
            JavaRDD<Rating> predictions = model.recommendationsForAllUsers(5).map(r -> new Rating(r.user(), r.product(), r.rating()));
            predictions.foreach(r -> System.out.println("User: " + r.user() + ", Product: " + r.product() + ", Rating: " + r.rating()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.stop();
        }
    }
}
