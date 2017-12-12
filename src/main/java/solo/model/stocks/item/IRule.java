package solo.model.stocks.item;

import java.io.Serializable;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IRule extends Serializable
{
	String getInfo(final Integer nRuleID);
	String getHelp(final String strCommandStart) throws Exception;
	void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID);
}
