package main.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import conf.ModelParams;
import main.anmm2.PairwiseTraining;

public class ThreadComputeGradaNMM2 implements Runnable {
	   private String threadName;
	   Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap ;//Key qid \t aid   Value qaMatchMatrix
	   Map<String, String []> qidToqTermsMap;//Key:qid   Value: qTermsArray
	   Map<String, Double[]> qidToUPrimeArrayMap;//Key:qid  Value: uPrimeArray
	   Map<String, Double[]> qidToQueryMeanVecMap;//Key:qid Value: queryMeanVector
       Map<String, Double> qidToSumExpMap;//Key:qid Value: sumExp
       Map<String, Double[]> termToWordVectorMap;
       double[][] batchWTKGrad;
	   double[] batchVPGrad;
	   double[] batchRTGrad;
	   String modelName;
	   Map<String, Double> termToIDFMap;
	   String qaTriple;
	   private CountDownLatch latch;

	   
	   
	   public ThreadComputeGradaNMM2( String name,
			   Map<String, ArrayList<ArrayList<Double>>> qaMatchMatrixMap,
			   Map<String, String []> qidToqTermsMap,
			   Map<String, Double[]> qidToUPrimeArrayMap,
			   Map<String, Double[]> qidToQueryMeanVecMap,
			   Map<String, Double> qidToSumExpMap,
			   Map<String, Double[]> termToWordVectorMap,
			   double[][] batchWTKGrad,
			   double[] batchVPGrad,
			   double[] batchRTGrad,
			   String modelName,
			   Map<String, Double> termToIDFMap,
			   String qaTriple, CountDownLatch latch
			   ){
	       this.threadName = name;
	       this.qaMatchMatrixMap = qaMatchMatrixMap;
	       this.qidToqTermsMap = qidToqTermsMap;
	       this.qidToUPrimeArrayMap = qidToUPrimeArrayMap;
	       this.qidToQueryMeanVecMap = qidToQueryMeanVecMap;
	       this.qidToSumExpMap = qidToSumExpMap;
	       this.termToWordVectorMap = termToWordVectorMap;
	       this.batchWTKGrad = batchWTKGrad;
	       this.batchVPGrad = batchVPGrad;
	       this.batchRTGrad = batchRTGrad;
	       this.modelName = modelName;
	       this.termToIDFMap = termToIDFMap;
	       this.qaTriple = qaTriple;
	       this.latch = latch;
	   }
	   
	   public void run() {
	      //System.out.println("Begin " +  threadName );
	      PairwiseTraining pt = new PairwiseTraining();
		    String [] qaTripleTokens = qaTriple.split("\t");
			String qid = qaTripleTokens[0], posAid = qaTripleTokens[1], negAid = qaTripleTokens[2];
			Double [] uPrimeArray = qidToUPrimeArrayMap.get(qid);
			double sumExp = qidToSumExpMap.get(qid);
			ArrayList<ArrayList<Double>> QAMatchMatrixPos = pt.getQAMatchMatrixByQidAid(qid, posAid, qaMatchMatrixMap);
			ArrayList<ArrayList<Double>> QAMatchMatrixNeg = pt.getQAMatchMatrixByQidAid(qid, negAid, qaMatchMatrixMap);
			//create a thread to compute grad for a given triple, will do this later after finish an intial version of nntextmatch-v51
			
			double deltaY = 1.0 - 
					pt.computeForwardPredictScore(QAMatchMatrixPos, uPrimeArray, sumExp, modelName, termToIDFMap, qidToqTermsMap.get(qid)) 
				  + pt.computeForwardPredictScore(QAMatchMatrixNeg, uPrimeArray, sumExp, modelName, termToIDFMap, qidToqTermsMap.get(qid));
			if(deltaY <= 0) { //if deltaY <= 0, skip this triple
				
			}
			else {//compute gradient and the sum of gradient
				  //!!! Multiple thread implementation !!!
				  //Method 1: parallel between different layer parameters:  before we update model parameter, we can always use the old parameter values for w_tk, r_t and v_p
				  //thus, the computation process of gradients for w_tk, r_t , v_p could be implemented by multiple threads. But in this case, we can't reuse some inter-medium 
				  //gradient computation results
				
				  //Method 2: parallel between different dimensions of parameters. When compute gradients, we can use multiple threads to compute different dimensions of model parameters
				  //The whole computation process is still back-propagation, but we compute gradient of different vector dimensions at the same time
				
				  //Method 3: parallel between qaTriples within the same mini-batch. Say mini-batch size = 1000, then model parameters are the same for the 1000 qaTriples in the same
				  //minibatch. So we can use multiple threads(e.g. 1000 threads) to compute gradients from these 1000 qaTriples at the same time
				
				  //Method 4: parallel by using GPU/Theano. For GPU, it can do matrix computation in parallel
				//back propagation from the output layer to the first layer
				//compute grad for vp
				for(int p = 0; p < ModelParams.vp.length; p++){
					batchVPGrad[p] += pt.computeGradientVp(QAMatchMatrixPos, QAMatchMatrixNeg, p, uPrimeArray, sumExp, termToWordVectorMap, qidToqTermsMap.get(qid), qidToQueryMeanVecMap.get(qid), modelName);
				}

				//compute grad for rt and wtk
				//udpate batchWTKGrad and batchRTGrad
				pt.computeGradientRtAndWtk(batchWTKGrad, batchRTGrad, QAMatchMatrixPos, QAMatchMatrixNeg, uPrimeArray, sumExp, modelName,termToIDFMap, qidToqTermsMap.get(qid));	
			}
			 this.latch.countDown();
		     //System.out.println("test latch count : " + latch.getCount());
			//System.out.println("End " +  threadName );
	   }
	}

