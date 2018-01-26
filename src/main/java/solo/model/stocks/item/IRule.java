package solo.model.stocks.item;

import java.io.Serializable;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IRule extends Serializable
{
	String getInfo();
	RateInfo getRateInfo();
	int getID();
	void setID(final int nID);
	String getHelp(final String strCommandStart) throws Exception;
	void check(final StateAnalysisResult oStateAnalysisResult);
	void remove();
}
