package solo.model.stocks.item;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.trade.RemoveOrderCommand;
import solo.utils.MathUtils;

public class Order extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -9072937437513312951L;
	
	public final static String CANCEL = "cancel";
	public final static String DONE = "done";
	public final static String ERROR = "error";
	public final static String EXCEPTION = "exception";
	public final static String NONE = "none";
	public final static String WAIT = "wait";
	
	public final static Order NULL = new Order(StringUtils.EMPTY, NONE, "Null order"); 
	
	protected String m_strID;
	protected String m_strState;
	protected OrderSide m_oSide;
	protected Date m_oCreated;
	protected String m_strMessage;
	
	protected BigDecimal m_nPrice;
	protected BigDecimal m_nVolume;
	protected BigDecimal m_nSum = null;
	
	protected Order m_oOriginalOrder = null;
	
	public Order()
	{
	}
	
	public Order(final Order oOriginalOrder)
	{
		m_oOriginalOrder = oOriginalOrder;
	}
	
	public Order(final String strID, final String strState, final String strMessage)
	{
		m_strID = strID;
		m_strState = strState;
		m_strMessage = strMessage;
	}
	
	public Order(final String strState, final String strMessage)
	{
		m_strState = strState;
		m_strMessage = strMessage;
	}
	
	public boolean isNull()
	{
		return StringUtils.isBlank(m_strID);
	}
	
	public boolean isCanceled()
	{
		return getState().equalsIgnoreCase(CANCEL);
	}
	
	public boolean isError()
	{
		return getState().equalsIgnoreCase(ERROR) || isException();
	}
	
	public boolean isException()
	{
		return getState().equalsIgnoreCase(EXCEPTION);
	}
	
	public boolean isDone()
	{
		return getState().equalsIgnoreCase(DONE);
	}
	
	public String getId()
	{
		return (null != m_strID ? m_strID : StringUtils.EMPTY);
	}
	
	public void setId(final String strID)
	{
		m_strID = strID;
	}
	
	public OrderSide getSide()
	{
		return m_oSide;
	}
	
	public void setSide(final OrderSide oSide)
	{
		m_oSide = oSide;
	}
	
	public void setSide(final String strSide)
	{
		if (strSide.equalsIgnoreCase(OrderSide.SELL.toString()))
			m_oSide = OrderSide.SELL;
		else
		if (strSide.equalsIgnoreCase("ask"))
			m_oSide = OrderSide.SELL;
		else
			m_oSide = OrderSide.BUY;
	}
	
	public String getState()
	{
		return (null != m_strState ? m_strState : NONE);
	}
	
	public void setState(final String strState)
	{
		m_strState = strState;
		if (m_strState.equalsIgnoreCase("canceled"))
			m_strState = CANCEL;
		if (m_strState.equalsIgnoreCase("processed"))
			m_strState = DONE;
		if (m_strState.equalsIgnoreCase("processing"))
			m_strState = WAIT;
		if (m_strState.equalsIgnoreCase("core_error"))
			m_strState = EXCEPTION;
	}
	
	public BigDecimal getPrice()
	{
		return m_nPrice;
	}
	
	public void setPrice(final BigDecimal nPrice)
	{
		m_nPrice = nPrice;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public void setVolume(final BigDecimal nVolume)
	{
		m_nVolume = nVolume;
	}
	
	public BigDecimal getSum()
	{
		if (null != m_nSum)
			return m_nSum;
		
		return (null == m_nPrice || null == m_nVolume ? BigDecimal.ZERO : m_nPrice.multiply(m_nVolume));
	}
	
	public void setSum(final BigDecimal nSum)
	{
		m_nSum = nSum;
	}	
	
	public Date getCreated()
	{
		return m_oCreated;
	}
	
	public void setCreated(final Date oCreated)
	{
		m_oCreated = oCreated;
	}
	
	public void setCreated(final String strCreated, final String strFormat)
	{
		try
		{
			final SimpleDateFormat oFormatter = new SimpleDateFormat(strFormat);
			setCreated(oFormatter.parse(strCreated));
		}
		catch (ParseException e) 
		{ 
			System.err.println("Error parsing date [" + strCreated + "]");
		}		
	}
	
	public String getMessage()
	{
		return (null != m_strMessage ? m_strMessage : StringUtils.EMPTY);
	}
	
	public Order getOriginalOrder()
	{
		return m_oOriginalOrder;
	}

	public String getInfoShort()
	{
		if (isNull() && getSum().compareTo(BigDecimal.ZERO) == 0)
			return "Null order" + (StringUtils.isNotBlank(getMessage()) ? " " + getMessage() : StringUtils.EMPTY)
			 	+ (StringUtils.isNotBlank(getState()) ? ". State [" + getState() + "]" : StringUtils.EMPTY);
		
		final String strOriinalInfoShort = (null != getOriginalOrder() ? getOriginalOrder().getInfoShort() : StringUtils.EMPTY);
		
		return getSide() + "/" + MathUtils.toCurrencyStringEx3(getPrice()) + 
			"/" + MathUtils.toCurrencyStringEx3(getSum()) +
			(StringUtils.isNotBlank(getMessage()) ? " " + getMessage() : StringUtils.EMPTY) + 
			(StringUtils.isNotBlank(strOriinalInfoShort) ? " Original : " + strOriinalInfoShort : StringUtils.EMPTY);
	}
	
	public String getInfo()
	{
		return getInfoShort() +
			" " + CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, getId());
	}
}

