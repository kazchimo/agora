package infra.exchange.coincheck.impl

object DryWriteCoincheckExchangeImpl
    extends DryCoincheckExchangeImpl with PublicTransactions with Transactions
    with DryOrders with DryOpenOrders with DryCancelOrder with DryCancelStatus
