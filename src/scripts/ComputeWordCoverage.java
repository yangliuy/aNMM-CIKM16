package scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.FileUtil;

import conf.PathConfig;

public class ComputeWordCoverage {
	
	static double totalWordsNum = 0;
	
	static double overlapWordsNum = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length < 2){
			System.err.println("please input params: wordTFFile vocabWordsFile ");
			System.exit(1);
		}
		//String modelInputDataFolder = args[0];
		
		String wordTFFile = args[0];//modelInputDataFolder + "word.tf"; //"data/TRECQA/ModelInputData/word.tf"
		String overlapWordsFile = args[1];//modelInputDataFolder  + "webapWord2Vec.vocab"; //"data/TRECQA/WordVec/wiki.vocab";
		Map<String, Integer> wordTFMap = new HashMap<String, Integer>();
		buildWordTFMap(wordTFFile, wordTFMap);
		
		ArrayList<String> overlapTermLines = new ArrayList<String>();
		FileUtil.readLines(overlapWordsFile, overlapTermLines);
		int coveredTermNum = 0;
		for(String overlapTermLine : overlapTermLines){
			String word = overlapTermLine.split(" ")[0];
			if(!wordTFMap.containsKey(word)){
				System.out.println("can't find word in WordTFMap: " + word);
				continue;
			}
			overlapWordsNum += wordTFMap.get(word);
			coveredTermNum++;
		}
		System.out.println("overlapWordsNum / totalWordsNum = " + overlapWordsNum + " / " + totalWordsNum + " = " + overlapWordsNum / totalWordsNum);
		System.out.println("coverage of terms: " + (double)coveredTermNum / (double)wordTFMap.size());
	}

	//1005
	private static void buildWordTFMap(String wordTFFile,
			Map<String, Integer> wordTFMap) {
		// TODO Auto-generated method stub
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(wordTFFile, lines);
		for(String line : lines){
			wordTFMap.put(line.split("\t")[0], Integer.parseInt(line.split("\t")[1]));
			totalWordsNum += Integer.parseInt(line.split("\t")[1]);
		}
		System.out.println("wordTFMap size " + wordTFMap.size());
	}
}
