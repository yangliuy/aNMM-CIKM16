package main.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import conf.ModelParams;
import main.anmm1.PairwiseTraining;


//Java multi-thread implementation
//The thread for computing gradients for a triple
public class ThreadComputeGradaNMM1 extends Thread {
	   private Thread t;
	   private String threadName;
	   Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap ;//Key qid \t aid   Value qaMatchMatrix
	   Map<String, String []> qidToqTermsMap;//Key:qid   Value: qTermsArray
	   Map<String, Double[]> qidToUPrimeArrayMap;;//Key:qid  Value: uPrimeArray
	   Map<String, Double[]> qidToQueryMeanVecMap;//Key:qid Value: queryMeanVector
       Map<String, Double> qidToSumExpMap;//Key:qid Value: sumExp
       Map<String, Double[]> termToWordVectorMap;
       double[] batchWKGrad;
	   double[] batchVPGrad;
	   String modelName;
	   Map<String, Double> termToIDFMap;
	   String qaTriple;
	   
	   CountDownLatch countDownLatch;
	   
	   
	   public ThreadComputeGradaNMM1( String name,
			   Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap,
			   Map<String, String []> qidToqTermsMap,
			   Map<String, Double[]> qidToUPrimeArrayMap,
			   Map<String, Double[]> qidToQueryMeanVecMap,
			   Map<String, Double> qidToSumExpMap,
			   Map<String, Double[]> termToWordVectorMap,
			   double[] batchWKGrad,
			   double[] batchVPGrad,
			   String modelName,
			   Map<String, Double> termToIDFMap,
			   String qaTriple,
			   CountDownLatch countDownLatch){
	       this.threadName = name;
	       this.qaMatchMatrixMap = qaMatchMatrixMap;
	       this.qidToqTermsMap = qidToqTermsMap;
	       this.qidToUPrimeArrayMap = qidToUPrimeArrayMap;
	       this.qidToQueryMeanVecMap = qidToQueryMeanVecMap;
	       this.qidToSumExpMap = qidToSumExpMap;
	       this.termToWordVectorMap = termToWordVectorMap;
	       this.batchWKGrad= batchWKGrad;
	       this.batchVPGrad = batchVPGrad;
	       this.modelName = modelName;
	       this.termToIDFMap = termToIDFMap;
	       this.qaTriple = qaTriple;
	       this.countDownLatch = countDownLatch;
	   }
	   
	   public void run() {
	     // System.out.println("Running " +  threadName );   
	        PairwiseTraining pt = new PairwiseTraining();
		 
		    String [] qaTripleTokens = qaTriple.split("\t");
			String qid = qaTripleTokens[0], posAid = qaTripleTokens[1], negAid = qaTripleTokens[2];
			Double [] uPrimeArray = qidToUPrimeArrayMap.get(qid);
			double sumExp = qidToSumExpMap.get(qid);
			ArrayList<ArrayList<Double>> QAMatchMatrixPos = pt.getQAMatchMatrixByQidAid(qid, posAid, qaMatchMatrixMap);
			ArrayList<ArrayList<Double>> QAMatchMatrixNeg = pt.getQAMatchMatrixByQidAid(qid, negAid, qaMatchMatrixMap);
			//create a thread to compute grad for a given triple
			
			double deltaY = 1.0 - 
					pt.computeForwardPredictScore(QAMatchMatrixPos, uPrimeArray, sumExp, modelName, termToIDFMap, qidToqTermsMap.get(qid)) 
				  + pt.computeForwardPredictScore(QAMatchMatrixNeg, uPrimeArray, sumExp, modelName, termToIDFMap, qidToqTermsMap.get(qid));
			if(deltaY <= 0) { //if deltaY <= 0, skip this triple
				
			}
			else {
				//compute grad for wk and vp
				for(int k = 0; k < ModelParams.wk.length; k++){
					batchWKGrad[k] += pt.computeGradientWk(QAMatchMatrixPos, QAMatchMatrixNeg, k, uPrimeArray, sumExp, modelName
							,termToIDFMap, qidToqTermsMap.get(qid));
				}
				if(!modelName.equals("V4-4")){
					for(int p = 0; p < ModelParams.vp.length; p++){
						batchVPGrad[p] += pt.computeGradientVp(QAMatchMatrixPos, QAMatchMatrixNeg, p, uPrimeArray, sumExp, termToWordVectorMap, qidToqTermsMap.get(qid), qidToQueryMeanVecMap.get(qid), modelName);
					}
				}
			}
	     //System.out.println("Thread " +  threadName + " exiting.");
	     countDownLatch.countDown();
	   }
	   
	   public void start ()
	   {
	     // System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }

	}

