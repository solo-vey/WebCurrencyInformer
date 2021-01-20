package solo;

import solo.model.stocks.worker.WorkerFactory;

/** Константы приложения */
public final class CurrencyInformer
{
	public static final String PROPERTIES_FILE_NAME = "WebCurrencyInformer.properties";
	public static final int DECIMAL_SCALE = 10; 
	
	CurrencyInformer() 
	{
		throw new IllegalStateException("Utility class");
	}
	
	public static void main(String[] args) throws Exception
    {
		WorkerFactory.start();
    }
}
