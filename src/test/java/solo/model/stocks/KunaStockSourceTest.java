package solo.model.stocks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import solo.model.stocks.worker.WorkerFactory;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }
    
    @Test public void test() throws Exception
    {
    	BufferedReader reader = new BufferedReader(new FileReader ("c:\\_2\\_3\\access.log"));
    	FileWriter writer = new FileWriter("c:\\_2\\_3\\userevents.txt"); 

    	String strLine = StringUtils.EMPTY;
        while((strLine = reader.readLine()) != null) 
        {
        	if (!strLine.contains("userevent"))
        		continue;
        	writer.write(strLine + "\r\n");
        	
        }
        writer.close();
        reader.close();
    }
    
    
    @Test public void test1() throws Exception
    {
    	BufferedReader reader = new BufferedReader(new FileReader ("c:\\_2\\_3\\userevents.txt"));
    	FileWriter writer = new FileWriter("c:\\_2\\_3\\userevents1.txt"); 

    	String strLine = StringUtils.EMPTY;
        while((strLine = reader.readLine()) != null) 
        {
        	final String[] strParts = strLine.split("\\ ");
        	final String strIP = strParts[0];
        	final String strEvent = strParts[6].replace("undefined", strIP);
        	
        	final String[] aEventParts = strEvent.split("\\/");
        	for (int nPos = 2; nPos < aEventParts.length; nPos++)
        		writer.write(aEventParts[nPos] + "\t");
        	writer.write("\r\n");
        	
        }
        writer.close();
        reader.close();
    }
    
    @Test public void test2() throws Exception
    {
    	BufferedReader reader = new BufferedReader(new FileReader ("c:\\_2\\_3\\userevents1.txt"));
    	FileWriter writer = new FileWriter("c:\\_2\\_3\\userevents2.txt"); 

    	String strLine = StringUtils.EMPTY;
        while((strLine = reader.readLine()) != null) 
        {
        	final String[] strParts = strLine.split("\t");
        	final String strLigaID = strParts[0];
        	final String strEvent = strParts[1];
        	
        	final String[] aEventParts = strEvent.split("\\/");
        	for (int nPos = 2; nPos < aEventParts.length; nPos++)
        		writer.write(aEventParts[nPos] + "\t");
        	writer.write("\r\n");
        	
        }
        writer.close();
        reader.close();
    }
}