package solo.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/** Работа с файлом настроек */
public final class ResourceUtils 
{
	/** The logger. */
	private static Logger s_oLogger = Logger.getLogger(ResourceUtils.class);
	
	/** Список уже зарегистрированных файлов с параметрами */
	private static Map<String, PropertyFileInfo> s_oRegisterProperyFiles = new HashMap<String, PropertyFileInfo>();

	/** Вычитываем в настройки проекта настройки из указанного файла 
	 * @return */
	public static PropertyFileInfo registerProperties(final String strProperyFile) 
	{
		try 
		{
			return PropertyFileInfo.getPropertyFileInfo(strProperyFile, s_oRegisterProperyFiles);
		} 
		catch (IOException e) 
		{
			Logger.getLogger(ResourceUtils.class).error(e.getMessage(),e);
		}
		
		return null;
	}
	
	/** Получение кирилических строк из property файла 
	 * @param strKey Ключ ресурса
	 * @param strProperyFile Имя файла настроек
	 * @return Строка */
	public static String getCyrylicResource(final String strKey, final String strProperyFile)
	{
		final PropertyFileInfo oProperties = registerProperties(strProperyFile);
		
		try 
		{ 
			final String strValue = new String(readProperty(strKey, oProperties).getBytes("ISO-8859-1"), "UTF-8"); 
			s_oLogger.info("Get propery [" + strKey + "] = [" + strValue + "]");
			return strValue; 
		} 
		catch(final Exception oException) 
		{
			final String strValue = readProperty(strKey, oProperties);
			s_oLogger.error("Error loading or converting string from ISO-8859-1 to UTF-8 propery [" + strKey + "] = [" + strValue + "]", oException);
			return strValue;
		}
	}
	
	/** Получение строкового значения из property файла (без учета кодировки)
	 * @param strKey Ключ ресурса
	 * @param strProperyFile Имя файла настроек
	 * @return строка без учета кодировки */
	public static String getResource(final String strKey, final String strProperyFile)
	{
		return getResource(strKey, strProperyFile, StringUtils.EMPTY);
	}
	
	/** Получение строкового значения из property файла (без учета кодировки) 
	 * @param strKey Ключ ресурса
	 * @param strDefault Значение по умолчанию
	 * @param strProperyFile Имя файла настроек
	 * @return строка без учета кодировки */
	public static String getResource(final String strKey, final String strProperyFile, final String strDefault)
	{
		final PropertyFileInfo oProperties = registerProperties(strProperyFile);
		final String strValue = readProperty(strKey, oProperties);
		if (null == strValue)
		{
			s_oLogger.info("Get propery [" + strKey + "]. Use default value [" + strDefault + "]");
			return strDefault;
		}
		
		s_oLogger.info("Get propery [" + strKey + "] = [" + strValue + "]");
		return strValue;
	}
	
	/** Получение числового значения из property файла 
	 * @param strKey Ключ ресурса
	 * @param nDefault Значение по умолчанию
	 * @param strProperyFile Имя файла настроек
	 * @return число */
	public static int getIntFromResource(final String strKey, final String strProperyFile, final int nDefault)
	{
		final PropertyFileInfo oProperties = registerProperties(strProperyFile);

		try 
		{ 
			final Integer nValue = Integer.parseInt(readProperty(strKey, oProperties).trim().replace("\t", StringUtils.EMPTY)); 
			s_oLogger.info("Get propery [" + strKey + "] = [" + nValue + "]");
			return nValue; 
		} 
		catch(final Exception oException) 
		{
			if (nDefault != Integer.MAX_VALUE && nDefault != Integer.MIN_VALUE)
				s_oLogger.error("Error loading or convert to int propery [" + strKey + "]. Use dafault value [" + nDefault + "]", oException);
			
			return nDefault;
		}
	}
	
	/** Получение логического значения из property файла 
	 * @param strKey Ключ ресурса
	 * @param strProperyFile Имя файла настроек
	 * @return число */
	public static boolean getBoolean(final String strKey, final String strProperyFile)
	{
		return "true".equalsIgnoreCase(getResource(strKey, strProperyFile));
	}

	/** Вычитываем значения из переменной окружения или property файла 
	 * @param strKey Имя ключа
	 * @param oProperties Файл с настройками
	 * @return Значение по указанному ключу. Если не нашли ни в переменных окружения ни в файле настроек - null
	 */
	private static String readProperty(final String strKey, final PropertyFileInfo oProperties)
	{
		final String strEnvironmentValueKey = oProperties.Name + "." + strKey; 
		final String strEnvironmentValue = System.getenv(strEnvironmentValueKey);
		if (null != strEnvironmentValue)
			return strEnvironmentValue;
		
		return oProperties.Properties.getProperty(strKey);
	}
}

/** Информация о файле настроек */
class PropertyFileInfo 
{
	/** Сами настройки */
	final public Properties Properties;
	/** Имя файла настроек */
	final public String Name;
	
	/** конструктор
	 * @param strProperyFile Имя файла настроек 
	 * @throws IOException */
	public PropertyFileInfo(final String strProperyFile, final Map<String, PropertyFileInfo> oRegisterProperyFiles) throws IOException
	{
		final Properties oProperties = new Properties();
		oProperties.load(ResourceUtils.class.getClassLoader().getResourceAsStream(strProperyFile));
		Properties = oProperties;
		Name = FilenameUtils.getBaseName(strProperyFile);

		oRegisterProperyFiles.put(strProperyFile, this);
	}

	/** Получаем данные о файле настроек по имени файла настроек
	 * @param strProperyFile Имя файла настроек
	 * @param oRegisterProperyFiles Список уже зарегистрированных файлов с параметрами 
	 * @return Информацию об указанном файле настроек 
	 * @throws IOException  */
	static PropertyFileInfo getPropertyFileInfo(final String strProperyFile, Map<String, PropertyFileInfo> oRegisterProperyFiles) throws IOException
	{
		if (oRegisterProperyFiles.containsKey(strProperyFile))
			return oRegisterProperyFiles.get(strProperyFile);
		
		return new PropertyFileInfo(strProperyFile, oRegisterProperyFiles);
	}
}

