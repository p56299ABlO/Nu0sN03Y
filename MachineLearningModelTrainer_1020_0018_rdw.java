// 代码生成时间: 2025-10-20 00:18:29
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.Evaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import java.util.Arrays;
import java.util.List;

/**
 * A class to train a machine learning model using Spark MLlib
 */
public class MachineLearningModelTrainer {

    private SparkSession spark;
    private JavaSparkContext javaSparkContext;
    private Dataset<Row> trainingData;

    public MachineLearningModelTrainer(SparkSession spark, Dataset<Row> trainingData) {
        this.spark = spark;
        this.javaSparkContext = new JavaSparkContext(spark.sparkContext());
        this.trainingData = trainingData;
    }

    /**
     * Trains a linear regression model using the training data
     * @return The trained model
     */
    public PipelineModel trainLinearRegressionModel() {
        try {
            // Define the stages for the pipeline
            List<PipelineStage> stages = Arrays.asList(
                new StringIndexer().setInputCol("label").setOutputCol("indexedLabel"),
                new VectorAssembler().setInputCols("feature1", "feature2").setOutputCol("features"),
                new LinearRegression().setLabelCol("indexedLabel").setFeaturesCol("features")
            );

            // Create the pipeline
            Pipeline pipeline = new Pipeline().setStages(stages);

            // Split the data into training and test sets
            Dataset<Row>[] splits = trainingData.randomSplit(new double[]{0.8, 0.2});
            Dataset<Row> trainingSet = splits[0];
            Dataset<Row> testSet = splits[1];

            // Create a ParamGridBuilder to search for the best parameters
            ParamGridBuilder paramGrid = new ParamGridBuilder()
                .addGrid(new LinearRegression().setLabelCol("indexedLabel").setFeaturesCol("features").setMaxIter(ParamGridBuilder.array(10, 20)))
                .addGrid(new LinearRegression().setLabelCol("indexedLabel").setFeaturesCol("features").setRegParam(ParamGridBuilder.array(0.1, 0.01)));

            // Create a CrossValidator to evaluate the model
            CrossValidator crossval = new CrossValidator().setEstimator(pipeline).setEvaluator(new Evaluator()).setEstimatorParamMaps(paramGrid).setNumFolds(2);

            // Train and validate the model
            CrossValidatorModel cvModel = crossval.fit(trainingSet);

            // Evaluate the best model
            PipelineModel bestModel = cvModel.bestModel();
            // bestModel.transform(testSet).show();

            return bestModel;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
            .appName("MachineLearningModelTrainer")
            .master("local[*]")
            .getOrCreate();

        // Load data (replace with actual data loading code)
        Dataset<Row> trainingData = spark.read()
            .option("header", "true")
            .option("inferSchema", "true")
            .csv("path_to_your_training_data.csv");

        MachineLearningModelTrainer trainer = new MachineLearningModelTrainer(spark, trainingData);
        PipelineModel trainedModel = trainer.trainLinearRegressionModel();

        // Save the trained model (optional)
        trainedModel.save("path_to_save_model");
    }
}
