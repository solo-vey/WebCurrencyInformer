package solo.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import solo.BaseQueryServiceTest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;

/**
 * The Class FavoritesServiceTest.
 */
public class SearchServiceTest  extends BaseQueryServiceTest 
{
	 	/** поиск по документам
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearch() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language", "ru");
			oForm.add("q", "Ставка за 1 щільний куб. метр деревини");
//			oForm.add("strict_mode", "");
//			oForm.add("search_group","500-0000.0001.0000");
//			oForm.add("type_doc_facet[]","001-0005");
//			oForm.add("p","1");

			addUser(new String[] {"fulltext_search_access", "all_documents", "general_legislation"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	    }
	 	
	 	/** поиск по документам без учета ограничений по вектору лицензий
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchAllDocuments() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language", "ru");
			oForm.add("q", "авансовий звіт");

			addUser(new String[] {"fulltext_search_access", "general_legislation"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/all", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	    }
	 		 	
	 	/** поиск по документам с отладочной информацией
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchDebug() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language", "ru");
			oForm.add("q","(закон");
//			oForm.add("search_group","500-0000.0001.0000");
//			oForm.add("type_doc_facet[]","001-0005");
//			oForm.add("p","1");

			addUser(new String[] {"fulltext_search_access", "all_documents"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/debug", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	    }
	 	
	 	/** поиск по документам - у пользователя указан признак - поиск по всем документам
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchWithAliasAllDocuments() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language", "ru");
//			oForm.add("q","*");
//			oForm.add("search_group","500-0000.0001.0000");
//			oForm.add("type_doc_facet[]","001-0005");
//			oForm.add("p","1");

			addUser(new String[] {"fulltext_search_access", "all_documents"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }
	 	
	 	
	 	/** поиск по документам - у пользователя нет доступа к документам вообще
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchNoAccessToDocuments() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language", "ru");
//			oForm.add("q","*");
//			oForm.add("search_group","500-0000.0001.0000");
//			oForm.add("type_doc_facet[]","001-0005");
//			oForm.add("p","1");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }	
	 	
	 	/** поиск по документам - реквизитный поиск
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchParameter() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("q", "закон");
			oForm.add("title", "тест");
			oForm.add("number", "1");
			oForm.add("search_group", "000-010.020");
			oForm.add("classific_uniform", "000-010.020");
			oForm.add("type_doc", "000-010.020");
			oForm.add("type_doc_facet", "000-010.020");
			oForm.add("typed", "000-010.020");
			oForm.add("typed_facet", "000-010.020");
			oForm.add("typed_no_of", "000-010.020");
			oForm.add("typed_no_of_facet", "000-010.020");
			oForm.add("organization", "000-010.020");
			oForm.add("organization_facet", "000-010.020");
			oForm.add("status", "000-010.020");
			oForm.add("status_facet", "000-010.020");
			oForm.add("classific_npa", "000-010.020");
			oForm.add("classific_npa_facet", "000-010.020");
			oForm.add("country", "000-010.020");
			oForm.add("country_facet", "000-010.020");
			oForm.add("classific_know_tax", "000-010.020");
			oForm.add("bl", "KD0005");
			oForm.add("docs_changing", "KD0005");
			oForm.add("fl", "KD0005");
			oForm.add("docs_changed", "KD0005");
			oForm.add("date_accept_from", "01.01.2015");
			oForm.add("date_accept_to", "01.01.2015");
			oForm.add("date_reciept_from", "01.01.2015");
			oForm.add("date_reciept_to", "01.01.2015");
			oForm.add("date_reg_in_mu_from", "01.01.2015");
			oForm.add("date_reg_in_mu_to", "01.01.2015");
			oForm.add("date_region_reg_from", "01.01.2015");
			oForm.add("date_region_reg_to", "01.01.2015");
			oForm.add("date_begin_from", "01.01.2015");
			oForm.add("date_begin_to", "01.01.2015");
			oForm.add("nom_mu", "1");
			oForm.add("number_region_reg", "1"); 
			oForm.add("sud_name", "тест");
			oForm.add("deputat_name", "тест"); 
			oForm.add("deputat_facet", "000-010.020");
			oForm.add("form_sub", "тест");
			oForm.add("session_vr", "000-010.020");
			oForm.add("session_vr_facet", "000-010.020");
			oForm.add("komitet_name", "тест");
			oForm.add("bases", "1");
			oForm.add("bankrupcy_bases", "1");
			oForm.add("draft_laws_bases", "1");
			oForm.add("action", "ACTUAL");
			oForm.add("id", "KD0005");
			oForm.add("classific_copy_nlz", "000-010.020");
			oForm.add("organization_with_alias_facet", "000-010.020");
			oForm.add("sud_facet", "000-010.020");

			addUser(new String[] {"parameter_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/parameter", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }	 	

	 	
	 	/** поиск по документам - реквизитный поиск
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchParameterByNumber() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("number", "21/");

			addUser(new String[] {"parameter_search_access", "all_documents"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/parameter", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }	 	
	 	
	 	/** поиск по документам - фильтр по датам
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchFilterByDate() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("date_accept_from","15.03.1994");
			oForm.add("date_accept_to","15.03.1994");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }	 	

	 	/** поиск по документам с сортировкой по названию 
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchWithSortByTitle() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("q", "закон");
			oForm.add("date_accept_from", "01.06.2015");
			oForm.add("date_accept_to", "03.06.2015");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по документам с сортировкой по дате 
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchWithSortByDate() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("q", "закон");
			oForm.add("p", "1");
			oForm.add("sort_by", "date desc");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по документам с отсечениям по областям поиска 
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchWithGroupAllDocuments() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("q", "закон");
			oForm.add("search_group", "500-0000");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по обратным ссылкам документа - на весь документ
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchByBacklinksWholeDocument() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("bl","T030436");
			oForm.add("search_group", "500-0000");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по обратным ссылкам документа - на отдельный параграф
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchByBacklinksParagrapth() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("bl","T030436-9");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по документам которые изменяет данный
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchByDocsChanging() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("docs_changing","VS06840");
			oForm.add("search_group", "500-0000");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по документам на которые ссылается данный 
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchByForwardLinks() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("fl", "Z960254K");
			oForm.add("search_group", "500-0000");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** поиск по документам которые изменяют данный
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchDocsChanged() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("docs_changed", "Z960254K");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** получение подсказок поика
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchGetSuggest() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("common_search_query", "закон про амністю");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/suggest", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** получение подсказок поика
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchGetSuggestWithSpecialSymbols() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("common_search_query", "\"зако");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/suggest", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** получение подсказок поика
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchGetSuggestGarbageSymbols() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
//			oForm.add("language","ru");
			oForm.add("common_search_query", ":/");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/suggest", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** получение подсказок поика
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchGetSuggestEmptyString() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("common_search_query", " ");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/suggest", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** получение подсказок поика
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchGetSuggestWithParenthesis() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("common_search_query", "(закон");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/suggest", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }

	 	/** получение подсказок поика
		 * 
		 * @throws URISyntaxException
		 * @throws IOException */
	 	@Test
	    public void testSearchGetSuggestWithParenthesis2() throws URISyntaxException, IOException 
	    {
			// Arrange
			final Form oForm = new Form();
			oForm.add("language","ru");
			oForm.add("common_search_query", "закон)");

			addUser(new String[] {"fulltext_search_access"}, new String[] {});

	 		//	Act
	 		final ClientResponse oResponse = executePost("search/suggest", oForm);

	 		//	Assert
	 		checkResult(oResponse);
	   }
	 	
}
