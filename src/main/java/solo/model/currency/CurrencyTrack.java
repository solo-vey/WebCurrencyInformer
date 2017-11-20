package solo.model.currency;

import java.util.LinkedList;
import java.util.List;

/** Отслеживание изменений курсов валют */
public class CurrencyTrack
{
	final static List<TrackInfo> s_oTrackCurrency = new LinkedList<TrackInfo>(); 
	
	/** Валюта с которой */
	public static void addTrack(final TrackInfo oCurrencyTrackInfo)
	{
		s_oTrackCurrency.add(oCurrencyTrackInfo);
	}
	
	public static List<TrackInfo> trackAll()
	{
		for(final TrackInfo oTrackInfo : s_oTrackCurrency)
		{
			
		}
		return null;
	}
}
