package solo.model.stocks.item.rules.task;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.HasParameters;
import solo.utils.CommonUtils;

public class TaskFactory extends HasParameters implements IRule
{
	private static final long serialVersionUID = 908092157964890096L;

	final static public String RATE_PARAMETER = "#rate#";
	final static public String TASK_TYPE_PARAMETER = "#taskType#";

	protected static Map<TaskType, Class<?>> s_oTaskClassByType = new HashMap<TaskType, Class<?>>();

	final protected TaskBase m_oTaskBase;
	
	static
	{
		registerTaskClass(TaskType.QUICKSELL,  TaskQuickSell.class);
	}
	
	static protected void registerTaskClass(final TaskType oTaskType, final Class<?> oClass)
	{
		s_oTaskClassByType.put(oTaskType, oClass);
	}
	
	static public TaskBase getTask(final TaskType oTaskType, final RateInfo oRateInfo, final String strPriceInfo) throws Exception
	{
		final Class<?> oClass = (Class<?>) s_oTaskClassByType.get(oTaskType);
		if (null == oClass)
			return null;
		
		final Constructor<?> oConstructor = oClass.getConstructor(RateInfo.class, String.class);
		return (TaskBase) oConstructor.newInstance(new Object[] { oRateInfo, strPriceInfo });
	}
	
	public TaskFactory(final String strCommandLine) throws Exception
	{
		super(strCommandLine, CommonUtils.mergeParameters(RATE_PARAMETER, TASK_TYPE_PARAMETER, TAIL_PARAMETER));
		final RateInfo oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
		final TaskType oType = (TaskType) getParameterAsEnum(TASK_TYPE_PARAMETER, TaskType.class);
		final String strPriceInfo = getParameter(TAIL_PARAMETER);
		m_oTaskBase = getTask(oType, oRateInfo, strPriceInfo);
	}
	
	public String getHelp(final String strCommandStart) throws Exception
	{
		String strHelp = StringUtils.EMPTY;
		for(final Entry<TaskType, Class<?>> oTaskInfo : s_oTaskClassByType.entrySet())
		{
			final String strCommandStartForTaskType = CommonUtils.mergeParameters(strCommandStart, RATE_PARAMETER, oTaskInfo.getKey().toString().toLowerCase());
			strHelp += getTask(oTaskInfo.getKey(), null, StringUtils.EMPTY).getHelp(strCommandStartForTaskType) + "\r\n";
		}
		
		return strHelp;
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return (null != m_oTaskBase ? m_oTaskBase.getInfo(nRuleID) : "/removeRule_" + nRuleID + "\r\n");
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		if (null != m_oTaskBase)
			m_oTaskBase.check(oStateAnalysisResult, nRuleID);
	}
	
	@Override public boolean equals(Object obj)
	{
		if (m_oTaskBase.equals(obj))
			return true;
		
		return super.equals(obj);
	}
}

