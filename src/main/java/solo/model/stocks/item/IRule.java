package solo.model.stocks.item;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IRule
{
	boolean check(final StateAnalysisResult oStateAnalysisResult);
	boolean getIsOccurred();
	String getInfo(final Integer nRuleID);
	String getMessage();
}
