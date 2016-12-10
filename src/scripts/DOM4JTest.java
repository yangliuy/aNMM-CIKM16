package scripts;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class DOM4JTest {

	public static void main(String[] args) throws IOException, DocumentException {
		// TODO Auto-generated method stub
		SAXReader reader = new SAXReader();
		Document doc = reader.read(new BufferedInputStream(
				new FileInputStream(new File("data/YahooCQA/ModelOriginalData/Webscope_L4/tmp_datasample.xml")))); //manner.xml
		Element e = doc.getRootElement();
		
		System.out.println("Number of elements in Root: " + e.nodeCount());
		Iterator<Element> docIterator = e.elementIterator("vespaadd");
		while(docIterator.hasNext()){
			Element docEleI = docIterator.next().element("document");
			
			String XML = docEleI.asXML();
			
			//System.out.println(XML);
			String qid = docEleI.elementText("uri");
			String queryTitle = docEleI.elementText("subject");
			String queryDesc = docEleI.elementText("content");
			
			
			String bestanswer = docEleI.elementText("bestanswer");
			String nbestanswers = docEleI.elementText("nbestanswers");
			
			System.out.println(qid + "\t" + queryTitle + "\t" + queryDesc + "\t" + bestanswer + "\t" + nbestanswers);
			break;
			
//			Element docText = docEleI.element("TEXT");
			//System.out.println("docText" + docText.asXML());
			
//			Iterator<Element> NONESentIterator = docText.elementIterator("NONE");
//			while(NONESentIterator.hasNext()){
//				List<Element> noneSents = NONESentIterator.next().elements();
//				for(Element sent : noneSents){
//					System.out.println(sent.getText());
//				}
//			}
//			
//			Iterator<Element> EXCELSentIterator = docText.elementIterator("EXCEL");
//			break;
		}
//		String TARGET_QID = "701";
		
		//Test get element
//		System.out.println(e.element("DOC").attributeValue("pageNumber"));
//		Element lineEle = e.element("DOC").element("region").element("section").element("line");
//		System.out.println(lineEle.getText());
		
		//Test iterator
//		Iterator<Element> pageIterator = e.elementIterator("page");
//		while(pageIterator.hasNext()){
//			Element pageEleI = pageIterator.next();
//			System.out.println("------------------PageNum : " + pageEleI.attributeValue("pageNumber"));
//			Iterator<Element> regionIterator = pageEleI.elementIterator("region");
//			while(regionIterator.hasNext()){
//				Iterator<Element> sectionIterator = regionIterator.next().elementIterator("section");
//				while(sectionIterator.hasNext()){
//					Iterator<Element> lineIterator = sectionIterator.next().elementIterator("line");
//					while(lineIterator.hasNext()){
//						Element lineEleI = lineIterator.next();
//						System.out.println(lineEleI.getText());
//					}
//				}
//			}
//		}
	}
}
