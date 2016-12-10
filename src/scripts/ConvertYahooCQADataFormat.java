package scripts;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.FileUtil;

/**Transfer Yahoo Webscope L4 and L6 (Yahoo CQA data) 
 * the format as defined in the input data for NNtextmatch
 * model
 * 
 * 1. Filter questions and answers by length between minLen and maxLen. e.g. 5-50
 * 2. Generate positive (question, answer) pair by group questions with their best answer
 * 3. Generate negative pairs
 *    3.1 Method 1: Negative sampling approach in the AAAI'16 paper by Shengxian Wan et. al. (Can also contact 
 *    				them for sharing the data through JF. In that case, we already have the results of multiple baselines)
 *    				A Deep Architecture for Semantic Matching with Multiple Positional Sentence Representaions
 *    				For each question, we first use its best answer as a query to retrieval 
 *    				the top 1000 results from the whole answer set, with Lucene or Galago,.
 *    				Then we randomly select 4 ansewrs from them to construct the negative pairs.
 *    3.2 Method 2: Approach by Daniel. Remove non-best answers. For each question, sample 4 best
 *    				answers of the other questions for it to construct negative pairs. For each 
 *    				question, the ratio of positive answer:negative answer = 1:4
 *    				Use the same version from Daniel can also enable us to directly compare with their BL
 *    				Can ask Daniel for his data version if needed
 * 4. Finally, split the whole dataset into training, validation and testing data with proportion 8:1:1
 * 5. Draw a table to show the statistics of the data
 * The raw data for L4 is manner.xml
 *    
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */

public class ConvertYahooCQADataFormat {

	public static void main(String[] args) throws FileNotFoundException, DocumentException {
		// TODO Auto-generated method stub

		String ModelInputDataPath = "data/YahooCQA/ModelInputData/Webscope_L4/";
		
		String rawDataPath = "data/YahooCQA/ModelOriginalData/Webscope_L4/";
		
		String rawDataFile = rawDataPath + "manner.xml";
		
		generateQsentQueriesFile(rawDataFile, ModelInputDataPath);
		System.out.println("data format transfering done!");
	}

	//generate the qsent file and the query file for nntextmatch model training
	private static void generateQsentQueriesFile(String rawDataFile, String modelInputDataPath) throws FileNotFoundException, DocumentException {
		// TODO Auto-generated method stub
		ArrayList<String> qSentLines = new ArrayList<String>(); //Format of qSent file: qid \t answer_sentid \t docid \t label \t sent   bestAnswer 1/nonBestAnswer 0
		ArrayList<String> queryLines = new ArrayList<String>(); //Format of query file: qid \t qsent \t qdesc  
		String queryFile = modelInputDataPath + "YahooCQA_l4.queries";
		String qSentFile = modelInputDataPath + "YahooCQA_l4.qsent";
		SAXReader reader = new SAXReader();
		Document doc = reader.read(new BufferedInputStream(
				new FileInputStream(new File(rawDataFile)))); //manner.xml
		Element e = doc.getRootElement();
		
		System.out.println("Number of elements in Root: " + e.nodeCount());
		Iterator<Element> docIterator = e.elementIterator("vespaadd");
		while(docIterator.hasNext()){
			Element docEleI = docIterator.next().element("document");
			String qid = docEleI.elementText("uri");
			String queryTitle = docEleI.elementText("subject");
			String queryDesc = docEleI.elementText("content");//.replaceAll("\n", " ");;
			if(queryDesc != null){
				queryDesc = queryDesc.replaceAll("\n", " ");
			}
			queryLines.add(qid + "\t" + queryTitle + "\t" + queryTitle); //We don't need queryDesc. Content of some queryDesc is also null.
			
			int answerID = 1;
			//Positive answer
			String bestanswer = docEleI.elementText("bestanswer").replaceAll("\n", " ");
			qSentLines.add(qid + "\t" + qid +"_" + answerID + "\t" + "D0\t1\t" + bestanswer);
			//Negative answer
			Element nbestanswers = docEleI.element("nbestanswers");
			Iterator<Element> answerItemIterator = nbestanswers.elementIterator("answer_item");
			while(answerItemIterator.hasNext()){
				answerID++;
				if(answerID == 2) {
					String nanswer = answerItemIterator.next().getText();
					continue;// this is duplicated best answer contained in nbestanswer field. need to confirm with Daniel
				}
				String nanswer = answerItemIterator.next().getText().replaceAll("\n", " ");
				qSentLines.add(qid + "\t" + qid +"_" + answerID + "\t" + "D0\t0\t" + nanswer);
			}
		}
		FileUtil.writeLines(qSentFile, qSentLines);
		FileUtil.writeLines(queryFile, queryLines);
	}
}
