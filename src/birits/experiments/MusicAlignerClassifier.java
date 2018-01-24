package birits.experiments;

import jaligner.Alignment;
import jaligner.Sequence;
import jaligner.SmithWatermanGotoh;
import static birits.experiments.MLFilter.readDataset;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixLoader;
import jaligner.util.SequenceParser;
import jaligner.util.SequenceParserException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;

/**
 *
 * @author martinianodl
 */
public class MusicAlignerClassifier {

    /**
     * Dataset Path
     */
    private static final String DATASET_PATH = "dataset/transposed_dataset.fa";

    /**
     * Query Path
     */
    private static final String QUERY_FOLDER = "QUERY/";

    /**
     * Substitution Matrix
     */
    private static final String MATRIX = "MUSSUM";

    /**
     * Get Query from Server or Local
     */
    private static final Boolean SERVER = false;

    /**
     * HoF Folder
     */
    public static final String OUTPUT_FOLDER = "HoF_Checkpoint/";

    /**
     * Results Folder
     */
    public static final String RESULTS_FOLDER = "RESULTS/";

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(MusicAlignerClassifier.class.getName());

    /**
     *
     * @param args
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        createOutputFolders();

        // Load dataset
        Instances featuresDataset = readDataset("dataset/complete_dataset.csv"); // Aways complete_dataset

        //Load Matrix
        Matrix matrix = MatrixLoader.load(MATRIX);

        // Manually Clean Memory
        Runtime mem = Runtime.getRuntime();

        // Hall of Fame (Best Scores)
        ArrayList<HoF> hof = new ArrayList();

        // Read dataset file and create dataset ArrayList
        ArrayList<String> d = new ArrayList();
        try (BufferedReader datasetIn = new BufferedReader(new FileReader(DATASET_PATH))) {
            String datasetLineReading;
            String datasetSong = "";

            while ((datasetLineReading = datasetIn.readLine()) != null) {
                if (datasetLineReading.startsWith(">")) {
                    datasetSong = datasetLineReading + "\n";
                    datasetSong += datasetIn.readLine();
                }
                d.add(datasetSong);
                datasetSong = "";
            }
            datasetIn.close();
        }

        // List of songs
        ArrayList<String> q = new ArrayList();

        // Get query file name from Server or Local
        String fileName = (SERVER) ? ConnServer.getQuery().split("\\.")[0] : "beginning_2_add.fa".split("\\.")[0];

        while (!fileName.equals("done")) {
            // Parse query file and load songs to ArrayList
            try (BufferedReader queryIn = new BufferedReader(new FileReader(QUERY_FOLDER + fileName + ".fa"))) {
                System.out.println(fileName);
                String queryLineReading;
                String querySong = "";
                while ((queryLineReading = queryIn.readLine()) != null) {
                    if (queryLineReading.startsWith(">")) {
                        querySong = queryLineReading + "\n";
                        querySong += queryIn.readLine();
                    }
                    q.add(querySong);
                    querySong = "";
                }
                queryIn.close();
            }

            // Align each song in the query list with the dataset
            double count = 0;
            for (String query : q) {
                count++;
                try {
                    String queryName = query.split("\n")[0].split(" ")[0].split("\\|")[1];

                    // Check if song was aligned before
                    if (!new File(OUTPUT_FOLDER + fileName + "/" + queryName + ".txt").exists()) {
                        for (String dataset : d) {
                            try {
                                String dataSetName = dataset.split("\n")[0].split(" ")[0].split("\\|")[1];

                                HoF h = new HoF();
                                Sequence s1 = SequenceParser.parse(query);
                                Sequence s2 = SequenceParser.parse(dataset);

                                Alignment alignment = SmithWatermanGotoh.align(s1, s2, matrix, 10f, 0.5f);

                                h.setScores(alignment.calculateScore());
                                h.setSong_names(dataSetName);
                                hof.add(h);
                            } catch (SequenceParserException e) {
                                logger.log(Level.SEVERE, "Failed running example: " + e.getMessage() + " " + fileName + " " + query + " " + dataset, e);
                            }
                        }
                        // Sort, save and clear hall of fame
                        Collections.sort(hof, (HoF h2, HoF h1) -> h1.scores.compareTo(h2.scores));
                        saveHoF(fileName, queryName, hof);
                        hof.clear();
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed running example: " + e + count, e);
                }

                // Update Progress
                updateProgress(count / q.size());

                // Clean memory
                mem.gc();
            }
            // clean query
            q.clear();

            // Filter 
            MLFilter.getResults(fileName, featuresDataset);

            if (SERVER) {
                ConnServer.queryDone(fileName + ".fa");
                fileName = ConnServer.getQuery().split("\\.")[0];
            } else {
                fileName = "done";
            }
            System.out.println("");
        }
        System.out.println("\nDONE");
    }

    private static void createOutputFolders() {
        File dir = new File(String.valueOf(OUTPUT_FOLDER));
        if (!dir.exists()) {
            dir.mkdir();
        }

        dir = new File(String.valueOf(RESULTS_FOLDER));
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private static void saveHoF(String fileName, String queryName, ArrayList<HoF> hof) {
        try {
            // create directory if it doesnt exist
            File directory = new File(String.valueOf(OUTPUT_FOLDER + fileName));
            if (!directory.exists()) {
                directory.mkdir();
            }
            // Save hall of fame to a file
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(OUTPUT_FOLDER + fileName + "/" + queryName + ".txt"));
            out.writeObject(hof);
            out.close();
        } catch (IOException e) {
        }
    }

    static void updateProgress(double progressPercentage) {
        final int width = 20; // progress bar width in chars

        DecimalFormat df2 = new DecimalFormat(".##");

        System.out.print("\r[");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) {
            System.out.print(".");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("]" + df2.format(progressPercentage * 100) + "%\t");
    }
}
