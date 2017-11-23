package solo.model.stocks;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import solo.model.currency.Currency;
import ua.lz.ep.utils.ResourceUtils;

public class MockStockSource extends BaseStockSource
{
	final protected String m_strDataRoot;
	protected Date m_oLastDate = new Date();
	protected Date m_oMaxDate = new Date();
	
	public MockStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strDataRoot = ResourceUtils.getResource("mock.data.root", getStockExchange().getStockProperties());
//		registerRate(new RateInfo(Currency.BTC, Currency.UAH));
		registerRate(new RateInfo(Currency.ETH, Currency.UAH));
	}
	
	void setDateStart(final Date oStartDate, final Date oMaxDate)
	{
		m_oLastDate = oStartDate;
		m_oMaxDate = oMaxDate;
	}
	
	public StockRateStates getStockRates() throws Exception
	{
		while (m_oMaxDate.after(m_oLastDate))
		{
			final String strDatePath = (new SimpleDateFormat("yyyy\\MM\\dd\\HH\\mm\\yyyy-MM-dd-HH-mm-ss")).format(m_oLastDate);
			final String strFullFileName = m_strDataRoot + "\\" + strDatePath + ".json";
			final File oData = new File(strFullFileName);
			if (oData.exists())
			{
				final String strJson = new String(Files.readAllBytes((new File(strFullFileName)).toPath()));
				final GsonBuilder oGsonBuilder = new GsonBuilder();

				final Gson oGson = oGsonBuilder.create();
				return oGson.fromJson(strJson, StockRateStates.class);
				
//				return JsonUtils.fromJson(strJson, StockRateStates.class);
			}
			
			m_oLastDate = DateUtils.addSeconds(m_oLastDate, 1);
		}
		
		throw new Exception("Is no more rates");
	}
}
