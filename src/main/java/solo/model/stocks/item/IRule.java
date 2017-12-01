package solo.model.stocks.item;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IRule
{
	String getInfo(final Integer nRuleID);
	void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID);
}
