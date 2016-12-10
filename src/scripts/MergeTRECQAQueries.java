package scripts;

import java.util.ArrayList;

import com.FileUtil;

public class MergeTRECQAQueries {
	
	
	public static void main(String[] args) {
		
		String trainAllQueryFile = "data/TRECQA/ModelInputData/train2393.cleanup.queries";
		String testQueryFile = "data/TRECQA/ModelInputData/test-less-than-40.manual-edit.queries";
		String devQueryFile = "data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.queries";
		
		String trecQAQueryAllFile = "data/TRECQA/ModelInputData/trecqa.queries";
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(trainAllQueryFile, lines);
		FileUtil.readLines(testQueryFile, lines);
		FileUtil.readLines(devQueryFile, lines);
		
		//qid titles descs
		//For TRECQA data, titles are the same with descs
		ArrayList<String> queryAllLines = new ArrayList<String>();
		for(String line : lines){
			String[] tokens = line.split("\t");
			queryAllLines.add(tokens[0] + "\t" + tokens[1] + "\t" + tokens[1]);
		}
		FileUtil.writeLines(trecQAQueryAllFile, queryAllLines);
	}
}
