/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.core;

import com.FileUtil;
import conf.PathConfig;
import main.core.QADocuments.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**Generate data for the NN based TREC QA factoid baseline
 * Preprocess WebAP data to train word vectors with word2vec tool
 * Output WebAP data as raw words file with basic tokenization/
 * Compute term coverage and word coverage after dropping rare terms
 * @author Liu Yang
 * @email lyang@cs.umass.edu
 */
public class QADocDataPreprocess {
	
    //print a word count file
    //term with the TF of term
	public void printWordCountFile(QADocuments qaDoc, String ModelResDataFolder) {
		// TODO Auto-generated method stub
    	String wordTFFile = ModelResDataFolder + "term.tf";
        ArrayList<String> wordTFLines = new ArrayList<String>();
        for(String word : qaDoc.termCountMap.keySet()){
        	wordTFLines.add(word + "\t" + qaDoc.termCountMap.get(word));
        }
        FileUtil.writeLines(wordTFFile, wordTFLines);
	}

	//Output word sequence to train word vectors using word2vec tool
	//For WebAP, we can train word vectors with WebAP/Gov2 data
	//For TRECQA, we can train word vectors with English Wikipedia dump and the AQUAINT corpus
	//Currently we use word vectors from English Wikipedia dump
    public void printWore2VecTrainFile(QADocuments qaDoc, String ModelResDataFolder) throws IOException {
		// TODO Auto-generated method stub
    	String word2VecTrainFile = ModelResDataFolder + "Word2Vec.train";
        FileWriter writer = new FileWriter(word2VecTrainFile);
        
        for(int i = 0; i < qaDoc.docs.size(); i++){
            String line = "";
            //answer sent words
            for(int k = 0; k < qaDoc.docs.get(i).answerSentLength; k++){
                line += qaDoc.indexToTermMap.get(qaDoc.docs.get(i).answerSentWords[k]) + " ";
            }
            writer.append(line);
            writer.flush();
        }
        System.out.println("output word2vec train file done!");
        writer.close();
	}

	//print a word dic file
    public void printWordDic(QADocuments qaDoc, String ModelResDataFolder) {
    	//"data/" + dataName + "/ModelInputData/"
        String wordDicFile = ModelResDataFolder + "wordIndexDic";
        ArrayList<String> wordIndexDicLines = new ArrayList<String>();
        //0456
        for(int i = 0; i < qaDoc.indexToTermMap.size(); i++){
            wordIndexDicLines.add(qaDoc.indexToTermMap.get(i) + " " + i);
        }
        FileUtil.writeLines(wordDicFile, wordIndexDicLines);
    }

    //0808
    //print the label, question sent word index and answer sentence word index
    //Format of QASentWordIndexFile
    //qid score qLength aLength qSentWordIndex aSentWordIndex
    public void printQASentWordIndex(QADocuments qaDoc, String ModelResDataFolder) throws IOException {
         String QASentWordIndexFile = ModelResDataFolder + "qaSentWordIndex";
         FileWriter writer = new FileWriter(QASentWordIndexFile);
         
         for(int i = 0; i < qaDoc.docs.size(); i++){
             String line = qaDoc.docs.get(i).qid + " " + qaDoc.docs.get(i).label + " " + qaDoc.docs.get(i).questionSentLength + " " + qaDoc.docs.get(i).answerSentLength + " ";
             //question sent word index
             for(int j = 0; j < qaDoc.docs.get(i).questionSentLength; j++){
                 line += qaDoc.docs.get(i).questionSentWords[j] + " ";
             }
             //answer sent word index
             for(int k = 0; k < qaDoc.docs.get(i).answerSentLength; k++){
                 line += qaDoc.docs.get(i).answerSentWords[k] + " ";
             }
             writer.append(line + "\n");
             writer.flush();
         }
         System.out.println("output word index done!");
         writer.close();
    }
    
    /*Computing IDF for all terms
    * Save idf information of all terms to a file 
    * to be used later to weight query terms
    * or derive ranking features.
    * Each line is term \t DFValue \t IDFValue
    * */
	public Map<String, Double> printTermIDFFile(QADocuments trainQADocSet, String modelResDataFolder) {
		// TODO Auto-generated method stub
		Map<String, Double> termToIDFMap = new HashMap<String, Double>();
		double totalDocNum = 0;
		Set<String> uniqueTermSet = new HashSet<String>();
		//scan answer document / sentence
		for(Document doc : trainQADocSet.docs){
			totalDocNum++;
			uniqueTermSet.clear();
			for(int answerTermIndex : doc.answerSentWords){
				uniqueTermSet.add(trainQADocSet.indexToTermMap.get(answerTermIndex));
			}
			for(String term : uniqueTermSet){
				if(termToIDFMap.containsKey(term)){
					termToIDFMap.put(term, termToIDFMap.get(term) + 1.0);
				} else {
					termToIDFMap.put(term, 1.0);
				}
			}
		}
		
		//scan question document / sentence
		Map<String, String[]> qidToqTermsMap = new HashMap<String, String[]>();
		for(Document doc : trainQADocSet.docs){
			if(!qidToqTermsMap.containsKey(doc.qid)){
				String [] qWordsCopy = new String [doc.questionSentWords.length];
				for(int j = 0; j < doc.questionSentWords.length; j++){
					qWordsCopy[j] = trainQADocSet.indexToTermMap.get(doc.questionSentWords[j]);
				}
				qidToqTermsMap.put(doc.qid, qWordsCopy);
			}
		}
		
		for(String qid : qidToqTermsMap.keySet()){
			totalDocNum++;
			uniqueTermSet.clear();
			for(String queryTerm : qidToqTermsMap.get(qid)){
				uniqueTermSet.add(queryTerm);
			}
			for(String term : uniqueTermSet){
				if(termToIDFMap.containsKey(term)){
					termToIDFMap.put(term, termToIDFMap.get(term) + 1.0);
				} else {
					termToIDFMap.put(term, 1.0);
				}
			}
		}
		
		String idfResFile = modelResDataFolder + "term.idf";
		ArrayList<String> lines = new ArrayList<String>();
		for(String term : termToIDFMap.keySet()){
			double idf = Math.log(totalDocNum / termToIDFMap.get(term));
			lines.add(term + "\t" + termToIDFMap.get(term) + "\t" + idf);
		}
		FileUtil.writeLines(idfResFile, lines);
		return termToIDFMap;
	}
}
