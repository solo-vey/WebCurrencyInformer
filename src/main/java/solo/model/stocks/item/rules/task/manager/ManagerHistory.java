package solo.model.stocks.item.rules.task.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import solo.utils.ResourceUtils;
import solo.utils.TraceUtils;

public class ManagerHistory
{
	protected final IStockExchange m_oStockExchange;
	protected final List<String> m_oHistory =  Collections.synchronizedList(new LinkedList<String>()); 
	
	public ManagerHistory(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		load();
	}
	
	public void addMessage(final String strMessage)
	{
		while (m_oHistory.size() > 100)
			m_oHistory.remove(0);
		
		final DateFormat oDateFormat = new SimpleDateFormat("HH:mm:ss");
		final String strDate = oDateFormat.format(new Date()) + "\t"; 
		m_oHistory.add(strDate + strMessage);
		save();
	}
	
	public List<String> getMessages()
	{
		return m_oHistory;
	}
	
	@Override public String toString() 
	{
		return StringUtils.join(m_oHistory, "\r\n");
	}
	
	public void save()
	{
		final String strStockEventsFileName = getFileName();

		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(strStockEventsFileName);
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(m_oHistory);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			TraceUtils.writeError("Save history exception : " + e.getMessage());
		}			
	}

	@SuppressWarnings("unchecked")
	public void load()
	{
		final String strStockEventsFileName = getFileName(); 
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(strStockEventsFileName);
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final List<String> oHistory = (List<String>) oStream.readObject();
	         m_oHistory.clear();
	         m_oHistory.addAll(oHistory);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (final Exception e) 
		{
			TraceUtils.writeError("Load history exception : " + e.getMessage() + "\r\n");
	    }			
	}

	String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\managerHistory.ser";
	}
}
