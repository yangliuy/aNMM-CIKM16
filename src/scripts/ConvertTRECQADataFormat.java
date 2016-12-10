package scripts;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.FileUtil;

/**Transfer the TREC-QA data to the format as defined
 * in the input sentence data for NNtextmatch model
 *
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */


public class ConvertTRECQADataFormat {

	public static void main(String[] args) throws IOException, DocumentException {
		// TODO Auto-generated method stub
		//train-less-than-40.manual-edit.xml
		//dev-less-than-40.manual-edit.xml
		//test-less-than-40.manual-edit.xml
		//train2393.cleanup.xml
		//0303
		
		String [] fileNames = {"train-less-than-40.manual-edit", "dev-less-than-40.manual-edit", "test-less-than-40.manual-edit", "train2393.cleanup"};
		String modelInputDataPath = "data/TRECQA/ModelInputData/";
		for(String fileName : fileNames){
			System.out.println("current file name: " + fileName);
			String qSentFile = modelInputDataPath + fileName + ".qsent";
			String queryFile = modelInputDataPath + fileName + ".queries";
			ArrayList<String> qSentLines = new ArrayList<String>(); //qid \t sentid \t docid \t label \t sent
			ArrayList<String> queryLines = new ArrayList<String>(); //qid \t qsent
			
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new BufferedInputStream(
					new FileInputStream(new File(modelInputDataPath + fileName + ".xml"))));
			Element e = doc.getRootElement();
			
			System.out.println("Number of elements in Root: " + e.nodeCount());
			int qid = 0;
			Iterator<Element> docIterator = e.elementIterator("QApairs");
			while(docIterator.hasNext()){
				qid++;
				int sentid = 1;
				Element docEleI = docIterator.next();
				String question = getFirstLine(docEleI.elementText("question"));
				System.out.println("question: " + question);
				String qidComplete;
				if(fileName.contains("train")){
					qidComplete = "train_" + qid;
				} else if(fileName.contains("test")) {
					qidComplete = "test_" + qid;
				} else {
					qidComplete = "dev_" + qid;
				}
				queryLines.add(qidComplete + "\t" + question);

				Iterator<Element> posAnswersIterator = docEleI.elementIterator("positive");
				while(posAnswersIterator.hasNext()){
					Element posEle = posAnswersIterator.next();
					//System.out.println("pos: " + getFirstLine(posEle.asXML()));
					qSentLines.add(qidComplete + "\t" + qidComplete + "_asent_" + sentid + "\tD0\t1\t" + getFirstLine(posEle.asXML()));
					sentid++;
				}

				Iterator<Element> negAnswersIterator = docEleI.elementIterator("negative");
				while(negAnswersIterator.hasNext()){
					Element negEle = negAnswersIterator.next();
					//System.out.println("neg: " + getFirstLine(negEle.asXML()));
					qSentLines.add(qidComplete + "\t" + qidComplete + "_asent_" + sentid + "\tD0\t0\t" + getFirstLine(negEle.asXML()));
					sentid++;
				}
			}
			FileUtil.writeLines(queryFile, queryLines);
			FileUtil.writeLines(qSentFile, qSentLines);
		}
	}
	
	private static String getFirstLine(String questionText) {
		// TODO Auto-generated method stub
		//System.out.println(questionText);
		if(questionText == null){
			return null;
		}
		return questionText.split("\n")[1].replaceAll("\t", " ");
	}
}
