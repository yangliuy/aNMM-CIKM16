package scripts;

import java.io.File;
import java.util.ArrayList;

import com.FileUtil;

public class MergeWebAPInputSentFilesIntoOne {
	
	public static void main(String[] args) {
		String trainFolder = "data/WebAP/OriginalSentData/train/";
		String testFolder = "data/WebAP/OriginalSentData/test/";
		String upperLevelFolder = "data/WebAP/OriginalSentData/";
		String singleTrainFile = upperLevelFolder + "train.qsent";
		String singleTestFile = upperLevelFolder + "test.qsent";
		
		mergeMultipleFilesIntoOne(trainFolder, singleTrainFile);
		mergeMultipleFilesIntoOne(testFolder, singleTestFile);
	}

	private static void mergeMultipleFilesIntoOne(String trainFolder, String singleTrainFile) {
		// TODO Auto-generated method stub
		ArrayList<String> lines = new ArrayList<String>();
		for(File file : new File(trainFolder).listFiles()){
			FileUtil.readLines(file.getAbsolutePath(), lines);
		}
		FileUtil.writeLines(singleTrainFile, lines);
	}
}
