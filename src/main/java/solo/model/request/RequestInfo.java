package solo.model.request;

import java.util.Date;

public class RequestInfo
{
	final RequestBlock requestBlock;
	
	int tryCount = 0;
	int maxTryCount;
	final Long start = (new Date()).getTime();
	Long startExecute;
	final String method;
	
	public RequestInfo(final String strMethod, final int nMaxTryCount, final RequestBlock oRequestBlock)
	{
		method = strMethod;
		maxTryCount = nMaxTryCount;
		requestBlock = oRequestBlock;
		
		requestBlock.incAllRequest();
		requestBlock.incWaitRequest();
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public void startExecute() 
	{
		requestBlock.decWaitRequest();
		requestBlock.incInProcRequest();
		startExecute = (new Date()).getTime();
	}

	public boolean tryMore()
	{
		tryCount++;
		return (tryCount <= maxTryCount);
	}
	
	public void finish()
	{
		requestBlock.decInProcRequest();
		requestBlock.addTotalDuration(getDuration());
		requestBlock.addWaitDuration(getWaitDuration());
	}
	
	public Long getDuration()
	{
		return ((new Date()).getTime() - startExecute);
	}
	
	public Long getWaitDuration()
	{
		return (startExecute - start);
	}
	
	@Override public String toString() 
	{
		return "[" + method + "] wait [" + getWaitDuration() + "] exec [" + getDuration() + "] Try [" + tryCount + "]";
	}
}