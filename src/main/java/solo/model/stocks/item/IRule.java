package solo.model.stocks.item;

import java.io.Serializable;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IRule extends Serializable
{
	String getInfo(final Integer nRuleID);
	void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID);
}
