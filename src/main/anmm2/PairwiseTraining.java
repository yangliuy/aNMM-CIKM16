package main.anmm2;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.FileUtil;

import main.core.QADocuments;
import main.core.QADocuments.Document;
import main.core.ThreadComputeGradaNMM2;
import conf.ModelParams;

/**Class for pair-wise training to learn optimal weights
 * 
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */

public class PairwiseTraining {

	//Randomly initialize the kDeminsion weight wk
	//Randomly initialize the gate parameter vp
	public void initWeights(String modelName, String binNum, String vectorDimen) {
		// TODO Auto-generated method stub
		//init vp
		ModelParams.vp = new double[Integer.valueOf(vectorDimen)];
		for(int i = 0; i < ModelParams.vp.length; i++){
			ModelParams.vp[i] = Math.random();
		}
		
		//init rt
		ModelParams.rt = new double[ModelParams.addedHiddenLayerRTDim];
		for(int i = 0; i < ModelParams.rt.length; i++){
			ModelParams.rt[i] = Math.random();
		}
		
		//init wtk
		ModelParams.wtk = new double[ModelParams.addedHiddenLayerRTDim][Integer.valueOf(binNum)];
		for(int i = 0; i < ModelParams.wtk.length; i++){
			for(int j = 0; j < ModelParams.wtk[i].length; j++){
				ModelParams.wtk[i][j] = Math.random();
			}
		}
	}
	
	//Construct QA positive/negative triples (S_x, S_y+, S_y-)
	//Make two answers a pair as long as they have different labels
	//Note that there may be no positive/negative answer sentences for some questions
	//Just compute the query number from the actual data instead of passing parameters
	public Set<String> constructQAPNTriples(QADocuments qaDocSet) {
		// TODO Auto-generated method stub
		Set<String> qaTriples = new HashSet<String>(); // A list of IDs for QATriples (S_x, S_y+, S_y-) QID PosAnswerID NegAnswerID
		int queryNum = computeQueryNumFromQADocSet(qaDocSet);
		System.out.println("queryNum in the qaDocSet: " + queryNum);
		
		int [] queryAnswersBoundary = new int[queryNum];
		findDiffQueryAnswerBounday(qaDocSet, queryAnswersBoundary, queryNum);
		int startIndex, endIndex, curQueryIndex=0;
		while(curQueryIndex < queryNum){
			//System.out.println("Construct QA Pos/Neg triples for Query: " + curQueryIndex);
			startIndex = queryAnswersBoundary[curQueryIndex];
			if(curQueryIndex <= (queryNum-2)) endIndex = queryAnswersBoundary[curQueryIndex + 1] - 1;
			else endIndex = qaDocSet.docs.size()-1;
			constructQATriplesForAQuery(qaDocSet, startIndex, endIndex, qaTriples);
			curQueryIndex++;
		}
		//System.out.println("Finished constructing QA Pos/Neg triples and qaTriples.size(): " + qaTriples.size());
		return qaTriples;
	}
	
	private int computeQueryNumFromQADocSet(QADocuments qaDocSet) {
		// TODO Auto-generated method stub
		Set<String> qidSet = new HashSet<String>();
		for(Document doc: qaDocSet.docs){
			qidSet.add(doc.qid);
		}
		return qidSet.size();
	}

	private void constructQATriplesForAQuery(QADocuments qaDocSet,
			int startIndex, int endIndex, Set<String> qaTriples) {
		// TODO Auto-generated method stub
		//System.out.println("qaDocSet.docs.size: " + qaDocSet.docs.size());
		for(int i = startIndex; i <= endIndex; i++){
 			Document qaDoc1 = qaDocSet.docs.get(i);
 			//System.out.println("qid: " + qaDoc1.qid + "\t" + "aid: " + qaDoc1.answerSentId + "\t" + "label: " + qaDoc1.label);
			//Consider all pairs
			//if qid1 == qid2 && label1 != label2 && notStoredBefore(maintain order for S_y+/S_y-)
			for(int j = startIndex; j < endIndex; j++){
				Document qaDoc2 = qaDocSet.docs.get(j);
				if(!qaDoc2.qid.equals(qaDoc1.qid)){
					System.err.println("Error: find different qid for qaDoc1 and qaDoc2: qaDoc1.qid = " + qaDoc1.qid + " qaDoc2.qid = " + qaDoc2.qid);
					return;
				}
				if(qaDoc1.label ==  qaDoc2.label) continue;
				//If the difference between two labels >=2, continue; only pair (0,1) (1,2) (2,3) (3,4)
				if(Math.abs(qaDoc1.label - qaDoc2.label) >= 2) continue;
				String qaTripleString = qaDoc1.qid;
				if(qaDoc1.label > qaDoc2.label){ // qid posAid negAid
					qaTripleString += "\t" + qaDoc1.answerSentId + "\t" + qaDoc2.answerSentId;
				} else if(qaDoc1.label < qaDoc2.label){
					qaTripleString += "\t" + qaDoc2.answerSentId + "\t" + qaDoc1.answerSentId;
				} else {
					System.err.println("Error: find qaTripleString with the same LabelScore. ");
					return;
				}
				//S_x \t S_y+ \t S_y-
				if(!qaTriples.contains(qaTripleString)){
					//System.out.println("Test: add new qaTripleString: " + qaTripleString);
					//System.out.println("qaDoc1.label: " + qaDoc1.label);
					//System.out.println("qaDoc2.label: " + qaDoc2.label);
					qaTriples.add(qaTripleString);
				}
			}
		}
	}

	//Find the boundary between answerSent under different queries
	//Store the first line index of each query
	private void findDiffQueryAnswerBounday(QADocuments qaDocSet,
			int[] queryAnswersBoundary, int queryNum) {
		// TODO Auto-generated method stub
		String curQid = qaDocSet.docs.get(0).qid;
		int i = 0;
		int j = 0;
		queryAnswersBoundary[j] = 0;
		while(i < qaDocSet.docs.size()){
			if(!curQid.equals(qaDocSet.docs.get(i).qid)){
				//System.out.println("found new qid: " + qaDocSet.docs.get(i).qid);
				curQid = qaDocSet.docs.get(i).qid;
				j++;
				//System.out.println("current j and i: " + j + "\t" + i);
				queryAnswersBoundary[j] = i;
			}
			i++;
		}
		
		if(j != queryNum - 1) {
			System.err.println("Error: find boundary error: j = " + j);
		}
		
		//System.out.println("Test: queryAnswersBoundary array ");
		//for(i = 0; i < queryAnswersBoundary.length; i++){
		//	System.out.println(queryAnswersBoundary[i]);
		//}
	}

	//save QA triples to a file
	public void saveQATripleFile(Set<String> qaTriples, String qaTripleFile) throws IOException {
		// TODO Auto-generated method stub
		FileWriter qaTripleWriter = new FileWriter(qaTripleFile);
		for(String qat : qaTriples){
			qaTripleWriter.append(qat + "\n");
			qaTripleWriter.flush();
		}
		qaTripleWriter.close();
	}
	
	//pair wise SGD training given all (S_x, S_y+, S_y-) triples   S_x \t S_y+ \t S_y-
	//Settings: with gate
	public void pairwiseSGDTraining(QADocuments trainQADocSet, 
			Set<String> trainQATriples, QADocuments testQADocSet,
			Set<String> testQATriples, Map<String, Double[]> termToWordVectorMap,
			 String ModelResDataFolder, String modelName,
			String binNum, String vecDim, String runModelType) throws InterruptedException {
		// TODO Auto-generated method stub
		int iterations = ModelParams.iterations, saveStep = ModelParams.saveStep, beginSaveIters = ModelParams.beginSaveIters;
		int maxThreadNum = Runtime.getRuntime().availableProcessors();
		ExecutorService tpes = Executors.newFixedThreadPool(maxThreadNum);//init a thread pool
		System.out.println("maxThreadNum which is the available cores: " + maxThreadNum);
		Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap = new HashMap<String, ArrayList<ArrayList<Double>>>(); //Key qid \t aid   Value qaMatchMatrix
		Map<String, String []> qidToqTermsMap = new HashMap<String, String []>(); //Key:qid   Value: qTermsArray
		Map<String, Double[]> qidToUPrimeArrayMap = new HashMap<String, Double[]>();//Key:qid  Value: uPrimeArray
		Map<String, Double[]> qidToQueryMeanVecMap = new HashMap<String, Double[]>();//Key:qid Value: queryMeanVector
		Map<String, Double> qidToSumExpMap = new HashMap<String, Double>();//Key:qid Value: sumExp
		Map<String, Double> termToIDFMap = new HashMap<String, Double>();//Key:term Value: idf value
		ArrayList<String> trainQATriplesArrayList = new ArrayList<String>();
		//!Need to compute qidToQueryMeanVecMap once and store it to avoid duplicate computations
		initQidToQueryMeanVecMap(qidToQueryMeanVecMap, trainQADocSet, testQADocSet, termToWordVectorMap, Integer.valueOf(vecDim));
		initQidToqTermsMap(qidToqTermsMap, trainQADocSet, testQADocSet);
		initTermToIDFMap(termToIDFMap, ModelResDataFolder);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for(String qaTriple : trainQATriples){
			trainQATriplesArrayList.add(qaTriple);
		}
		//Note that to speed up the training process, we can change the structure of qaMatchMatrixMap
		//We can map each row of qaMatchMatrixMap into B bins
		initQAMatchMatrixMap(trainQADocSet, qaMatchMatrixMap, modelName, Integer.valueOf(binNum));
		initQAMatchMatrixMap(testQADocSet, qaMatchMatrixMap, modelName, Integer.valueOf(binNum));
		
		//!!!Note that UPrimeArray need to be updated in each iteration/triple since v_p is updated in each iteration
		updateQidToUPrimeArrayMap(trainQADocSet, testQADocSet, termToWordVectorMap, qidToUPrimeArrayMap, qidToSumExpMap, qidToQueryMeanVecMap, modelName, qidToqTermsMap);
		
		if(iterations < saveStep + beginSaveIters){
			System.err.println("Error: the number of iterations should be larger than " + (saveStep + beginSaveIters));
			System.exit(0);
		}
		//System.out.println("Begin pairwise training: num of train triples and test triples: " + trainQATriples.size() + "  " + testQATriples.size());
		//!Note which part need to be updated in each iteration,
		for(int i = 1; i <= iterations; i++){
			System.out.println("Iteration/Epoch " + i);
			Date dateObj = new Date();
			System.out.println("Test1 Time: " + df.format(dateObj));
			if((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep) == 0)){
				//Saving the model
				System.out.println("Saving model at iteration " + i + " ... ");
				saveIteratedModel(i, trainQADocSet, ModelResDataFolder);
			}
			double oldLoss = 0;
			//In every iteration, recompute the loss and see whether it decreases
			if(i == 1){
				oldLoss = computeHingeloss(trainQATriples, qaMatchMatrixMap, qidToUPrimeArrayMap, qidToSumExpMap,  modelName, termToIDFMap, qidToqTermsMap);
				System.out.println("Current loss on training data: " + oldLoss);
			} else {
				double curLoss = computeHingeloss(trainQATriples, qaMatchMatrixMap, qidToUPrimeArrayMap, qidToSumExpMap, modelName, termToIDFMap, qidToqTermsMap);
				System.out.println("Current loss on training data: " + curLoss);
				double curTestLoss = computeHingeloss(testQATriples, qaMatchMatrixMap, qidToUPrimeArrayMap, qidToSumExpMap, modelName, termToIDFMap, qidToqTermsMap);
				if(runModelType.equals("Testing")){
					System.out.println("Current loss on testing data: " + curTestLoss);
				} else {
					System.out.println("Current loss on validation/dev data: " + curTestLoss);
				}
				
				if(i % 5 == 0){
					//In every 5 iterations, compute IR metrics on testing data with current model parameters w_k and v_p and write predicted score to file
					computeIRMetricsEval(trainQADocSet, testQADocSet, qidToUPrimeArrayMap, qidToSumExpMap,  i, ModelResDataFolder, qaMatchMatrixMap, modelName, termToIDFMap, qidToqTermsMap);
					//In every 5 iterations, print learnt query term importance for training/testing data by gate function
					printGateLearntQueryTermImportance(trainQADocSet, testQADocSet, qidToUPrimeArrayMap,  qidToqTermsMap, i, ModelResDataFolder, modelName);
					
				}
				if(curLoss - oldLoss < ModelParams.lossChangeThreshold){
					System.out.println("curLoss - oldLoss is less than lossChangeThreshold. Stop. curLoss - oldLoss  and lossChangeThreshold are : " + (curLoss - oldLoss) + "\t" + ModelParams.lossChangeThreshold);
					break;
				}
				oldLoss = curLoss;
			}
			
			double adaptiveEta1 = computeAdaptiveLR(ModelParams.eta1, i, ModelParams.iterations);
			double adaptiveEta2 = computeAdaptiveLR(ModelParams.eta2, i, ModelParams.iterations);
			double adaptiveEta3 = computeAdaptiveLR(ModelParams.eta3, i, ModelParams.iterations);
			
			double[][] oldWtk = new double[ModelParams.wtk.length][ModelParams.wtk[0].length];
			for(int t = 0; t < oldWtk.length; t++){
				for(int k = 0; k < oldWtk[t].length; k++){
					oldWtk[t][k] = ModelParams.wtk[t][k];
				}
			}
			
			double[] oldRt = new double[ModelParams.rt.length];
			for(int t = 0; t < oldRt.length; t++){
				oldRt[t] = ModelParams.rt[t];
			}
			
			double[] oldVp = new double[ModelParams.vp.length];
			for(int p = 0; p < oldVp.length; p++){
				oldVp[p] = ModelParams.vp[p];
			}

			double wtkChangeSumSquare = 0;
			double vpChangeSumSquare = 0;
			double rtChangeSumSquare = 0;
			
			//Use SGD to update weight w_tk, r_t and v_p
			//Update 03302016 
			//Use mini-batch gradient decent and multi-thread implementation
			//Use thread pool
			dateObj = new Date();
			System.out.println("Test2 Time: " + df.format(dateObj));
			int curQATriplePointer = 0;
			double[][] batchWTKGrad = new double[ModelParams.wtk.length][ModelParams.wtk[0].length];
			double[] batchVPGrad = new double[ModelParams.vp.length];
			double[] batchRTGrad = new double[ModelParams.rt.length];
			int totalTripleNum = trainQATriples.size();

			//begin one epoch
			while(curQATriplePointer < totalTripleNum){
				int batchStartIndex = curQATriplePointer;
				int batchEndIndex = Math.min(curQATriplePointer + ModelParams.batchSize - 1, totalTripleNum - 1);
				int numTripleInCurrentBatch = batchEndIndex - batchStartIndex + 1;
				int numWorks = numTripleInCurrentBatch; // the number of tasks in a batch
				CountDownLatch latch = new CountDownLatch(numTripleInCurrentBatch);
				ThreadComputeGradaNMM2 [] works = new ThreadComputeGradaNMM2[numWorks];
				int workIndex = 0;
				
				for(int batchI = batchStartIndex; batchI <= batchEndIndex; batchI++){
					//System.out.println("testA: batchI " + batchI );
					String qaTriple = trainQATriplesArrayList.get(batchI);
					works[workIndex] = new ThreadComputeGradaNMM2( "Thread-curQATriplePointer-" + curQATriplePointer, qaMatchMatrixMap,
							qidToqTermsMap, qidToUPrimeArrayMap, qidToQueryMeanVecMap, qidToSumExpMap, termToWordVectorMap, batchWTKGrad,
							batchVPGrad, batchRTGrad, modelName, termToIDFMap, qaTriple, latch);
					tpes.execute(works[workIndex]);
					workIndex++;
					curQATriplePointer++;
				}
				
		        try {
					latch.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(batchEndIndex == totalTripleNum - 1){
					System.out.println("Finish last batch, update model parameters");
				} else {
					System.out.println("Finish batch " + curQATriplePointer / ModelParams.batchSize + ", update model parameters");
				}
				dateObj = new Date();
    			System.out.println("Test3 Time when finished one batch: " + df.format(dateObj));
				//update parameters
				for(int p = 0; p < ModelParams.vp.length; p++){
					ModelParams.vp[p] -= adaptiveEta3 * batchVPGrad[p];
					batchVPGrad[p] = 0;
				}
				//Update U' array and sumExp array
				updateQidToUPrimeArrayMap(trainQADocSet, testQADocSet, termToWordVectorMap, qidToUPrimeArrayMap, qidToSumExpMap, qidToQueryMeanVecMap, modelName, qidToqTermsMap);
					
			    //Update r_t and w_tk
				for(int t = 0; t < ModelParams.rt.length; t++){
					ModelParams.rt[t] -= adaptiveEta2 * batchRTGrad[t];
					batchRTGrad[t] = 0;
					
					for(int k = 0; k < ModelParams.wtk[0].length; k++){
						ModelParams.wtk[t][k] -= adaptiveEta1 * batchWTKGrad[t][k];
						batchWTKGrad[t][k] = 0;
					}	
				}
			}
			
			for(int t = 0; t < ModelParams.wtk.length; t++){
				for(int k = 0; k < ModelParams.wtk[0].length; k++){
					wtkChangeSumSquare += Math.pow(ModelParams.wtk[t][k] - oldWtk[t][k], 2);
				}
			}
			
			System.out.println("After scan all the triples, wtkChangeSumSquare = " + wtkChangeSumSquare);
			
			for(int t = 0; t < ModelParams.rt.length; t++){
				rtChangeSumSquare += Math.pow(ModelParams.rt[t] - oldRt[t], 2);
			}
			System.out.println("After scan all the triples, rtChangeSumSquare = " + rtChangeSumSquare);
			
			for(int p = 0; p < ModelParams.vp.length; p++){
				vpChangeSumSquare += Math.pow(ModelParams.vp[p] - oldVp[p], 2);
			}
			System.out.println("After scan all the triples, vpChangeSumSquare = " + vpChangeSumSquare);
		}
		tpes.shutdown();
	}

	private void initTermToIDFMap(Map<String, Double> termToIDFMap, String modelResDataFolder) {
		// TODO Auto-generated method stub
		String idfFileName = modelResDataFolder + "term.idf";
		ArrayList<String> lines = new ArrayList<String>();
		FileUtil.readLines(idfFileName, lines);
		for(String line : lines){
			String[] tokens = line.split("\t");
			termToIDFMap.put(tokens[0], Double.valueOf(tokens[2]));
		}
	}

	private void initQidToqTermsMap(Map<String, String[]> qidToqTermsMap, QADocuments trainQADocSet,
			QADocuments testQADocSet) {
		// TODO Auto-generated method stub
		for(Document doc : trainQADocSet.docs){
			if(!qidToqTermsMap.containsKey(doc.qid)){
				String [] qWordsCopy = new String [doc.questionSentWords.length];
				for(int j = 0; j < doc.questionSentWords.length; j++){
					qWordsCopy[j] = trainQADocSet.indexToTermMap.get(doc.questionSentWords[j]);
				}
				qidToqTermsMap.put(doc.qid, qWordsCopy);
			}
		}
		for(Document doc : testQADocSet.docs){
			if(!qidToqTermsMap.containsKey(doc.qid)){
				String [] qWordsCopy = new String [doc.questionSentWords.length];
				for(int j = 0; j < doc.questionSentWords.length; j++){
					qWordsCopy[j] = testQADocSet.indexToTermMap.get(doc.questionSentWords[j]);
				}
				qidToqTermsMap.put(doc.qid, qWordsCopy);
			}
		}
	}

	private void initQidToQueryMeanVecMap(Map<String, Double[]> qidToQueryMeanVecMap, QADocuments trainQADocSet,
			QADocuments testQADocSet, Map<String, Double[]> termToWordVectorMap, Integer vecDim) {
		// TODO Auto-generated method stub
		//qidToQueryMeanVecMap  key: qid  value:queryMeanVec
		//Compute the mean of all word vectors of terms in qid
		Set<String> qidSet = new HashSet<String>();
		for(Document doc : trainQADocSet.docs){
			if(qidSet.contains(doc.qid)) continue;
			addNewQidQueryMeanVecPair(trainQADocSet.indexToTermMap, doc, qidToQueryMeanVecMap, termToWordVectorMap, vecDim);
			qidSet.add(doc.qid);
		}
		for(Document doc : testQADocSet.docs){
			if(qidSet.contains(doc.qid)) continue;
			addNewQidQueryMeanVecPair(testQADocSet.indexToTermMap, doc, qidToQueryMeanVecMap, termToWordVectorMap, vecDim);
			qidSet.add(doc.qid);
		}
	}

	private void addNewQidQueryMeanVecPair(ArrayList<String> indexToTermMap, Document doc, Map<String, Double[]> qidToQueryMeanVecMap,
			Map<String, Double[]> termToWordVectorMap, Integer vecDim) {
		// TODO Auto-generated method stub
		Double [] queryMeanVec = new Double[vecDim];
		for(int z = 0; z < queryMeanVec.length; z++){
			queryMeanVec[z] = 0.0;
		}
		for(int j = 0; j < doc.questionSentWords.length; j++){
			Double[] queryTermWordVec = termToWordVectorMap.get(indexToTermMap.get(doc.questionSentWords[j]));
			for(int z = 0; z < vecDim; z++){
				queryMeanVec[z] += queryTermWordVec[z];
			}
		}
		Double[] queryMeanVecCopy = new Double[vecDim];
		for(int z = 0; z < vecDim; z++){
			queryMeanVec[z] /= (double) doc.questionSentWords.length;
			queryMeanVecCopy[z] = queryMeanVec[z] ;
		}
		qidToQueryMeanVecMap.put(doc.qid, queryMeanVecCopy);
	}
	

	//Compute IR metrics on both training and testing data with current model parameters w_k and v_p and write predicted score to file
	//Setting: with gate function
	private void computeIRMetricsEval(QADocuments trainQADocSet, QADocuments testQADocSet, Map<String, Double[]> qidToUPrimeArrayMap, Map<String, Double> qidToSumExpMap, 
			int iter, String modelResDataFolder, Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap, 
			String modelName, Map<String, Double> termToIDFMap, Map<String, String[]> qidToqTermsMap) {
		// TODO Auto-generated method stub
		String trainDataScoreFile = modelResDataFolder + "nntextmatch_iter_" + iter +"_train.score";
		String testDataScoreFile = modelResDataFolder + "nntextmatch_iter_" + iter +"_test.score";
		//score file format
		// 030  Q0     ZF08-175-870  0     4238   prise1 
	    // qid  iter   docno         rank  sim    run_id 
		// dev_1 Q0 dev_1_asent_1 0 0.282 nntextmatch
		ArrayList<String> scoreLines = new ArrayList<String>();
		String line = "";
		for(int i = 0; i < testQADocSet.docs.size(); i++){
			line = testQADocSet.docs.get(i).qid + " Q0 " + testQADocSet.docs.get(i).answerSentId + " 0 ";
			double nntextmatchScore = computeForwardPredictScore(qaMatchMatrixMap.get(testQADocSet.docs.get(i).qid + "\t" + testQADocSet.docs.get(i).answerSentId), 
					qidToUPrimeArrayMap.get(testQADocSet.docs.get(i).qid), qidToSumExpMap.get(testQADocSet.docs.get(i).qid), modelName, termToIDFMap, qidToqTermsMap.get(testQADocSet.docs.get(i).qid));
			line += nntextmatchScore + " nntextmatch";
			scoreLines.add(line);
		}
		FileUtil.writeLines(testDataScoreFile, scoreLines);
		
		scoreLines.clear();
		line = "";
		for(int i = 0; i < trainQADocSet.docs.size(); i++){
			line = trainQADocSet.docs.get(i).qid + " Q0 " + trainQADocSet.docs.get(i).answerSentId + " 0 ";
			double nntextmatchScore = computeForwardPredictScore(qaMatchMatrixMap.get(trainQADocSet.docs.get(i).qid + "\t" + trainQADocSet.docs.get(i).answerSentId), 
					qidToUPrimeArrayMap.get(trainQADocSet.docs.get(i).qid), qidToSumExpMap.get(trainQADocSet.docs.get(i).qid), modelName, termToIDFMap, qidToqTermsMap.get(trainQADocSet.docs.get(i).qid));
			line += nntextmatchScore + " nntextmatch";
			scoreLines.add(line);
		}
		FileUtil.writeLines(trainDataScoreFile, scoreLines);
	}
	
	//Genterate qrel file for compute IR metrics
	public void generateQrelFile(QADocuments testQADocSet, String ModelResDataFolder) {
		// TODO Auto-generated method stub
		//"data/" + dataName + "/ModelRes/"
		String testDataQrelFile = ModelResDataFolder + "nntextmatch.qrel";
		//qrel file format
		//  qid  iter  docno  rel 
		ArrayList<String> qrelLines = new ArrayList<String>();
		String line = "";
		for(int i = 0; i < testQADocSet.docs.size(); i++){
			line = testQADocSet.docs.get(i).qid + " Q0 " + testQADocSet.docs.get(i).answerSentId;
			line += " " + testQADocSet.docs.get(i).label;
			qrelLines.add(line);
		}
		FileUtil.writeLines(testDataQrelFile, qrelLines);
	}
	
	private double computeAdaptiveLR(double eta1, int i, int iterations) {
		// TODO Auto-generated method stub
		return eta1 * (1 - (double) i / (double) (iterations + 1));
	}

	//Update U' and sumExp array for all qids
	private void updateQidToUPrimeArrayMap(QADocuments trainQADocSet,
			QADocuments testQADocSet, Map<String, Double[]> termToWordVectorMap, 
			Map<String, Double[]> qidToUPrimeArrayMap, Map<String, Double> qidToSumExpMap, 
			Map<String, Double[]> qidToQueryMeanVecMap,  String modelName, Map<String, String []> qidToqTermsMap) {
		// TODO Auto-generated method stub
		qidToUPrimeArrayMap.clear(); // Clear old values 
		qidToSumExpMap.clear();
		for(String qid : qidToQueryMeanVecMap.keySet()){
			String [] queryTerms = qidToqTermsMap.get(qid);
			Double [] uPrimeArray = new Double [queryTerms.length];
			for(int j = 0; j < queryTerms.length; j++){
				Double[] queryTermWordVec = termToWordVectorMap.get(queryTerms[j]);
				double uPrime = 0;
				//If there no word embedding for this word Set u' = 0
				if(queryTermWordVec != null){
					  if(modelName.equals("V5-1")){
						for(int p = 0; p < queryTermWordVec.length; p++){
							uPrime += queryTermWordVec[p] * ModelParams.vp[p];
						}
					} 
					} else {
						System.err.println("In updateQidToUPrimeArrayMap, unsupported modelName type: " + modelName);
						System.exit(1);
					}
				uPrimeArray[j] = uPrime;
			}
			double sumExp = 0;
			for(double up : uPrimeArray){
				sumExp += Math.exp(up);
			}
			qidToUPrimeArrayMap.put(qid, uPrimeArray);
			qidToSumExpMap.put(qid, sumExp);
		}
	}

	//print out learnt query term importance for training/testing data by gate function
	private void printGateLearntQueryTermImportance(QADocuments trainQADocSet, QADocuments testQADocSet,
			Map<String, Double[]> qidToUPrimeArrayMap,  Map<String, String[]> qidToqTermsMap, int i, String modelResDataFolder, String modelName) {
		// TODO Auto-generated method stub
		String queryTermImportFile = modelResDataFolder + "queryTermImportance_iter_" + i + ".txt";
		ArrayList<String> lines = new ArrayList<String>();
		for(String qid : qidToUPrimeArrayMap.keySet()){
			String line = qid + "\t";
			for(String queryTerm : qidToqTermsMap.get(qid)){
				line += queryTerm + " ";
			}
			line += "\t";
			for(Double uprime : qidToUPrimeArrayMap.get(qid)){
				line += uprime + " ";
			}
			line += "\t";
			if(modelName.equals("V5-1")){
				double sumExp = 0;
				for(Double uprime : qidToUPrimeArrayMap.get(qid)){
					sumExp += Math.exp(uprime);
				}
				for(Double uprime : qidToUPrimeArrayMap.get(qid)){
					line += Math.exp(uprime)/sumExp + " ";
				}
			} else {
				System.err.println("wrong model name type in printGateLearntQueryTermImportance function: " + modelName);
			}
			lines.add(line);
		}
		FileUtil.writeLines(queryTermImportFile, lines);
	}

	//Setting: with gate function
	private double computeHingeloss(Set<String> qaTriples,
			Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap, Map<String, Double[]> qidToUPrimeArrayMap, Map<String, Double> qidToSumExpMap,  String modelName,
			Map<String, Double> termToIDFMap, Map<String, String[]> qidToqTermsMap) {
		// TODO Auto-generated method stub
		double curLoss = 0;
		for(String qaTriple : qaTriples){
			String [] qaTripleTokens = qaTriple.split("\t");
			String qid = qaTripleTokens[0], posAid = qaTripleTokens[1], negAid = qaTripleTokens[2];
			Double [] uPrimeArray = qidToUPrimeArrayMap.get(qid);
			double sumExp = qidToSumExpMap.get(qid);
			//Use a hashMap to speed up look up QAMatchMatrix
			ArrayList<ArrayList<Double>> QAMatchMatrixPos = getQAMatchMatrixByQidAid(qid, posAid, qaMatchMatrixMap);
			ArrayList<ArrayList<Double>> QAMatchMatrixNeg = getQAMatchMatrixByQidAid(qid, negAid, qaMatchMatrixMap);
			double deltaY = 1.0 - computeForwardPredictScore(QAMatchMatrixPos, uPrimeArray, sumExp, modelName, termToIDFMap, qidToqTermsMap.get(qid)) 
					+ computeForwardPredictScore(QAMatchMatrixNeg, uPrimeArray, sumExp, modelName, termToIDFMap, qidToqTermsMap.get(qid));
			curLoss += Math.max(deltaY, 0);
			//System.out.println("In computeHingeloss, the value of curLoss: " + curLoss);
		}
		return curLoss;
	}
	
	//Compute the gradient to update w_k
	//Added gate into the model
	//computeGradientRtAndWtk(batchWTKGrad, batchRTGrad, QAMatchMatrixPos, QAMatchMatrixNeg, uPrimeArray, sumExp, modelName,termToIDFMap, qidToqTermsMap.get(qid));
	public void computeGradientRtAndWtk(
			double[][] batchWTKGrad, double [] batchRTGrad,
			ArrayList<ArrayList<Double>> qAMatchMatrixPos,
			ArrayList<ArrayList<Double>> qAMatchMatrixNeg, Double[] uPrimeArray, double sumExp, 
			String modelName, Map<String, Double> termToIDFMap, String[] qTerms) {
		// TODO Auto-generated method stub
		//Compute gradients w.r.t. r_t
		//Compute \partial y / \partial u'' to be reused later
		
		//Compute gradients w.r.t. w_tk
		//Reuse \partial y / \partial u'' during the computation process(back-propagation)
		double[][] gradWtk = new double[ModelParams.wtk.length][ModelParams.wtk[0].length];
		double[] gradRt = new double[ModelParams.rt.length];
		for(int j = 0; j < qAMatchMatrixPos.size(); j++){
			double partialYUDPPos = computeYUDP(qAMatchMatrixPos, j);
			double partialYUDPNeg = computeYUDP(qAMatchMatrixNeg, j);
			
			for(int t = 0; t < ModelParams.rt.length; t++){
				double uPost = computeUt(qAMatchMatrixPos.get(j), t);
				double uNegt = computeUt(qAMatchMatrixNeg.get(j), t);
				gradRt[t] += (Math.exp(uPrimeArray[j]) / sumExp) * (-partialYUDPPos* sigmoid(uPost) + partialYUDPNeg* sigmoid(uNegt));
				for(int k = 0; k < ModelParams.wtk[0].length; k++){
					gradWtk[t][k] += (Math.exp(uPrimeArray[j]) / sumExp) * (- partialYUDPPos * ModelParams.rt[t]* sigmoid(uPost)*(1-sigmoid(uPost))*qAMatchMatrixPos.get(j).get(k) 
							+ partialYUDPNeg * ModelParams.rt[t]* sigmoid(uNegt)*(1-sigmoid(uNegt))*qAMatchMatrixNeg.get(j).get(k) );
				}
			}
		}
		
		for(int t = 0; t < ModelParams.rt.length; t++){
			batchRTGrad[t] += gradRt[t];
			for(int k = 0; k < ModelParams.wtk[0].length; k++){
				batchWTKGrad[t][k] += gradWtk[t][k];
			}
		}
	}

	//Compute the gradient to update v_p
	//Added gate into the model
	public double computeGradientVp(
			ArrayList<ArrayList<Double>> qAMatchMatrixPos,
			ArrayList<ArrayList<Double>> qAMatchMatrixNeg, int p,
			Double[] uPrimeArray, double sumExp, Map<String, Double[]> termToWordVectorMap, String [] qWords, Double[] queryMeanVec, String modelName) {
		// TODO Auto-generated method stub
		double sumExpL = 0;
		for(int l = 0; l < qAMatchMatrixPos.size(); l++){
			sumExpL += Math.exp(uPrimeArray[l]) * termToWordVectorMap.get(qWords[l])[p];
		}
		double grad = 0;
		for(int j = 0; j < qAMatchMatrixPos.size(); j++){
			double uDPPos = computeUDoublePrime(qAMatchMatrixPos, j);
			double uDPNeg = computeUDoublePrime(qAMatchMatrixNeg, j);
			if(!termToWordVectorMap.containsKey(qWords[j])){
				continue; // For query word without word embedding, don't consider them when computing gradients of Vp
			}
			double qjp = termToWordVectorMap.get(qWords[j])[p];
			//System.out.println("sigmoid(uPrimeArray[j]) * (1 - sigmoid(uPrimeArray[j])): " + sigmoid(uPrimeArray[j]) * (1 - sigmoid(uPrimeArray[j])));
			//System.out.println("qjp: " + qjp);
			//System.out.println("(-sigmoid(uPos) + sigmoid(uNeg): " + ((-sigmoid(uPos) + sigmoid(uNeg))));
			double uPrimej = uPrimeArray[j];
			if(modelName.equals("V5-1")) { //softmax gate with an added layer
				double bigX = (Math.exp(uPrimej) * qjp * sumExp - Math.exp(uPrimej) * sumExpL) / Math.pow(sumExp, 2);
				grad += bigX * (-sigmoid(uDPPos) + sigmoid(uDPNeg));
			}
			  else{
				System.err.println("In computeGradientVp, unsupported modelName type: " + modelName);
				System.exit(1);
			}
		}
		return grad;
	}

	//Compute U = \sum_k w_k * x_jk
	private double computeUt(ArrayList<Double> xj, int t) {
		// TODO Auto-generated method stub
		double u = 0;
		for(int k = 0; k < xj.size(); k++){
			u += ModelParams.wtk[t][k] * xj.get(k);
		}
		return u;
	}
	
	//Compute U'' = \sum_t r_t * sigmoid( \sum_k w_tk * x_jk)
	private double computeUDoublePrime(ArrayList<ArrayList<Double>> qAMatchMatrix, int j) {
		double [][] wtk = ModelParams.wtk;
		double uDoublePrime = 0;
		for(int t = 0; t < wtk.length; t++){
			int u = 0;
			for(int k = 0; k < wtk[t].length; k++){
				u += wtk[t][k] * qAMatchMatrix.get(j).get(k);
			}
			uDoublePrime += ModelParams.rt[t]*sigmoid(u);
		}
		return uDoublePrime;
	}
	
	private double computeYUDP(ArrayList<ArrayList<Double>> qAMatchMatrix, int j) {
		// TODO Auto-generated method stub
		double uDoublePrime = computeUDoublePrime(qAMatchMatrix, j);
		return sigmoid(uDoublePrime) * (1 - sigmoid(uDoublePrime));
	}

	//Compute the forward predicted matching score given the QAMatch matrix and the given weight w_k, v_p
	//Setting: with gate function
	public double computeForwardPredictScore(
			ArrayList<ArrayList<Double>> qAMatchMatrix, Double[] uPrimeArray, double sumExp, 
			String modelName, Map<String, Double> termToIDFMap, String[] qTermsArray) {
		// TODO Auto-generated method stub
		double predictedScore = 0;
		for(int j = 0; j < qAMatchMatrix.size(); j++){
			double uDoublePrime = computeUDoublePrime(qAMatchMatrix, j);
			if(modelName.equals("V5-1")){//softmax gate + bin weight + add a hidden layer for multiple-bin weight settings
				predictedScore += (Math.exp(uPrimeArray[j]) / sumExp) * sigmoid(uDoublePrime);
			} else {
				System.err.println("Wrong model name type in computeForwardPredictScore function : " + modelName);
			}
		}
		return predictedScore;
	}
	
	//Return the index of the corresponding Wk dimension given the qaMatchScore
	//if qaMatchScore == 1 wkIndex =20;
	//otherwise wkIndex = 0-19
	private  int getWKIndexByQAMatchScore(Double qaMatchScore, int BinNum) {
		// TODO Auto-generated method stub
		int wkIndex;
		int BinNumWithoutExactMatch = BinNum - 1;
		if(qaMatchScore == 1.0){
			wkIndex = BinNumWithoutExactMatch;
		} else {
			wkIndex = new BigDecimal((qaMatchScore + 1.0) * 0.5 * BinNumWithoutExactMatch).setScale(0, BigDecimal.ROUND_DOWN).intValue();
		}
		return wkIndex;
	}

	private double sigmoid(double curQueryScore) {
		// TODO Auto-generated method stub
		return (1/( 1 + Math.pow(Math.E,(-1*curQueryScore))));
	}
	
	private void initQAMatchMatrixMap(QADocuments qaDocSet,
			Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap, String modelName, int binNum) {
		// TODO Auto-generated method stub
		//Key qid \t aid
		//Value qaMatchMatrix
		if(modelName.equals("V0") || modelName.equals("V1") || modelName.equals("V2")){
			for(Document doc : qaDocSet.docs){
				ArrayList<ArrayList<Double>> qaMatchMatrixCopy = new ArrayList<ArrayList<Double>>(doc.QAMatchMatrix);
				qaMatchMatrixMap.put(doc.qid + "\t" + doc.answerSentId, qaMatchMatrixCopy);
			}
		} else {//default: add bin weight
			for(Document doc : qaDocSet.docs){
				ArrayList<ArrayList<Double>> qaMatchMatrixCopyBin = new ArrayList<ArrayList<Double>>(); //M * Bin
				for(int i = 0; i < doc.QAMatchMatrix.size(); i++){
					ArrayList<Double> qaMatchMatrixCopyBinRow = new ArrayList<Double>();
					for(int bin = 0; bin < binNum; bin++){
						qaMatchMatrixCopyBinRow.add(0.0);
					}
					for(int j = 0; j < doc.QAMatchMatrix.get(i).size(); j++){
						double mScore = doc.QAMatchMatrix.get(i).get(j);
						int binIndex = getWKIndexByQAMatchScore(mScore, binNum);
						qaMatchMatrixCopyBinRow.set(binIndex, qaMatchMatrixCopyBinRow.get(binIndex) + mScore);
					}
					ArrayList<Double> aListCopy = new ArrayList<Double>(qaMatchMatrixCopyBinRow);
					qaMatchMatrixCopyBin.add(aListCopy);
				}
				qaMatchMatrixMap.put(doc.qid + "\t" + doc.answerSentId, qaMatchMatrixCopyBin);
			}
		}
	}

	//get QAMatchMatrix by Qid and Aid
	public ArrayList<ArrayList<Double>> getQAMatchMatrixByQidAid(String qid,
			String posAid, Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap) {
		// TODO Auto-generated method stub
		return qaMatchMatrixMap.get(qid + "\t" + posAid);
	}

	private void saveIteratedModel(int i, QADocuments qaDocSet, String modelResDataFolder) {
		// TODO Auto-generated method stub
		//Save weight w_t_k
		String paramWtkFile = modelResDataFolder + "wtk_iter_" + i + ".wtk";
		ArrayList<String> lines = new ArrayList<String>();
		String line = "";
		double [][] wtk = ModelParams.wtk;
		for(int t = 0; t < wtk.length; t++){
			line = "";
			for(int k = 0; k < wtk[0].length; k++){
				line += wtk[t][k] + "\t";
			}
			lines.add(line);
		}
		FileUtil.writeLines(paramWtkFile, lines);
		
		//Save weight v_p
		String paramVPFile = modelResDataFolder + "vp_iter_" + i + ".vp";
		lines.clear();
		line = "";
		double [] vp = ModelParams.vp;
		for(int j = 0; j < vp.length; j++){
			line += vp[j] + "\t";
		}
		lines.add(line);
		FileUtil.writeLines(paramVPFile, lines);
		
		//Save weight r_t
		String paramRTFile = modelResDataFolder + "rt_iter_" + i + ".rt";
		lines.clear();
		line = "";
		double [] rt = ModelParams.rt;
		for(int t = 0; t < rt.length; t++){
			line += rt[t] + "\t";
		}
		lines.add(line);
		FileUtil.writeLines(paramRTFile, lines);
	}
}
