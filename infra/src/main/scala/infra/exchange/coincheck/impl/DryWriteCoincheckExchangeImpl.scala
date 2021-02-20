package infra.exchange.coincheck.impl

final case class DryWriteCoincheckExchangeImpl(orderSettledInterval: Int)
    extends DryCoincheckExchangeImpl with PublicTransactions with Transactions
    with DryOrders with DryOpenOrders with DryCancelOrder with DryCancelStatus
