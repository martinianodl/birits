package birits.evaluations;


import static birits.experiments.MusicAlignerClassifier.OUTPUT_FOLDER;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;


/**
 *
 * @author martinianodl
 */
public class Averages {
    public static void main(String[] args) throws IOException {
        try (BufferedWriter csvFinal = new BufferedWriter(new FileWriter("averages_imperfect"
                + ".csv"))) {
            csvFinal.write("part,num,type,MRR_A@1,MRR_F@1,MRR_A@5,MRR_F@5,MRR_A@10,MRR_F@10,MRR_A@25,MRR_F@25");
            csvFinal.newLine();
            File folder = new File("RESULTS/");
            File[] listOfFiles = folder.listFiles();
            for (File listOfFile : listOfFiles) {
                System.out.println(listOfFile);
                if (listOfFile.isFile() && listOfFile.getName().contains(".csv")) {
                    FileReader f1 = new FileReader(listOfFile.getAbsolutePath());
                    try (BufferedReader buffRead = new BufferedReader(f1)) {
                        String inst;
                        ArrayList<Double> MRR_A1 = new ArrayList<>();
                        ArrayList<Double> MRR_F1 = new ArrayList<>();
                        ArrayList<Double> MRR_A5 = new ArrayList<>();
                        ArrayList<Double> MRR_F5 = new ArrayList<>();
                        ArrayList<Double> MRR_A10 = new ArrayList<>();
                        ArrayList<Double> MRR_F10 = new ArrayList<>();
                        ArrayList<Double> MRR_A25 = new ArrayList<>();
                        ArrayList<Double> MRR_F25 = new ArrayList<>();
                        
                        while((inst = buffRead.readLine()) != null){
                            String[] split = inst.split(",");
                            if(!split[0].equals("fileId")){
                                MRR_A1.add(Double.parseDouble(split[2]));
                                MRR_F1.add(Double.parseDouble(split[3]));
                                MRR_A5.add(Double.parseDouble(split[4]));
                                MRR_F5.add(Double.parseDouble(split[5]));
                                MRR_A10.add(Double.parseDouble(split[6]));
                                MRR_F10.add(Double.parseDouble(split[7]));
                                MRR_A25.add(Double.parseDouble(split[8]));
                                MRR_F25.add(Double.parseDouble(split[9]));
                            }
                        }
                        String[] splitFile = listOfFile.getName().split("_");
                        System.out.println(splitFile[0] + ", " + splitFile[1] + ", " + splitFile[2].substring(0, splitFile[2].length()-4) + ", " + calculateAverage(MRR_A1) + ", " + calculateAverage(MRR_F1) + ", " + calculateAverage(MRR_A5) + ", " + calculateAverage(MRR_F5) + ", " + calculateAverage(MRR_A10) + ", " + calculateAverage(MRR_F10) + ", " + calculateAverage(MRR_A25) + ", " + calculateAverage(MRR_F25));
                        csvFinal.write(splitFile[0] + ", " + splitFile[1] + ", " + splitFile[2].substring(0, splitFile[2].length()-4) + ", " + calculateAverage(MRR_A1) + ", " + calculateAverage(MRR_F1) + ", " + calculateAverage(MRR_A5) + ", " + calculateAverage(MRR_F5) + ", " + calculateAverage(MRR_A10) + ", " + calculateAverage(MRR_F10) + ", " + calculateAverage(MRR_A25) + ", " + calculateAverage(MRR_F25));
                        csvFinal.newLine();
                    }
                }
            }
        }
	}
	
	private static double calculateAverage(ArrayList <Double> marks) {
		Double sum = (double) 0;
			if(!marks.isEmpty()) {
                            sum = marks.stream().map((mark) -> mark).reduce(sum, (accumulator, _item) -> accumulator + _item);
				return sum.doubleValue() / marks.size();
			}
			return sum;
		}

    }
