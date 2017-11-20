package solo;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import ua.lz.ep.utils.UnitTestUtils;

import org.codehaus.jackson.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import java.io.*;


/**
 * The Class FavoritesServiceTest.
 */
public class BaseQueryServiceTest  extends JerseyTest 
{
	/** Инициализация тестов
	 * 
	 * @throws Exception */
	public BaseQueryServiceTest()
	{
		super(new WebAppDescriptor.Builder("ua.lz.ep.service")
			 		 .initParam("com.sun.jersey.spi.container.ContainerRequestFilters", "ua.lz.ep.service.filter.AuthorizationRequestFilter")
					 .initParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
				.contextPath("webEp").build());
	}

	/** Добавляем сесию пользователя в мемкеш  
	 * @throws IOException 
	 * @throws JsonProcessingException  */
	@Before
	public void addUser() throws JsonProcessingException, IOException
	{
		UnitTestUtils.addUser(new String[] {}, new String[] {});
	}

	/** Добавляем сесию пользователя в мемкеш с указанием связанных с ним алиасов  
	 * @param aDataAliases Список алиасов данных 
	 * @param aFunctionAliases Список алиасов функциональности
	 * @throws JsonProcessingException
	 * @throws IOException */
	public void addUser(final String[] aDataAliases, final String[] aFunctionAliases) throws JsonProcessingException, IOException
	{
		UnitTestUtils.addUser(aDataAliases, aFunctionAliases);
	}
	
	/** Удаляем сесию пользователя */
	@After
	public void deleteUser()
	{
		UnitTestUtils.deleteUser();
	}	 

	/** Выполняем POST запрос 
	 * @param strPath Путь к рест сервису
	 * @return Ответ сервера */
	public ClientResponse executeGet(String strPath)
	{
		return UnitTestUtils.executeGet(strPath, resource());
	}

	/** Выполняем POST запрос 
	 * @param strPath Путь к рест сервису
	 * @param oForm Параметры запроса
	 * @return Ответ сервера */
	public ClientResponse executePost(String strPath, final Form oForm)
	{
		return UnitTestUtils.executePost(strPath, resource(), oForm);
	}

	/** Выполняем POST запрос 
	 * @param strPath Путь к рест сервису
	 * @param requestBody Параметры запроса
	 * @param mediaType Медиа тип
	 * @return Ответ сервера */
	public ClientResponse executePost(String strPath, final Object requestBody, final String mediaType)
	{
		return UnitTestUtils.executePost(strPath, resource(), requestBody, mediaType);
	}

	/** Проверяем ответ сервера
	 * @param oResponse Ответ сервера 
	 * @return Ответ сервера 
	 * @throws IOException 
	 * @throws JsonProcessingException */
	public static String checkResult(final ClientResponse oResponse, int nCode, int nMaxJsonLength) throws JsonProcessingException, IOException
	{
		return UnitTestUtils.checkResult(oResponse, nCode, nMaxJsonLength);
	}

	/** Проверяем ответ сервера - стандартная проверка - код ответа 200
	 * @param oResponse Ответ сервера 
	 * @return Ответ сервера 
	 * @throws IOException 
	 * @throws JsonProcessingException */
	public static String checkResult(final ClientResponse oResponse) throws JsonProcessingException, IOException
	{
		return UnitTestUtils.checkResult(oResponse);
	}
	
	/** Проверяем ответ сервера - стандартная проверка - код ответа 403
	 * @param oResponse Ответ сервера 
	 * @return Ответ сервера 
	 * @throws IOException 
	 * @throws JsonProcessingException */
	public static String checkResultNoAccess(final ClientResponse oResponse) throws JsonProcessingException, IOException
	{
		return UnitTestUtils.checkResultNoAccess(oResponse);
	}
}
