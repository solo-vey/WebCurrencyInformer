package solo.model.stocks.item;

public enum OrderSide
{
	/** Продажа */
	SELL,
	/** Покупка */
	BUY;
	
	@Override public String toString()
	{
		return super.toString().toLowerCase();
	}
}
