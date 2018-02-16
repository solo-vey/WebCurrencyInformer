package solo.model.stocks.item.rules.task.trade;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.ResourceUtils;

public class TradeHistory implements Serializable
{
	private static final long serialVersionUID = 5109523570126715297L;
	
	protected String m_strHistory = StringUtils.EMPTY;
	protected File m_oFile;
	protected int m_nRuleID = 0;
	protected String m_strType = StringUtils.EMPTY;
	
	public TradeHistory(final int nRuleID, final String strType)
	{
		m_nRuleID = nRuleID;
		m_strType = strType;
	}

	public void addToHistory(final String strMessage)
	{
		m_strHistory += strMessage + "\r\n";
		addToLog(strMessage);
	}
	
	public void addToLog(final String strMessage)
	{
		try 
		{
			final Path oPath = Paths.get(getFileName());
			if (!Files.exists(oPath, LinkOption.NOFOLLOW_LINKS))
			{
				if (!Files.exists(oPath.getParent(), LinkOption.NOFOLLOW_LINKS))
					Files.createDirectory(oPath.getParent());
				Files.createFile(oPath);
			}
			
			final DateFormat oDateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
		    Files.write(Paths.get(getFileName()), (oDateFormat.format(new Date()) + "\t" + strMessage + "\r\n").getBytes(), StandardOpenOption.APPEND);
		}
		catch (IOException e) {}
	}	
	
	public String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + WorkerFactory.getStockExchange().getStockName() + "\\" + m_strType + "\\" + m_nRuleID + ".txt";
	}
	
	@Override public String toString()
	{
		return m_strHistory;
	}
}
