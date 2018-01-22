package solo.model.stocks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    	final List<File> aFiles = new LinkedList<File>();
    	final File oFolder = new File("c:\\_2\\_3\\product\\product\\");
    	for (final File oFileEntry : oFolder.listFiles()) 
    	{
            if (oFileEntry.isDirectory())
                continue;
            aFiles.add(oFileEntry);
        }
    	
		/*FileWriter writer = new FileWriter("c:\\_2\\_3\\product\\product\\result\\userevents.txt"); 
    	for(final File oFile : aFiles)
    	{
    		BufferedReader reader = new BufferedReader(new FileReader (oFile));

    		String strLine = StringUtils.EMPTY;
    		while((strLine = reader.readLine()) != null) 
    		{
    			if (!strLine.contains("userevent"))
    				continue;
    			writer.write(strLine + "\r\n");
        	
    		}
    		reader.close();
    	}
     	writer.close();
   
     	BufferedReader reader = new BufferedReader(new FileReader ("c:\\_2\\_3\\product\\product\\result\\userevents.txt"));
    	writer = new FileWriter("c:\\_2\\_3\\product\\product\\result\\userevents.txt1"); 

    	String strLine = StringUtils.EMPTY;
        while((strLine = reader.readLine()) != null) 
        {
        	final String[] strParts = strLine.split("\\|");
        	final String strIP = strParts[0];
        	final String strEvent = strParts[3].replace("undefined", strIP).split("\\ ")[1];
        	
        	final String[] aEventParts = strEvent.split("\\/");
        	for (int nPos = 2; nPos < aEventParts.length; nPos++)
        		writer.write(aEventParts[nPos] + "\t");
        	writer.write("\r\n");
        	
        }
        writer.close();
        reader.close();*/
    
    	BufferedReader reader = new BufferedReader(new FileReader ("c:\\_2\\_3\\product\\product\\result\\userevents.txt1"));
    	FileWriter writer = new FileWriter("c:\\_2\\_3\\product\\product\\result\\userevents.txt2"); 

    	String strLine = StringUtils.EMPTY;
    	final Map<String, Map<String, Integer>> aUserEventCount = new HashMap<String, Map<String, Integer>>();
    	final Map<String, Boolean> aAllEvents = new HashMap<String, Boolean>();
        while((strLine = reader.readLine()) != null) 
        {
        	final String[] strParts = strLine.split("\t");
        	if (strParts.length < 2)
        		continue;
        	
        	final String strLigaID = strParts[0];
        	final String strEvent = strParts[1];
        	
        	if (strEvent.startsWith("?"))
        		continue;
        	
        	if (!aAllEvents.containsKey(strEvent))
        		aAllEvents.put(strEvent, true);
        	
        	Map<String, Integer> oEventCount = aUserEventCount.get(strLigaID);
        	if (!aUserEventCount.containsKey(strLigaID))
        	{
        		oEventCount = new HashMap<String, Integer>();
        		aUserEventCount.put(strLigaID, oEventCount);
        	}
        	
        	Integer nCount = oEventCount.get(strEvent);
        	if (null == nCount)
        		nCount = 0;
        	oEventCount.put(strEvent, nCount + 1);
        }
        
        String strLastEvent = StringUtils.EMPTY;
        for (final String strEvent : aAllEvents.keySet())
        	strLastEvent = strEvent;
        
        writer.write("User\tisBot\tisAnonym\t");
        for (final String strEvent : aAllEvents.keySet())
    		writer.write(strEvent + (strEvent.equalsIgnoreCase(strLastEvent) ? StringUtils.EMPTY : "\t"));
        writer.write("\r\n");
        
        boolean bIsFirst = true;
        for(final Entry<String, Map<String, Integer>> oUserInfo : aUserEventCount.entrySet())
        {
        	if (!bIsFirst)
        		writer.write("\r\n");
        	
        	writer.write(oUserInfo.getKey() + "\t");
        	writer.write((oUserInfo.getKey().startsWith("order.test@") || 
        			oUserInfo.getKey().startsWith("test1803@bonita") || oUserInfo.getKey().startsWith("test03049@bonita") ? "1" : "0") + "\t");
        	writer.write((oUserInfo.getKey().contains("@") ? "0" : "1") + "\t");
        	
        	
        	for (final String strEvent : aAllEvents.keySet())
        	{
        		final Integer oCount = oUserInfo.getValue().get(strEvent);
        		writer.write((null == oCount ? 0 : oCount) + (strEvent.equalsIgnoreCase(strLastEvent) ? StringUtils.EMPTY : "\t"));
        	}
        	bIsFirst = false;
        }
        
        writer.close();
        reader.close();
    }
}