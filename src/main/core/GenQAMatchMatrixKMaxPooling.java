package main.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.core.QADocuments.Document;

import com.FileUtil;
import com.MatrixUtil;

import conf.ModelParams;
import conf.PathConfig;

/**Class for generating QA match matrix and K max pooling
 *
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */

public class GenQAMatchMatrixKMaxPooling {
	
	public void initTermToWordVectorMap(Map<String, Double[]> termToWordVectorMap , String preTrainedWordVecFile) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader wordVecReader = new BufferedReader(new FileReader(preTrainedWordVecFile));
		String line;
		int i = 0;
		while((line = wordVecReader.readLine()) != null){
			if(i == 0) {
				i++;
				continue;
			}
			String [] tokens = line.split(" ");
			String term = tokens[0];
			Double [] wordVec = new Double[tokens.length - 1];
			for(int j = 1; j < tokens.length; j++){
				wordVec[j-1] = Double.parseDouble(tokens[j]);
			}
			termToWordVectorMap.put(term, wordVec);
			i++;
		}
		System.out.println("termToWordVectorMap size: " + termToWordVectorMap.size());
	}

	public void geneateQAMatrix(QADocuments qaDocSet,
			Map<String, Double[]> termToWordVectorMap, int vecDim) {
		// TODO Auto-generated method stub
		Map<String, Double> cachTermSimMap = new HashMap<String, Double>();// QWordIndex AWordIndex(increasing order) -> Term Similarity  TermSim cache to speed up
		for(int i = 0; i < qaDocSet.docs.size(); i++){
			//if(i % 1000 == 0){//test
			//	System.out.println("Computing QA match matrix for QA pair index: " + i);
			//}
			Document qaDoc = qaDocSet.docs.get(i);
			ArrayList<String> indexToTermMap = qaDocSet.indexToTermMap;
			ArrayList<Double> qaMatchScoreLine = new ArrayList<Double>();
			//QAMatchMatrix is queryLen * answerLen. Each row is corresponding to a query word
			for(int queryWordIndex : qaDoc.questionSentWords){
				//System.out.println("test1: termToWordVectorMap size " + termToWordVectorMap.size());
				Double[] queryWordVec = getWordVecFromWordIndex(queryWordIndex, termToWordVectorMap, indexToTermMap, vecDim);
				qaMatchScoreLine.clear();
				for(int answerWordIndex : qaDoc.answerSentWords){
					if(!termToWordVectorMap.containsKey(indexToTermMap.get(queryWordIndex)) || !termToWordVectorMap.containsKey(indexToTermMap.get(answerWordIndex)) ){
						//If the term is not found in the wordVectorMap, we adopt the following strategy:
						//Only allow this word to match with itself (set sim = 1); Otherwise, set sim = 0
						if(queryWordIndex == answerWordIndex) qaMatchScoreLine.add(1.0);
						else qaMatchScoreLine.add(0.0);
					} else {
						String qaIndexString = getQAIndexStringFromQAIndex(queryWordIndex, answerWordIndex);
						double dotProduct;
						if(!cachTermSimMap.containsKey(qaIndexString)){
							Double[] answerWordVec = getWordVecFromWordIndex(answerWordIndex, termToWordVectorMap, indexToTermMap, vecDim);
							dotProduct = MatrixUtil.dotProdNormalize(queryWordVec, answerWordVec);
							cachTermSimMap.put(qaIndexString, dotProduct);
						} else {
							dotProduct = cachTermSimMap.get(qaIndexString);
						}
						qaMatchScoreLine.add(dotProduct);
					}
				}
				ArrayList<Double> qaMatchScoreLineCopy = new ArrayList<Double>(qaMatchScoreLine);
				qaDoc.QAMatchMatrix.add(qaMatchScoreLineCopy);
			}
			//System.out.println("test: QAMatchMatrix " + qaDoc.QAMatchMatrix);
		}
	}

	private Double[] getWordVecFromWordIndex(int queryWordIndex,
			Map<String, Double[]> termToWordVectorMap, ArrayList<String> indexToTermMap, int vecDim) {
		// TODO Auto-generated method stub
		Double [] wordVec;
		String term = indexToTermMap.get(queryWordIndex);
		if(termToWordVectorMap.containsKey(term)){
			wordVec = termToWordVectorMap.get(term);
		} else { 
			//If the term is not found in the wordVectorMap, we adopt the following strategy:
			//Only allow this word to match with itself (set sim = 1); Otherwise, set sim = 0
			//For the word vector, we randomly initialized it with numbers randomly sampling
			//from the uniform distribution U[-0.25,0.25] following the SIGIR15 paper Aliaksei et al.
			wordVec = new Double[vecDim];
			for(int i = 0; i < vecDim; i++){
				wordVec[i] = Math.random() * 0.5 - 0.25;
			}
			termToWordVectorMap.put(term, wordVec);
		}
		return wordVec;
	}

	private String getQAIndexStringFromQAIndex(int queryWordIndex, int answerWordIndex) {
		// TODO Auto-generated method stub
		String qaIndexString;
		if(queryWordIndex < answerWordIndex) {
			qaIndexString = queryWordIndex + " " + answerWordIndex;
		} else {
			qaIndexString = answerWordIndex + " " + queryWordIndex;
		}
		return qaIndexString;
	}
	
	//Perform K-max pooling on the QAMatchMatrix field of each document
	//Maintain the order after K-max pooling
	public void doKMaxPooling(QADocuments qaDocSet) {
		// TODO Auto-generated method stub
		for(int i = 0; i < qaDocSet.docs.size(); i++){
			if(i % 1000 == 0){
				System.out.println("Perform K-max pooling for QA pair index: " + i);
			}
			Document qaDoc = qaDocSet.docs.get(i);
			ArrayList<ArrayList<Double>> originalQAMatchMatrix = qaDoc.QAMatchMatrix;
			ArrayList<ArrayList<Double>> pooledQAMatchMatrix = new ArrayList<ArrayList<Double>>();
			for(int j = 0; j < originalQAMatchMatrix.size(); j++){
				List<Double> qWordMatchLine = originalQAMatchMatrix.get(j);
				if(qWordMatchLine.size() < ModelParams.maxPoolingSizeK){
					//In this case, we maintain all values and add 0 to make the length K
					ArrayList<Double> pooledLine = new ArrayList<Double>(qWordMatchLine);
					while(pooledLine.size() < ModelParams.maxPoolingSizeK){
						pooledLine.add(0.0);
					}
					pooledQAMatchMatrix.add(pooledLine);
				} else {
					//In this case, we pick the K max values and maintain the original order
					//System.out.println("special test1: pooledLine before Kmax pooling: " + qWordMatchLine );
					//System.out.println("special test1 before Kmax pooling qWordMatchLine size: " + qWordMatchLine.size());
					double kthEle = findKthElement(qWordMatchLine);
					//Count how many elements are with the same values "kthEle" / how many elements are less than "kthEle"
					int beforekthEleCount = 0;
					for(double ele : qWordMatchLine){
						if(ele > kthEle){
							beforekthEleCount++;
						}
					}
					int needToAdd = ModelParams.maxPoolingSizeK - beforekthEleCount;
					ArrayList<Double> pooledLine = new ArrayList<Double>();
					for(double ele : qWordMatchLine){
						if(ele > kthEle){
							pooledLine.add(ele);
						} else if(ele == kthEle){
							if(needToAdd > 0) {
								pooledLine.add(ele);
								needToAdd--;
							}
						} else {
							continue;
						}
					}
					//System.out.println("special test2: pooledLine after Kmax pooling: " + pooledLine );
					//System.out.println("special test2: pooledLine size after Kmax pooling: " + pooledLine.size());
					if(pooledLine.size() != ModelParams.maxPoolingSizeK) {
						System.err.println("test: pooledLine.size() != ModelParams.maxPoolingSizeK");
						break;
					}
					pooledQAMatchMatrix.add(pooledLine);
				}
			}
			qaDoc.QAMatchMatrix = pooledQAMatchMatrix;
			
//			//testing
//			System.out.print("Current Question: ");
//			for(int qWordIndex : qaDoc.questionSentWords){
//				System.out.print(qaDocSet.indexToTermMap.get(qWordIndex) + " ");
//			}
//			System.out.println();
//			
//			System.out.print("Current Answer: ");
//			for(int aWordIndex : qaDoc.answerSentWords){
//				System.out.print(qaDocSet.indexToTermMap.get(aWordIndex) + " ");
//			}
//			System.out.println();
//			System.out.println("Current Label: " + qaDoc.label);
//			
//			System.out.println("test: QAMatchMatrix after K max pooling");
//			for(int ii = 0; ii < qaDoc.QAMatchMatrix.size(); ii++){
//				for(int jj = 0; jj < qaDoc.QAMatchMatrix.get(ii).size(); jj++){
//					System.out.print(qaDoc.QAMatchMatrix.get(ii).get(jj) + " ");
//				}
//				System.out.println();
//			}
//			System.out.println();
		}
	}
	
	//Find K-th element in a List
	private double findKthElement(List<Double> qWordMatchLine) {
		// TODO Auto-generated method stub
		List<Double> qWordMatchLineCopy = new ArrayList<Double>(qWordMatchLine);
		Collections.sort(qWordMatchLineCopy, Collections.reverseOrder());
		//System.out.println("test: arrayList after sort: " + qWordMatchLineCopy);
		//System.out.println("test: the K-th ele: " + qWordMatchLineCopy.get(ModelParams.maxPoolingSizeK - 1));
		return qWordMatchLineCopy.get(ModelParams.maxPoolingSizeK - 1);
	}
}
