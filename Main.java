import java.io.*;
import java.util.*;

/**
 * Main Class Builds K Means model and outputs resulting clusters to txt files
 */
public class Main {
    /**
     * Main function calls function to read in files, run k means algorithm, then print results to a file
     * @param args String array representing the values passed in by the user
     * @throws IOException if file passed in by the user cannot be opened/read from
     */
    public static void main(String[] args) throws IOException {
        int k = 6;
        boolean converged = false;

        ArrayList<float[]> dataset = readFile(args[0]);
        ArrayList<float[]> previousCentroids;
        ArrayList<float[]> currentCentroids = initializeRandomCentroids(k, dataset);

        while (!converged) {
            previousCentroids = new ArrayList<>(currentCentroids);
            ArrayList<ArrayList<float[]>> clusters = determineCluster(currentCentroids, dataset);
            currentCentroids = redetermineCentroid(clusters);

            converged = isConverged(previousCentroids, currentCentroids);
        }

        ArrayList<ArrayList<float[]>> finalClusters = determineCluster(currentCentroids, dataset);
        outputClusters(finalClusters, k);
    }

    /**
     * Function randomly selects k points from the dataset to become the initial data that we will cluster around
     * @param k int representing the number of clusters or centroids
     * @param dataset ArrayList<float[]> representing the whole collection of data/numbers
     * @return ArrayList<float[]> representing the random data instances that we have randomly picked to begin clustering around
     */
    public static ArrayList<float[]> initializeRandomCentroids(int k, ArrayList<float[]> dataset) {
        ArrayList<Integer> randomIndices = new ArrayList<>();
        ArrayList<float[]> initialCentroids = new ArrayList<>();

        Random random = new Random();

        while (randomIndices.size() < k) {
            int randomIndex = random.nextInt(dataset.size());

            if (!randomIndices.contains(randomIndex)) {
                randomIndices.add(randomIndex);
                initialCentroids.add(dataset.get(randomIndex));
            }
        }

        return initialCentroids;
    }

    /**
     * Function calculates and returns mean of a cluster in order to find the new center of a specific cluster
     * @param clusters ArrayList<ArrayList<float[]>> representing the data contained in each of the clusters
     * @return ArrayList<float[]> representing the new centroids / mean of each of the clusters
     */
    public static ArrayList<float[]> redetermineCentroid(ArrayList<ArrayList<float[]>> clusters) {
        ArrayList<float[]> newCentroids = new ArrayList<>();

        for (ArrayList<float[]> cluster : clusters) {
            if (cluster.size() == 0) {
                continue;
            }

            int featureCount = cluster.get(0).length;
            float[] newCentroid = new float[featureCount];

            for (float[] data : cluster) {
                for (int featureIndex = 0; featureIndex < featureCount; featureIndex++) {
                    newCentroid[featureIndex] += data[featureIndex];
                }
            }

            for (int featureIndex = 0; featureIndex < featureCount; featureIndex++) {
                newCentroid[featureIndex] = newCentroid[featureIndex] / cluster.size();
            }

            newCentroids.add(newCentroid);
        }

        return newCentroids;
    }

    /**
     * Function loops through each data instance in the dataset and finds/reassigns the cluster it is closest to in distance
     * @param centroids ArrayList<float[]> representing the centers of the clusters
     * @param dataset ArrayList<float[]> representing the whole collection of data/numbers
     * @return ArrayList<ArrayList<float[]>> representing the clusters and the newly reassigned data within them
     */
    public static ArrayList<ArrayList<float[]>> determineCluster(ArrayList<float[]> centroids, ArrayList<float[]> dataset) {
        ArrayList<ArrayList<float[]>> clusters = new ArrayList<>();

        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        for (float[] data : dataset) {
            int cluster = 0;
            float smallestDistance = Float.MAX_VALUE;

            for (int i = 0; i < centroids.size(); i++) {
                float distance = calculateEuclideanDistance(centroids.get(i), data);

                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    cluster = i;
                }
            }
            clusters.get(cluster).add(data);
        }

        return clusters;
    }

    /**
     * Function to determine if the centroids of an old instance of the clusters and a new instance are exactly the same
     * @param centroids1 ArrayList<float[]> representing the first/old set of centroids that we want to check for equivalency
     * @param centroids2 ArrayList<float[]> representing the second/new set of centroids that we want to check for equivalency
     * @return boolean representing whether the centroids have changed or have not changed
     */
    public static boolean isConverged(ArrayList<float[]> centroids1, ArrayList<float[]> centroids2) {
        for (int i = 0; i < centroids1.size(); i++) {
            for (int featureIndex = 0; featureIndex < centroids1.get(i).length; featureIndex++) {
                if (centroids1.get(i)[featureIndex] != centroids2.get(i)[featureIndex]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Function to calculate the Euclidean distance between two pieces of data (a centroid and another data piece)
     * @param startingPoint float[] representing the first row of data that we want to measure the distance between
     * @param endingPoint float[] representing the second row of data that we want to measure the distance between
     * @return float representing the Euclidean distance between the rows
     */
    public static float calculateEuclideanDistance(float[] startingPoint, float[] endingPoint) {
        float sum = (float) 0.0;

        for (int i = 0; i < startingPoint.length; i++) {
            sum += (float) Math.pow(startingPoint[i] - endingPoint[i], 2);
        }

        return (float) Math.sqrt(sum);
    }

    /**
     * Function to read in a csv file
     * @return ArrayList<float[]> representing the dataset that has been read from the given file
     * @throws IOException if file passed in by the user cannot be opened/read from
     */
    public static ArrayList<float[]> readFile(String filepath) throws IOException {
        File file = new File(filepath);
        Scanner scanner = new Scanner(file);

        ArrayList<float[]> dataset = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");

            float[] row = new float[values.length];

            for (int i = 0; i < values.length; i++) {
                row[i] = Float.parseFloat(values[i]);
            }

            dataset.add(row);
        }

        scanner.close();

        return dataset;
    }

    /**
     * Function to write the data within each cluster to their own individual files
     * @param finalClusters ArrayList<ArrayList<float[]>> representing the clusters and their data after they have converged
     * @param k int representing the number of clusters
     * @throws IOException if file cannot be written to
     */
    public static void outputClusters(ArrayList<ArrayList<float[]>> finalClusters, int k) throws IOException {
        for (int clusterNumber = 0; clusterNumber < k; clusterNumber++) {
            FileWriter file = new FileWriter("cluster_" + (clusterNumber + 1) + ".txt");
            ArrayList<float[]> clusterData = finalClusters.get(clusterNumber);

            for (float[] row : clusterData) {
                for (int i = 0; i < row.length - 1; i++) {
                    file.write(row[i] + ", ");
                }

                file.write("\n");
            }

            file.close();
        }
    }
}