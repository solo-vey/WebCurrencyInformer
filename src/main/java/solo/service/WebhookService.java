package solo.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Получение   */
@Path("/webhook")
public class WebhookService 
{
	/** Получение справочника указаного типа 
	 * @param strType Тип стпавочника
	 * @param oParameters Все параметры запроса
	 * @param oRequest Сам РЕЕЗ запрос
	 * @return Возвращается JSON объект который в зависимости от типа справочника предстваляет собой либо линейный список записей, либо дерево (для классификаторв)
	 * @throws Exception */
	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	public Response webhook(@Context HttpServletRequest oRequest) throws Exception
	{
		return null;
	}
}
