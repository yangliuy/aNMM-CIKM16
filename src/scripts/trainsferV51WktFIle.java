package scripts;

import java.util.ArrayList;

import com.FileUtil;

public class trainsferV51WktFIle {
	static String wktFile = "data/TRECQA/ModelRes/test_metrics_V5-1_bn200_vd700_04252016_TA/wtk_iter_5.wtk";
	
	public static void main(String[] args) {
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(wktFile, lines);
		ArrayList<String> transferedLines = new ArrayList<String>();
		int currentFirstDim = 0;
		for(String line : lines){
			String[] tokens = line.split("\t");
			System.out.println("Test: print tokens length " + tokens.length);
			String transferedLine = "";
			for(int i = currentFirstDim ; i < currentFirstDim + 200; i++){
				transferedLine += tokens[i] + "\t";
			}
			transferedLines.add(transferedLine);
			System.out.print("transferedLines.size " + transferedLines.size());
			currentFirstDim += 200;
		}
		
		FileUtil.writeLines(wktFile + ".fix", transferedLines);
		System.out.println("transferedLines[9]: " + transferedLines.get(9));
		
		
	}
}
