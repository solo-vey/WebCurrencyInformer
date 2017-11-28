package solo.model.stocks.item;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import ua.lz.ep.utils.JsonUtils;
import ua.lz.ep.utils.ResourceUtils;

public class Events
{
	final protected IStockExchange m_oStockExchange;
	final protected List<Event> m_oEventList = new LinkedList<Event>();
	
	public Events(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
	}
	
	public void addEvent(final Event oEvent)
	{
		m_oEventList.add(oEvent);
		save();
	}
	
	public void removeEvent(final Event oEvent)
	{
		m_oEventList.remove(oEvent);
		save();
	}
	
	public void removeAllOccurred()
	{
		final List<Event> aOccurred = new LinkedList<Event>();
		for(final Event oEvent : getList())
		{
			if (oEvent.getIsOccurred())
				aOccurred.add(oEvent);
		}
		m_oEventList.removeAll(aOccurred);
		save();
	}
	
	public Collection<Event> getList()
	{
		return m_oEventList;
	}
	
	private void save()
	{
		try
		{
			final String strJson = JsonUtils.toJson(m_oEventList);
			final String strStockEventsFileName = getFileName();
			FileUtils.writeStringToFile(new File(strStockEventsFileName), strJson);
		}
		catch (IOException e){	}
	}

	protected void load()
	{
		try
		{
			final String strStockEventsFileName = getFileName(); 
			String strJson = new String(Files.readAllBytes((new File(strStockEventsFileName)).toPath()));
			final GsonBuilder oGsonBuilder = new GsonBuilder();
	
			final Gson oGson = oGsonBuilder.create();
			final Type oType = new TypeToken<List<Event>>(){}.getType();
			final List<Event> oEventList = oGson.fromJson(strJson, oType);
			m_oEventList.clear();
			m_oEventList.addAll(oEventList);
		}
		catch (IOException e) { 		}
	}

	/**
	 * @return
	 */
	String getFileName()
	{
		final String strStockEventsFileName = ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\events.json";
		return strStockEventsFileName;
	}
}
