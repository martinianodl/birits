package birits.experiments;

import static birits.experiments.MusicAlignerClassifier.OUTPUT_FOLDER;
import static birits.experiments.MusicAlignerClassifier.RESULTS_FOLDER;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author martinianodl
 */
public class MLFilter {

    public static final ArrayList<Integer> AT_N = new ArrayList<>(Arrays.asList(1, 5, 10, 25));

    public static void getResults(String fileName, Instances dataset) throws FileNotFoundException, IOException, ClassNotFoundException, Exception {
        System.out.println("\nFiltering: " + fileName);

        Instances imperfectDataset = readDataset("features/" + fileName + ".csv"); // String query_dataset

        int folds = 2;

        // For Reproducibility
        int seed = 5;
        Random rand = new Random(seed);
        Instances randDataset = new Instances(dataset);
        Instances randImperfectDataset = new Instances(imperfectDataset);
        randDataset.stratify(folds);
        randImperfectDataset.stratify(folds);

        // Create results file
        File csvOut = new File(RESULTS_FOLDER + fileName + ".csv");
        if (!csvOut.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(RESULTS_FOLDER + fileName + ".csv"), true /* append = true */))) {
                writer.println("fileId,predicted,MRR_A@1,MRR_F@1,MRR_A@5,MRR_F@5,MRR_A@10,MRR_F@10,MRR_A@25,MRR_F@25");
            }
        }

        // Cross validation
        for (int fold_n = 0; fold_n < folds; fold_n++) {
            System.out.println("FOLD " + fold_n);
            Instances train = randDataset.trainCV(folds, fold_n);
            Instances test = randImperfectDataset.testCV(folds, fold_n);

            ArrayList<String> songNames = new ArrayList<>();
            for (int i = 0; i < test.size(); i++) {
                songNames.add(test.get(i).toString(0));
            }

            // Remove song names from CSV
            Remove remove = new Remove();
            remove.setAttributeIndices("1");
            remove.setInputFormat(train);
            train = Filter.useFilter(train, remove);
            test = Filter.useFilter(test, remove);

            // Train SVM
            SMO cls = new SMO();
            cls.buildClassifier(train);

            // Predict classes
            for (int i = 0; i < test.size(); i++) {
                double clsLabel = cls.classifyInstance(test.instance(i));
                String queryName = songNames.get(i).split("/")[7].split("\\.")[0];
                String predicted = test.classAttribute().value((int) clsLabel);

                // 
                if (!CSVtoString(fileName).contains(queryName)) {
                    System.out.println(queryName);
                    ArrayList<HoF> hof = new ArrayList<>(getHoF(fileName, queryName));

                    if (hof.size() > 0) {
                        ArrayList<Float> MRR_aligned = new ArrayList<>();
                        ArrayList<Float> MRR_filtered = new ArrayList<>();

                        ArrayList<Integer> ranking = new ArrayList<>(getRanking(hof));
                        ArrayList<HoF> filtered = new ArrayList<>(filter(queryName, predicted, ranking, hof));
                        ranking = new ArrayList<>(getRanking(filtered));

                        for (Integer n : AT_N) {
                            MRR_aligned.add(MMR(n, ranking, hof, queryName));
                            MRR_filtered.add(MMR(n, ranking, filtered, queryName));
                        }

                        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(RESULTS_FOLDER + fileName + ".csv"), true))) {
                            writer.print(queryName + "," + predicted + ",");
                            boolean first = true;
                            for (int x = 0; x < AT_N.size(); x++) {
                                if (first) {
                                    first = false;
                                    writer.print(MRR_aligned.get(x) + "," + MRR_filtered.get(x));
                                } else {
                                    writer.print("," + MRR_aligned.get(x) + "," + MRR_filtered.get(x));
                                }
                            }
                            writer.println("");
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        System.out.println(checkCSV(fileName) ? "Done" : "Error: Less than 10199 files");
    }

    public static List<String> CSVtoString(String fileName) {
        List<String> csvFile = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(RESULTS_FOLDER + fileName + ".csv"))) {
            csvFile = stream.skip(1).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFile;
    }

    public static boolean checkCSV(String filename) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(RESULTS_FOLDER + filename + ".csv"))) {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            is.close();
            filename = filename.split("\\.")[0];

            if (count == 10200) {
                FileUtils.deleteDirectory(new File(MusicAlignerClassifier.OUTPUT_FOLDER + filename));
                return true;
            }
            return false;
        }
    }

    public static ArrayList<HoF> filter(String query, String predicted, ArrayList<Integer> ranking, ArrayList<HoF> hof) throws Exception {
        ArrayList<HoF> filtered = new ArrayList<>();
        int position = 0;
        ArrayList<HoF> found = new ArrayList<>();
        for (int rank = 1; rank <= Collections.max(ranking); rank++) {
            float ties = Collections.frequency(ranking, rank);
            if (ties > 0) {
                for (int i = 0; i < ties; i++) {
                    if (ranking.get(position) == rank) {
                        if (predicted.equals(hof.get(position).getSong_names().subSequence(0, 3))) {
                            found.add(hof.get(position));
                        }
                    }
                    position++;
                }
                if (found.size() > 0) {
                    filtered.addAll(found);
                    found.clear();
                } else {
                    position -= ties;
                    for (int i = 0; i < ties; i++) {
                        filtered.add(hof.get(position));
                        position++;
                    }
                }
            }
        }
        return filtered;
    }

    private static ArrayList<HoF> getHoF(String fileName, String queryName) {
        ArrayList<HoF> hof = new ArrayList<>();
        try {
            try (FileInputStream fis = new FileInputStream(OUTPUT_FOLDER + fileName + "/" + queryName + ".txt");
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                hof = (ArrayList) ois.readObject();
            }
        } catch (IOException ioe) {
            System.out.println("Not found " + ioe.getMessage());
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found " + c.getMessage());
        }
        return hof;
    }

    public static Instances readDataset(String csvFile) throws Exception {
        DataSource source = new DataSource(csvFile);
        Instances instancias = source.getDataSet();
        if (instancias.classIndex() == -1) {
            instancias.setClassIndex(instancias.numAttributes() - 1);
        }
        return instancias;
    }

    public static ArrayList<Integer> getRanking(ArrayList<HoF> hof) {
        ArrayList<Integer> ranking = new ArrayList<>();
        for (int i = 0; i < hof.size(); i++) {
            if (i > 0) {
                if (hof.get(i).getScores().equals(hof.get(i - 1).getScores())) {
                    ranking.add(ranking.get(i - 1));
                } else {
                    ranking.add(i + 1);
                }
            } else {
                ranking.add(i + 1);
            }
        }
        return ranking;
    }

    public static float MMR(int n, ArrayList<Integer> ranking, ArrayList<HoF> hof, String query) {
        float MRR = 0;
        int position = 0;
        for (int rank = 1; rank <= n; rank++) {
            float ties = Collections.frequency(ranking, rank);
            if (ties > 0) {
                float acerto = 0;
                for (int i = 0; i < ties; i++) {
                    acerto += (hof.get(position).getSong_names().equals(query) ? 1 : 0);
                    position++;
                }
                MRR += (acerto / ties) * (1.0 / rank);
                if (MRR > 0) {
                    break;
                }
            }
        }
        return MRR;
    }
}
