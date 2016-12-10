package scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.FileUtil;

/**Transfer the format of WikiQA data to the input format of aNMM model
 * 
 * author: lyang@cs.umass.edu
 * 
 */



public class ConvertWikiQADataFormat {
	
	static int numOfQuestions = 0;
	static int numOfCandidateAS = 0;
	static int numOfCorrectAS = 0;
	
	public static void main(String[] args) {
		
		String baseFolder = "/home/lyang/EclipseWorkspace/NLPIRNNTextMatchEclipse/data/WikiQA/ModelInputData/";
		
		ArrayList<String> queryLines = new ArrayList<String>();
		generateQsentFileHelper(baseFolder, "train", queryLines);
		generateQsentFileHelper(baseFolder, "dev", queryLines);
		generateQsentFileHelper(baseFolder, "test", queryLines);
		FileUtil.writeLines(baseFolder + "WikiQA.queries", queryLines);
		
		//For the test/dev data, we need to filter those queries for which there are no correct answers
		filterQueriesWithNoCoreectAnswersInTestDev(baseFolder, "dev");
		filterQueriesWithNoCoreectAnswersInTestDev(baseFolder, "test");
		
	}

	private static void filterQueriesWithNoCoreectAnswersInTestDev(String baseFolder, String dataType) {
		// TODO Auto-generated method stub
		//Construct a HashMap where
		//Key is qid
		//Value is the number of correct answers
		Map<String, Integer> qidCorrectAnsNumMap = new HashMap<String, Integer>();
		String wikiQAQsentFile = baseFolder + "WikiQA-" + dataType + ".qsent";
		String wikiQAQsentFilterNCAFile = baseFolder + "WikiQA-filterNoCA-" + dataType + ".qsent";
		ArrayList<String> qSentLines = new ArrayList<String>();
		FileUtil.readLines(wikiQAQsentFile, qSentLines);
		for(String qSLine : qSentLines){
			String [] tokens = qSLine.split("\t");
			if(tokens[3].equals("1")){//found correct answers
				if(qidCorrectAnsNumMap.containsKey(tokens[0])){
					qidCorrectAnsNumMap.put(tokens[0], qidCorrectAnsNumMap.get(tokens[0])+1);
				} else {
					qidCorrectAnsNumMap.put(tokens[0], 1);
				}
			}
		}
		System.out.println("number of queries with at least one correct answer  in " + dataType + " : " + qidCorrectAnsNumMap.size());
		ArrayList<String> filterLines = new ArrayList<String>();
		for(String qSLine : qSentLines){
			if(qidCorrectAnsNumMap.containsKey(qSLine.split("\t")[0])){
				filterLines.add(qSLine);
			}
		}
		FileUtil.writeLines(wikiQAQsentFilterNCAFile, filterLines);
	}

	private static void generateQsentFileHelper(String baseFolder, String dataType, ArrayList<String> queryLines) {
		// TODO Auto-generated method stub
		String wikiQAInputFileName = baseFolder + "WikiQA-" + dataType + ".tsv";
		String wikiQAQsentFile = baseFolder + "WikiQA-" + dataType + ".qsent";
		generateQsentFile(wikiQAInputFileName, wikiQAQsentFile, queryLines, dataType);
	}

	private static void generateQsentFile(String wikiQAInputFileName, String wikiQAQsentFile, ArrayList<String> queryLines, String dataType) {
		// TODO Auto-generated method stub
		ArrayList<String> wikiQAInputLines = new ArrayList<String>();
		FileUtil.readLines(wikiQAInputFileName, wikiQAInputLines);
		numOfQuestions = 0;
		numOfCandidateAS = 0;
		numOfCorrectAS = 0;
		
		ArrayList<String> qsentLines = new ArrayList<String>();
		
		for(int i = 0; i < wikiQAInputLines.size(); i++){
			if(i == 0) continue;
			String inputLine = wikiQAInputLines.get(i);
			String [] tokens = inputLine.split("\t");
			//train_1	train_1_asent_1	D0	1	the IRON LADY ; A Biography of Margaret Thatcher by Hugo Young -LRB- Farrar , Straus  Giroux -RRB-
			//Qid \t Sid \t DO \t label \t sentence
			String qid = tokens[0];
			String query = tokens[1];
			String sid = tokens[4];
			String label = tokens[6];
			String sent = tokens[5];
			String qsentLine = qid + "\t" + sid + "\tD0\t" + label + "\t" + sent;
			qsentLines.add(qsentLine);
			numOfCandidateAS++;
			if(label.equals("1")){
				numOfCorrectAS++;
			}
			if(!queryLines.contains(qid + "\t" + query + "\t" + query)){
				numOfQuestions++;
				queryLines.add(qid + "\t" + query + "\t" + query);
			}
		}
		FileUtil.writeLines(wikiQAQsentFile, qsentLines);
		System.out.println("num of questions in " + dataType + " : " + numOfQuestions);
		System.out.println("num of candidateAS in " + dataType + " : " + numOfCandidateAS);
		System.out.println("num of correct answer sentence in " + dataType + " : " + numOfCorrectAS);
	}
}
