package solo.model.stocks.item.command.base;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.utils.ResourceUtils;
import solo.utils.TraceUtils;

public class CommandHistory
{
	protected final IStockExchange m_oStockExchange;
	protected final List<String> m_oHistory =  Collections.synchronizedList(new LinkedList<String>()); 
	
	public CommandHistory(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		load();
	}
	
	public void addCommand(final ICommand oCommand)
	{
		if (!(oCommand instanceof IHistoryCommand))
			return;
		
		final String strCommandLine = oCommand.getCommandLine();  
		final String strFullCommand = CommandFactory.getCommandName(oCommand.getClass()) + 
			(StringUtils.isNotBlank(strCommandLine) ? "_" + strCommandLine.replace(" ", "_") : StringUtils.EMPTY);
		m_oHistory.remove(strFullCommand);
		while (m_oHistory.size() > 50)
			m_oHistory.remove(0);
		
		m_oHistory.add(strFullCommand);
		save();
	}
	
	public List<String> getCommands()
	{
		return m_oHistory;
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
			save();
	    }			
	}

	String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\history.ser";
	}
}
