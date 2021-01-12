package solo.model.stocks.item.command.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LastErrors
{
	protected final List<String> m_oErrors =  Collections.synchronizedList(new LinkedList<String>()); 
	
	public void addError(final String strError)
	{
		m_oErrors.remove(strError);
		while (m_oErrors.size() > 5)
			m_oErrors.remove(0);
		
		m_oErrors.add(strError);
	}
	
	public List<String> getErrors()
	{
		return m_oErrors;
	}
}
