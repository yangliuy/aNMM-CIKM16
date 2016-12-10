package main.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.FileUtil;
import com.Stopwords;

import conf.ModelParams;
import main.core.QADocuments.Document;

/**Compute extra simple features for combining
 * Consider simple additional features like WO,
 * IDF weighted WO, BM25, QL, etc.
 * For some implementation of WO, BM25, QL features
 * Merge nntextmatch score as the last feature into
 * the feature file
 * @author Liu Yang
 * @email lyang@cs.umass.edu
 */

public class ComputeExtraFeatsToCombineAndMerge {
	
	static Map<String, Double> termContainSentCountMap = new HashMap<String, Double>();//Key term, Value number of sentences containing this term (document frequency)
	static Map<String, Double> answerIDToMatchScoreMap = new HashMap<String, Double>();//Key answer sent ID, Value match score learnt from nntextmatch model
    static double sentTotalNum = 0;
    static double avgSentLen = 0;
    static double k1 = 1.2;
    static double b = 0.75;
    static int zeroLenSentCounter = 0;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		if(args.length < 1) {
			System.err.println("please input params: queryFile trainFile validationFile testFile dataName ModelInputDataFolder trainNNScoreFile testNNScoreFile featSet modelVersion.   all paths should be with /");
			System.exit(1);
		}
		//Sample command line parameters
		//data/TRECQA/ModelInputData/trecqa.queries data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent
		//TRECQA data/TRECQA/ModelInputData/ 
		//data/TRECQA/ModelRes/test_metrics_V5-1_bn200_vd700_04252016/nntextmatch_iter_20_train.score
		//data/TRECQA/ModelRes/tuneBinVecNumValid_V5-1/tune_binnum_200_vecdim_700/nntextmatch_iter_20.score
		//data/TRECQA/ModelRes/test_metrics_V5-1_bn200_vd700_04252016/nntextmatch_iter_20_test.score
		//For train-less and train-all use the score of the corresponding file
		//For dev and test use the score of training with TRAIN-ALL
		String queryFile = args[0];
		String trainFile = args[1];
		String validFile = args[2]; //validation file is used to tune parameters
		String testFile = args[3];
		String dataName = args[4]; //options: WebAP TRECQA YahooCQA(to be added)
		String ModelInputDataFolder = args[5];
		String trainNNScoreFile = args[6]; //data/TRECQA/ModelRes/test_metrics_V5-1_bn200_vd700_04252016/nntextmatch_iter_20_train.score
		String validNNScoreFile = args[7]; //data/TRECQA/ModelRes/tuneBinVecNumValid_V5-1/tune_binnum_200_vecdim_700/nntextmatch_iter_20.score   !!!the score files under tune data path is corresponding to valid files
		String testNNScoreFile = args[8]; //data/TRECQA/ModelRes/test_metrics_V5-1_bn200_vd700_04252016/nntextmatch_iter_20_test.score
		String featSet = args[9];//AllFeat or WO or BM25 or QL
		String modelVersion = args[10];//Experiments with V4-1/V4-3/V5-1
		
        //Compute the following 6 simple text match features to combine with nntextmatch score
        //1. 4 word overlap features:  word co-occurrence count between all words, IDF weighted word co-occurrence count between all words, the previous two features between only non-stop words
        //2. BM25 score
        //3. QL score
        QADocuments trainQADocSet= new QADocuments();
        QADocuments validQADocSet = new QADocuments();
        QADocuments testQADocSet = new QADocuments();
        QADocDataPreprocess QADP = new QADocDataPreprocess();
        trainQADocSet.readQADocs(trainFile, queryFile, false, dataName); //Do subsampling only when isSubsampling is true and dataName = "WebAP"; Otherwise, we don't do data subsampling
        validQADocSet.readQADocs(validFile, queryFile, false, dataName);
        testQADocSet.readQADocs(testFile, queryFile, false, dataName);
        
        QADP.printWordDic(trainQADocSet, ModelInputDataFolder);
        QADP.printQASentWordIndex(trainQADocSet, ModelInputDataFolder);
        QADP.printWordCountFile(trainQADocSet, ModelInputDataFolder);
        QADP.printTermIDFFile(trainQADocSet, ModelInputDataFolder);
        //FileUtil.saveClass(qaDocSet, serializedData);
        //qaDocSet = FileUtil.loadClass(qaDocSet, serializedData);
        System.out.println("total number of QA sent pairs in training data: " + trainQADocSet.docs.size());
        System.out.println("total number of QA sent pairs in valid data: " + validQADocSet.docs.size());
        System.out.println("total number of QA sent pairs in testing data: " + testQADocSet.docs.size());
        
        Map<String, Double> backgroundWD = new HashMap<String, Double>();
        initBackgroundWD(backgroundWD, trainQADocSet, validQADocSet, testQADocSet);
        initTermContainSentCountMap(trainQADocSet, validQADocSet, testQADocSet);
        initAnswerIDToMatchScoreMap(trainNNScoreFile, validNNScoreFile, testNNScoreFile);
        
        generateFeatureFile(trainFile, trainQADocSet, ModelInputDataFolder, backgroundWD, featSet, modelVersion);
        generateFeatureFile(validFile, validQADocSet, ModelInputDataFolder, backgroundWD, featSet, modelVersion);
        generateFeatureFile(testFile, testQADocSet, ModelInputDataFolder, backgroundWD, featSet, modelVersion);
	}

	//Key answer sent ID, Value match score learnt from nntextmatch model
	private static void initAnswerIDToMatchScoreMap(String trainNNScoreFile, String validNNScoreFile,
			String testNNScoreFile) {
		// TODO Auto-generated method stub
		GetNNTextmatchScoreGivenScoreFile(trainNNScoreFile);
		GetNNTextmatchScoreGivenScoreFile(validNNScoreFile);
		GetNNTextmatchScoreGivenScoreFile(testNNScoreFile);
	}

	private static void GetNNTextmatchScoreGivenScoreFile(String trainNNScoreFile) {
		// TODO Auto-generated method stub
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(trainNNScoreFile, lines);
		for(String line : lines){
			String[] tokens = line.split(" ");
			String answerID = tokens[2];
			String nntextmatchScore = tokens[4];
			answerIDToMatchScoreMap.put(answerID, Double.valueOf(nntextmatchScore));
		}
	}

	private static void initBackgroundWD(Map<String, Double> backgroundWD, QADocuments trainQADocSet, QADocuments validQADocSet, QADocuments testQADocSet) {
		// TODO Auto-generated method stub
		addQADocSetTerms(backgroundWD, trainQADocSet);
		addQADocSetTerms(backgroundWD, validQADocSet);
		addQADocSetTerms(backgroundWD, testQADocSet);
		double totalTermCount = 0;
		for(String term : backgroundWD.keySet()){
			totalTermCount += backgroundWD.get(term);
		}
		for(String term : backgroundWD.keySet()){
			backgroundWD.put(term, backgroundWD.get(term) / totalTermCount);
		}
	}

	private static void addQADocSetTerms(Map<String, Double> backgroundWD, QADocuments trainQADocSet) {
		// TODO Auto-generated method stub
		for(String term : trainQADocSet.termCountMap.keySet()){
			if(backgroundWD.containsKey(term)){
				backgroundWD.put(term, backgroundWD.get(term) + trainQADocSet.termCountMap.get(term).doubleValue());
			} else {
				backgroundWD.put(term, trainQADocSet.termCountMap.get(term).doubleValue());
			}
		}
	}

	private static void generateFeatureFile(String trainFile, QADocuments trainQADocSet, String modelInputDataFolder, Map<String, Double> backgroundWD, String featSet, String modelVersion) throws Exception {
		// TODO Auto-generated method stub
		//Compute simple text matching features including WO, IDF weighted WO, BM25 and QL
		String featureFile = trainFile + "_" + modelVersion + "_Add_" + featSet + ".feat";
		//Feature file format: label qid:<qid> <feature>:<value> <feature>:<value> <feature>:<value> <feature>:<value> ... <feature>:<value> # <info> could be answerSent or answerID
		//Example
//		3 qid:1 1:1 2:1 3:0 4:0.2 5:0 # 1A
//		2 qid:1 1:0 2:0 3:1 4:0.1 5:1 # 1B 
//		1 qid:1 1:0 2:1 3:0 4:0.4 5:0 # 1C
		//6 simple features: WO, IDFWeightedWO, WOStop, IDFWeightedWOStop, BM25, QL
		ArrayList<String> featureLines = new ArrayList<String>();
		double[] WOFeats = new double[4];//WO, IDFWeightedWO, WOStop, IDFWeightedWOStop
		
		for(Document doc : trainQADocSet.docs){
			computeWOFeats(WOFeats, doc, trainQADocSet);
			//double BM25 = computeBM25Score();
			double QL = computeQLScore(doc, trainQADocSet, backgroundWD);
			double BM25 = computeBM25Score(doc, trainQADocSet);
			String featLine = doc.label + " qid:" + doc.qid ;
			if(featSet.equals("AllFeat")) {
				featLine += " 1:" + WOFeats[0] +  
						" 2:" + WOFeats[1] + 
						" 3:" + WOFeats[2] + 
						" 4:" + WOFeats[3] + 
						" 5:" + QL + 
						" 6:" + BM25 + 
						" 7:" + answerIDToMatchScoreMap.get(doc.answerSentId) +  " # ";
			} else if(featSet.equals("WO")){
				featLine += " 1:" + answerIDToMatchScoreMap.get(doc.answerSentId) +
						" 2:" + WOFeats[0] +  
						" 3:" + WOFeats[1] + 
						" 4:" + WOFeats[2] + 
						" 5:" + WOFeats[3] +  " # ";
			} else if(featSet.equals("BM25")){
				featLine += " 1:" + answerIDToMatchScoreMap.get(doc.answerSentId) +
						" 2:" + BM25 + " # ";
			} else if(featSet.equals("QL")){
				featLine += " 1:" + answerIDToMatchScoreMap.get(doc.answerSentId) +
						" 2:" + QL + " # ";
			} else {
				System.err.println("Error: unsupported feature set type!");
				System.exit(1);
			}
			//add answer ID as comment part 
			featLine += doc.answerSentId;
			//add answer sentences as comment part for debugging
//			for(int i = 0; i < doc.answerSentLength; i++){
//				featLine += trainQADocSet.indexToTermMap.get(doc.answerSentWords[i]) + " ";
//			}
			featureLines.add(featLine);
		}
		FileUtil.writeLines(featureFile, featureLines);
	}
	
	   private static void computeWOFeats(double[] wOFeats, Document doc, QADocuments trainQADocSet) {
		// TODO Auto-generated method stub
		   double WO = 0;
		   double idfWeightedWO = 0;
		   double WOStop = 0;
		   double idfWeightedWOStop = 0;
		   if(doc.questionSentLength == 0){
			   System.err.println("In computeWOFeats function, find question with zero length !");
			   System.exit(1);
		   } else {
			   for(int qTerm : doc.questionSentWords){
				   boolean contained = false;
				   for(int aTerm : doc.answerSentWords){
					   if(aTerm == qTerm) {
						   contained = true;
						   break;
					   }
				   }
				   if(contained){
					   double idf = 0;
					   if(termContainSentCountMap.containsKey(qTerm)){
						   idf = Math.log(sentTotalNum / termContainSentCountMap.get(qTerm));
					   } else {
						   //System.out.println("Can't find qTerm in termContainSentCountMap: " + qTerm);
						   idf = 8.478;// DF = 1
					   }
					   //System.out.println("trainQADocSet.indexToTermMap.get(qTerm)) : " + trainQADocSet.indexToTermMap.get(qTerm));
					   WO++;
					   idfWeightedWO += idf;
					   if(!Stopwords.isStopword(trainQADocSet.indexToTermMap.get(qTerm))){
						   WOStop++;
						   idfWeightedWOStop += idf;
					   }
				   }
			   }
			   wOFeats[0] = WO;
			   wOFeats[1] = idfWeightedWO;
			   wOFeats[2] = WOStop;
			   wOFeats[3] = idfWeightedWOStop;
		   }   
	   }
	    
	    private static double computeQLScore(Document doc, QADocuments trainQADocSet, Map<String, Double> backgroundWD) throws Exception {
	        //compute the QL socre based on the likelihood of the query being generated from the sentences
	        String[] queryTerms = new String[doc.questionSentLength];
	        String[] sentTerms = new String[doc.answerSentLength];
	        double mu = ModelParams.mu;
	        
	        for(int i = 0; i < doc.questionSentLength; i++){
	        	queryTerms[i] = trainQADocSet.indexToTermMap.get(doc.questionSentWords[i]);
	        }
	        
	        for(int i = 0; i < doc.answerSentLength; i++){
	        	sentTerms[i] = trainQADocSet.indexToTermMap.get(doc.answerSentWords[i]);
	        }
	        
	        Map<String, Integer> queryTermCountMap = computeTermCountMap(queryTerms);
	        Map<String, Integer> sentTermCountMap = computeTermCountMap(sentTerms);
	        double lmScore = 0;
	        for(String queryTerm : queryTermCountMap.keySet()){
	            int sentCount;
	            if(sentTermCountMap.containsKey(queryTerm)){
	                sentCount = sentTermCountMap.get(queryTerm);
	            } else {
	                sentCount = 0;
	            }
	            double backgroundP = backgroundWD.get(queryTerm);
	            //System.out.println("queryTerm: " + queryTerm);
	            //System.out.println("backgroundWD size: " + backgroundWD.size());
	            lmScore += queryTermCountMap.get(queryTerm) * Math.log( (double)(sentCount + mu * backgroundP) / (double)(sentTerms.length + mu));
	            //score += Math.log((tfs.get(token) + u * retrieval.getNodeStatistics("#counts:@/" + token + "/:part=postings.krovetz()").nodeFrequency / collectionLength) / (doc.terms.size() + u));
	        }
	        return lmScore;
	    }
	    
	    private static Map<String, Integer> computeTermCountMap(String[] terms) {
	        Map<String, Integer> termCountMap = new HashMap<String, Integer>();
	        for(String term : terms){
	            if(termCountMap.containsKey(term)){
	                termCountMap.put(term, termCountMap.get(term) + 1);
	            } else {
	                termCountMap.put(term, 1);
	            }
	        }
	        return termCountMap;
	    }
	    
	    private static double computeBM25Score(Document doc, QADocuments trainQADocSet) {
	    	//reference:  BM25 Wiki
	        double bm25 = 0;
	        double k1 = 1.2;
	        double b = 0.75;
	        String[] queryTerms = new String[doc.questionSentLength];
	        String[] sentTerms = new String[doc.answerSentLength];
	        for(int i = 0; i < doc.questionSentLength; i++){
	        	queryTerms[i] = trainQADocSet.indexToTermMap.get(doc.questionSentWords[i]);
	        }
	        
	        for(int i = 0; i < doc.answerSentLength; i++){
	        	sentTerms[i] = trainQADocSet.indexToTermMap.get(doc.answerSentWords[i]);
	        }
	        
	        Map<String, Double> queryTermSentTFMap = new HashMap<String, Double>();
	         for(String queryTerm : queryTerms){
	             for(String answerToken : sentTerms){
	                 if(answerToken.equals(queryTerm)){
	                     if(queryTermSentTFMap.containsKey(queryTerm)){
	                    	 queryTermSentTFMap.put(queryTerm, queryTermSentTFMap.get(queryTerm) + 1.0);
	                     } else {
	                    	 queryTermSentTFMap.put(queryTerm, 1.0);
	                     }
	                 } 
	             }
	         }

	         for(String queryTerm : queryTerms){
	        	double numSentContainQTerm = 0;
	        	if(termContainSentCountMap.containsKey(queryTerm)){
	        		numSentContainQTerm = termContainSentCountMap.get(queryTerm);
	        	}
	        	 
	             double idf = Math.log(sentTotalNum - numSentContainQTerm + 0.5 / numSentContainQTerm + 0.5);
	             double tf ;
	             if(!queryTermSentTFMap.containsKey(queryTerm)){
	                 tf = 0;
	             } else {
	                 tf = queryTermSentTFMap.get(queryTerm);
	             }
	             bm25 += idf * (tf * (k1 + 1) / (tf + k1 * (1 - b + b * (doc.answerSentLength / avgSentLen))));
	         }
	        // System.out.println("bm25: " + bm25);
	         return bm25;
	     }
	    
	    
	    private static void initTermContainSentCountMap(QADocuments trainQADocSet, QADocuments validQADocSet, QADocuments testQADocSet){
	    	//Key term; Value number of sentences containing this term
            Set<Integer> uniqueTermsInSent = new HashSet<Integer>();
            computeTermContainSentCountGivenOneDocSet(uniqueTermsInSent, trainQADocSet);
            computeTermContainSentCountGivenOneDocSet(uniqueTermsInSent, validQADocSet);
            computeTermContainSentCountGivenOneDocSet(uniqueTermsInSent, testQADocSet);
	        avgSentLen = avgSentLen / sentTotalNum;
	        System.out.println("avgSentLen: " + avgSentLen);
	        System.out.println("sentTotalNum: " + sentTotalNum);
	        //System.out.println(termContainSentCountMap);
	    }

		private static void computeTermContainSentCountGivenOneDocSet(Set<Integer> uniqueTermsInSent,
				QADocuments trainQADocSet) {
			// TODO Auto-generated method stub
			//Scan answer documents/sentences
			for(Document doc : trainQADocSet.docs){
                sentTotalNum++;
                avgSentLen+= doc.answerSentLength;
                uniqueTermsInSent.clear();
                for(int term : doc.answerSentWords){
                    uniqueTermsInSent.add(term);
                }
                for(int term : uniqueTermsInSent){
                	String token = trainQADocSet.indexToTermMap.get(term);
                    if(termContainSentCountMap.containsKey(token)){
                        termContainSentCountMap.put(token, termContainSentCountMap.get(token) + 1.0);
                    } else {
                        termContainSentCountMap.put(token, 1.0);
                    }
                }
            }
			
			//Scan question documents/sentences
			for(String qid : trainQADocSet.qidQsentMap.keySet()){
				sentTotalNum++;
				int [] qSentWords = trainQADocSet.qidQsentMap.get(qid);
				avgSentLen+= qSentWords.length;
				uniqueTermsInSent.clear();
				for(int term : qSentWords){
                    uniqueTermsInSent.add(term);
                }
                for(int term : uniqueTermsInSent){
                	String token = trainQADocSet.indexToTermMap.get(term);
                    if(termContainSentCountMap.containsKey(token)){
                        termContainSentCountMap.put(token, termContainSentCountMap.get(token) + 1.0);
                    } else {
                        termContainSentCountMap.put(token, 1.0);
                    }
                }
			}
		}
}
