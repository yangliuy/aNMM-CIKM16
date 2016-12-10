package main.anmm2;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import main.core.GenQAMatchMatrixKMaxPooling;
import main.core.QADocDataPreprocess;
import main.core.QADocuments;


/**Main class of the whole data pipeline (V5-1 which is aNMM2)
 * 
 * Step1: Read serialized data and init QA match matrix based on pre-trained word vectors 
 * with word2vec tool. Then do K-max pooling for each query
 * word and output the matrix after K-max pooling.
 * 
 * Step2: Pair-wise training to learning optimal weights
 * 
	//V5 Add a hidden layer & Multiple bin sizes in the added hidden layer (used fixed bin size firstly then update)
	//Need to implement this setting for softmax gate function which can further improve the performance
	//Consider mini-batch gradient descent to get more robust performance
	 The latest version update see the comments within the main function
 * 
 *
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */

public class aNMM2 {

	public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException{
		
		if(args.length < 11) {
			System.err.println("please input params: queryFile trainFile validationFile testFile preTrainedWordVecFile ModelResDataPath dataName modelName binNum vectorDimen.   all paths should be with /");
			System.exit(1);
		}
		String queryFile = args[0]; //data/TRECQA/ModelInputData/trecqa.queries
		String trainFile = args[1]; //data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
		String validFile = args[2]; //data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
		String testFile = args[3]; //data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent
		String preTrainedWordVecFile = args[4]; //data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec
		String ModelResDataFolder = args[5]; //data/TRECQA/ModelRes/output_v41_softmax_binw/
		String dataName = args[6]; //options: WebAP TRECQA YahooCQA WikiQA
		String modelName = args[7]; //options: V5-1
		String binNum = args[8]; //number of bins
		String vectorDimen = args[9]; //number of word vector dimensions
		String runModelType = args[10];//options: Validation-- In this run type, the test data is validFile. We want to tune hyper-parameter with validFile
									   //		  Testing-- In this run type, the test data is testFile. We want to do prediction on testFile with the optimal parameters learned on validFile
		
		File modelResPath = new File(ModelResDataFolder);
		if(!modelResPath.exists()) {
			System.out.println("create path : " + ModelResDataFolder);
			modelResPath.mkdir();
		} else {
			System.out.println(ModelResDataFolder + " exist! ");
		}
		System.out.println("runModelType: " + runModelType);
        //String preTrainedWordVecFile = PathConfig.ModelInputDataFolder + "webapWord2Vec_Skip_win10.output";
        Map<String, Double[]> termToWordVectorMap = new HashMap<String, Double[]>(); //term -> wordVecString  / the separator is white space
        
        //1. Data preprocess
        //If there are too much negative samples, you need to do negative sampling. We don't need so much negative samples
        //The parameter for controlling the negative sampling ratio: negativeSampleRatio
        //Read both train and test data and compute loss/IR metrics on test data
        QADocuments trainQADocSet= new QADocuments();
        QADocuments testQADocSet = new QADocuments();
        QADocDataPreprocess QADP = new QADocDataPreprocess();
        GenQAMatchMatrixKMaxPooling genQAMatchMaxkP = new GenQAMatchMatrixKMaxPooling();
        genQAMatchMaxkP.initTermToWordVectorMap(termToWordVectorMap, preTrainedWordVecFile);
        trainQADocSet.readQADocs(trainFile, queryFile, true, dataName); //Do subsampling only when isSubsampling is true and dataName = "WebAP"; Otherwise, we don't do data subsampling
        if(runModelType.equals("Validation")){
        	testQADocSet.readQADocs(validFile, queryFile, false, dataName);
        } else if(runModelType.equals("Testing")){
        	testQADocSet.readQADocs(testFile, queryFile, false, dataName);
        } else {
        	System.err.println("Error run model type: " + runModelType);
        }
        
        QADP.printWordDic(trainQADocSet, ModelResDataFolder);
        QADP.printQASentWordIndex(trainQADocSet, ModelResDataFolder);
        //QADP.printWore2VecTrainFile(trainQADocSet, ModelResDataFolder);
        QADP.printWordCountFile(trainQADocSet, ModelResDataFolder);
        QADP.printTermIDFFile(trainQADocSet, ModelResDataFolder);
        //FileUtil.saveClass(qaDocSet, serializedData);
        //qaDocSet = FileUtil.loadClass(qaDocSet, serializedData);
        System.out.println("total number of QA sent pairs in training data: " + trainQADocSet.docs.size());
        System.out.println("total number of QA sent pairs in testing data: " + testQADocSet.docs.size());
        
        //2. GenerateQA Match matrix
        genQAMatchMaxkP.geneateQAMatrix(trainQADocSet, termToWordVectorMap, Integer.valueOf(vectorDimen));
        genQAMatchMaxkP.geneateQAMatrix(testQADocSet, termToWordVectorMap, Integer.valueOf(vectorDimen));

        //3. Pair-wise training to learn optimal weight
        //   Output the final learned weights as the learned model
        //   Predict the matching score on test data and compute evaluation metrics
        PairwiseTraining pwtrain = new PairwiseTraining();
        pwtrain.initWeights(modelName, binNum, vectorDimen);
        //construct QA pos/neg triples and output to a file
        Set<String> trainQATriples = pwtrain.constructQAPNTriples(trainQADocSet);
        Set<String> testQATriples = pwtrain.constructQAPNTriples(testQADocSet);
        //FileUtil.saveClass(trainQADocSet, trainSerializedData);
        //FileUtil.saveClass(testQADocSet, testSerializedData);
        System.out.println("total number of QA triples in training data: " + trainQATriples.size());
        System.out.println("total number of QA triples in testing data: " + testQATriples.size());
        
        //pwtrain.saveQATripleFile(qaTriples, qaTripleFile);
        //Need to pass termToWordVectorMap to get the word embedding of the query words
        pwtrain.generateQrelFile(testQADocSet, ModelResDataFolder);
        
        System.out.println("modelName: " + modelName);
        if(modelName.equals("V5-1")){//V5-1 which is aNMM2
        	//V5-1 Add a hidden layer & Multiple bin sizes in the added hidden layer  MultiGranBGNN Model (used fixed bin size firstly then update)
        	//Need to implement this setting for softmax gate function which can further improve the performance
        	//Consider mini-batch gradient descent to get more robust performance
        	System.out.println("Current model: aNMM2");
        	pwtrain.pairwiseSGDTraining(trainQADocSet, trainQATriples, testQADocSet, testQATriples, termToWordVectorMap, ModelResDataFolder, modelName, binNum, vectorDimen, runModelType); 
        	
        } else {
        	System.err.println("In main, unsupported modelName type : " + modelName);
        }
    }
}
