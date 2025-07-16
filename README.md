Trading bot for Binance
- Trading based on SMA (Simple Moving Average) with a period of 15 minutes
- The first position is opened when the price and SMA cross the volume specified in the input field
- The position is averaged if the price rolls back by 2% (3% if there were more than 10 additional buys) from the entry price
- The volume of the additional buy is equal to half of the base volume
- Take profit is set at 1% of the entry price
- If there were more than 30 additional buys, then take profit is set at 0.2% of the entry price (The market is falling sharply and you need to close the position faster to save capital)
