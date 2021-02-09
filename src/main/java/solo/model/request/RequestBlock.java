package solo.model.request;

public class RequestBlock
{
    int allRequest = 0;
    int waitRequest = 0;
    int inProcRequest = 0;
    Long waitDuration = 0L;
    Long totalDuration = 0L;
    
    public RequestInfo addRequest(final String strMethod, final int nMaxTryCount)
    {
    	return new RequestInfo(strMethod, nMaxTryCount, this);
    }
    
	public void incAllRequest()
	{
		allRequest++;
	}
	
	public void incWaitRequest()
	{
		waitRequest++;
	}
	
	public void decWaitRequest()
	{
		if (waitRequest > 0)
			waitRequest--;
	}
	
	public void incInProcRequest()
	{
		inProcRequest++;
	}
	
	public void decInProcRequest()
	{
		if (inProcRequest > 0)
			inProcRequest--;
	}
	
	public void addWaitDuration(final long nDuration)
	{
		waitDuration += nDuration;
	}
	
	public void addTotalDuration(final long nDuration)
	{
		totalDuration += nDuration;
	}
	
	@Override
	public String toString() 
	{
    	final Long avgDuration = (allRequest > 0 ? totalDuration / allRequest : 0);
    	final Long avgWaitDuration = (allRequest > 0 ? waitDuration / allRequest : 0);
		return "Total[" + allRequest + "]/InProc[" + inProcRequest + "]/Wait[" + waitRequest + "]. Duration [" + avgDuration + "]/Wait[" + avgWaitDuration + "]";
	}
}