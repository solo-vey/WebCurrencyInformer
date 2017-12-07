package solo.model.stocks.item.command.base;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.command.system.SendMessageCommand;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.ITransport;

abstract public class BaseCommand extends HasParameters implements ICommand
{
	public BaseCommand(final String strRuleInfo, final String strParametersTemplate)
	{
		super(strRuleInfo, strParametersTemplate);
	}

	public String getHelp()
	{
		return "/" + CommandFactory.getCommandName(getClass()) + (StringUtils.isNotBlank(getTemplate()) ? "_" + getTemplate() : StringUtils.EMPTY);
	}
	
	public static MainWorker getMainWorker()
	{
		return WorkerFactory.getMainWorker();
	}
	
	public static ITransport getTransport()
	{
		return WorkerFactory.getMainWorker().getTransport();
	}
	
	public static IStockExchange getStockExchange()
	{
		return WorkerFactory.getMainWorker().getStockExchange();
	}
	
	public void execute() throws Exception
	{
	}
	
	public String getInfo()
	{
		return m_strCommandInfo;
	}
	
	public static String getCommand(final String strTemplate)
	{
		return "/" + strTemplate;
	}
	
	public void sendMessage(final String strMessage)
	{
		if (StringUtils.isBlank(strMessage))
			return;
		
		final ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}
