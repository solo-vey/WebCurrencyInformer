package solo.model.stocks.history;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.StockRateStates;
import ua.lz.ep.utils.ResourceUtils;

public class StocksHistory
{
	public static void addHistory(final IStockExchange oStockExchange, final StockRateStates oStockRateStates)
	{
		final boolean bIsSaveHistory = ResourceUtils.getBoolean("history.save", oStockExchange.getStockProperties());
		if (!bIsSaveHistory)
			return;
		
		final String strHistoryRoot = ResourceUtils.getResource("history.root", oStockExchange.getStockProperties(),
											ResourceUtils.getResource("history.root", CurrencyInformer.PROPERTIES_FILE_NAME));
		
		if (StringUtils.isBlank(strHistoryRoot))
			return;
		
		try
		{
			final String strJson = oStockRateStates.toJson();
			final String strDatePath = (new SimpleDateFormat("yyyy\\MM\\dd\\HH\\mm\\yyyy-MM-dd-HH-mm-ss")).format(new Date());
			final String strFullFileName = strHistoryRoot + "\\" + oStockExchange.getStockName() + "\\" + strDatePath + ".json"; 
			FileUtils.writeStringToFile(new File(strFullFileName), strJson);
		}
		catch (IOException e) {}
	}
}
