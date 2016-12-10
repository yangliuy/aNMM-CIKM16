package main.core;

import com.FileUtil;
import com.FuncUtils;

import conf.ModelParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**Class for Q&A documents for the data preprocessing 
 *
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */
public class QADocuments implements java.io.Serializable{
	private static final long serialVersionUID = 2L;
    
    public ArrayList<Document> docs; 
    public Map<String, Integer> termToIndexMap;
    public ArrayList<String> indexToTermMap;
    public Map<String,Integer> termCountMap;
    Map<String, int []> qidQsentMap;// Key: qid  Value: qSentWords

    public QADocuments(){
            docs = new ArrayList<Document>();
            termToIndexMap = new HashMap<String, Integer>();
            indexToTermMap = new ArrayList<String>();
            termCountMap = new HashMap<String, Integer>();
    }

    public void readQADocs(String trainFile, String queryFilePath, boolean isSubSampling, String dataName){
        ArrayList<String> queryLines = new ArrayList<String>();
        FileUtil.readLines(queryFilePath, queryLines);
        qidQsentMap = getQidQSentMap(queryLines); 
        
        System.out.println("current read doc: " + trainFile);
        ArrayList<String> docLines = new ArrayList<String>();
        FileUtil.readLines(trainFile, docLines);
        for(String docLine : docLines){
        	String[] docTokens = docLine.split("\t");
        	String qid = docTokens[0];
        	int[] qSentWords = qidQsentMap.get(qid);
        	Document doc = new Document(docLine, qSentWords);
        	if(doc.answerSentLength >= ModelParams.preprocessASentMinLen &&  doc.answerSentLength <= ModelParams.preprocessASentMaxLen){//As a preprocess step, filter sentences that are too short/too long.
            	if(dataName == "WebAP" && isSubSampling && doc.label == 0 ) {
            		//For negative samples in training data, we do sub-sampling
            		if(Math.random() > ModelParams.negativeSampleRatio){
            			continue;
            		}
            	}
            	docs.add(doc);
            }
        }
    }

    public Map<String, int []> getQidQSentMap(ArrayList<String> queryLines) {
        Map<String, int []> qidQsentMap = new HashMap<String, int []>();
        for(String queryLine : queryLines){
            String[] tokens = queryLine.split("\t");
            qidQsentMap.put(tokens[0], getWordIndexArrayFromSent(tokens[2]));
        }
        return qidQsentMap;
    }

    
    public int[] getWordIndexArrayFromSent(String qSent) {
        int[] wordIndexArray;
        ArrayList<String> words = new ArrayList<String>();
        FileUtil.tokenizeStanfordTKAndLowerCase(qSent, words);
        //1. !!! Can't remove noise words like numbers
        //e.g. Who received the Will Rogers Award in 1989? Which large U.S. city had the highest murder rate for 1988? In such questions, numbers and proper nouns are very important! Numbers can't be removed!
        //2. For NN baseline, we can maintain the stop words
        //Firstly maintain the stop words as the text8 file in word2vec tool 
        //3. Remove -lrb- and -rrb-
        //4. Remove punctuation and math symbols
        for(int i = 0; i < words.size(); i++){
            if(words.get(i).equals("-lrb-") || words.get(i).equals("-rrb-")){ // || Stopwords.isStopword(words.get(i)) || FileUtil.isNoiseWord(words.get(i)) 
                words.remove(i);
                i--;
            }
        }
        //Transfer word to index
        wordIndexArray = new int[words.size()];
        for(int i = 0; i < words.size(); i++){
            String word = words.get(i);
            if(!termToIndexMap.containsKey(word)){
                int newIndex = termToIndexMap.size();
                termToIndexMap.put(word, newIndex);
                indexToTermMap.add(word);
                termCountMap.put(word, new Integer(1));
                wordIndexArray[i] = newIndex;
            } else {
                wordIndexArray[i] = termToIndexMap.get(word);
                termCountMap.put(word, termCountMap.get(word) + 1);
            }
        }
        words.clear();
        return wordIndexArray;
    }

    public class Document implements java.io.Serializable {	
    	private static final long serialVersionUID = 1L;
    	
    	//Each document is a (questionSent, answerSent) pair. The primary key is (qid, answerSentId)
        public String qid;
        public String answerSentId;
        public int label;
        int questionSentLength;
        int answerSentLength;
        public int[] questionSentWords;
        int[] answerSentWords;
        // Qlen * Alen. Each line is corresponding to one query word. Different lines can have different lengths
        public ArrayList<ArrayList<Double>> QAMatchMatrix; 
        
        public Document(String qaSentPairLine, int[] qSentWords){
            //Read file and initialize word index array
            String [] qaSentPairTokens = qaSentPairLine.split("\t");
            qid = qaSentPairTokens[0];
            answerSentId = qaSentPairTokens[1];
            label = FuncUtils.convertLabelToScore(qaSentPairTokens[3]);
            String answerSent = qaSentPairTokens[4];
            answerSentWords = getWordIndexArrayFromSent(answerSent);
            //copy qSentWords
            questionSentWords = new int[qSentWords.length];
            for(int i = 0; i < qSentWords.length; i++){
                questionSentWords[i] = qSentWords[i];
            }
            //Also need to add terms in questions into the termToIndexMap and indexToTermMap to have a more complete vocabulary

            questionSentLength = questionSentWords.length;
            answerSentLength = answerSentWords.length;
            QAMatchMatrix = new ArrayList<ArrayList<Double>>(); //will add elements later
        }
    }
}
