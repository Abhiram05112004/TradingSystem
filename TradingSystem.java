import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

// User class to represent buyers and sellers
class User {
    private String name;
    private double balance;

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

// Stock class to represent the stock being traded
class Stock {
    private String symbol;
    private double price;

    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

// Enum for Order Types
enum OrderType {
    BUY,
    SELL
}

// Enum for Order Types
enum OrderCategory {
    LIMIT,
    MARKET
}

// Order class representing a buy or sell order
class Order {
    private User user;
    private Stock stock;
    private int quantity;
    private OrderType orderType;
    private double price;
    private OrderCategory orderCategory;

    public Order(User user, Stock stock, int quantity, OrderType orderType, double price, OrderCategory orderCategory) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.orderType = orderType;
        this.price = price;
        this.orderCategory = orderCategory;
    }

    public User getUser() {
        return user;
    }

    public Stock getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public double getPrice() {
        return price;
    }

    public OrderCategory getOrderCategory() {
        return orderCategory;
    }
}

// Class to manage the order book and matching logic
class OrderBook {
    private PriorityQueue<Order> buyOrders;
    private PriorityQueue<Order> sellOrders;

    public OrderBook() {
        buyOrders = new PriorityQueue<>((a, b) -> Double.compare(b.getPrice(), a.getPrice())); // Highest price first
        sellOrders = new PriorityQueue<>((a, b) -> Double.compare(a.getPrice(), b.getPrice())); // Lowest price first
    }

    // Add order to the buy or sell queue
    public void addOrder(Order order) {
        if (order.getOrderType() == OrderType.BUY) {
            if (order.getOrderCategory() == OrderCategory.MARKET) {
                processMarketOrder(order);
            } else {
                buyOrders.add(order);
            }
        } else {
            if (order.getOrderCategory() == OrderCategory.MARKET) {
                processMarketOrder(order);
            } else {
                sellOrders.add(order);
            }
        }
        matchOrders();
    }

    private void processMarketOrder(Order order) {
        // Market orders are processed immediately
        while (order.getQuantity() > 0 && !sellOrders.isEmpty()) {
            Order sellOrder = sellOrders.peek();
            if (sellOrder.getQuantity() > 0) {
                int tradeQuantity = Math.min(order.getQuantity(), sellOrder.getQuantity());
                executeTrade(order, sellOrder, tradeQuantity);
                order.setQuantity(order.getQuantity() - tradeQuantity);
                sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);
                if (sellOrder.getQuantity() == 0) {
                    sellOrders.poll();
                }
            }
        }
    }

    // Method to match buy and sell orders
    private void matchOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();

            // Match only if the buy price >= sell price
            if (buyOrder.getPrice() >= sellOrder.getPrice()) {
                int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

                // Execute the trade
                executeTrade(buyOrder, sellOrder, tradeQuantity);

                // Adjust the quantities
                buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
                sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

                // If the buy order is completely filled, remove it
                if (buyOrder.getQuantity() == 0) {
                    buyOrders.poll();  // Remove from queue
                }

                // If the sell order is completely filled, remove it
                if (sellOrder.getQuantity() == 0) {
                    sellOrders.poll();  // Remove from queue
                }
            } else {
                break;  // No matching orders available
            }
        }
    }

    // Function to handle trade execution
    private void executeTrade(Order buyOrder, Order sellOrder, int tradeQuantity) {
        double tradePrice = sellOrder.getPrice();  // The price at which the trade is executed
        double totalCost = tradeQuantity * tradePrice;

        // Update the buyer's balance
        buyOrder.getUser().setBalance(buyOrder.getUser().getBalance() - totalCost);
        System.out.println("Buyer " + buyOrder.getUser().getName() + " buys " + tradeQuantity + " shares of " +
                buyOrder.getStock().getSymbol() + " at " + tradePrice + " for $" + totalCost);

        // Update the seller's balance
        sellOrder.getUser().setBalance(sellOrder.getUser().getBalance() + totalCost);
        System.out.println("Seller " + sellOrder.getUser().getName() + " sells " + tradeQuantity + " shares of " +
                sellOrder.getStock().getSymbol() + " at " + tradePrice + " for $" + totalCost);
    }
}

// Exchange class for managing the trading system
class Exchange {
    private OrderBook orderBook = new OrderBook();

    public void placeOrder(Order order) {
        // Validate the order
        if (validateOrder(order)) {
            orderBook.addOrder(order);
        } else {
            System.out.println("Invalid order: Insufficient funds or invalid quantity.");
        }
    }

    // Order validation logic
    private boolean validateOrder(Order order) {
        if (order.getOrderType() == OrderType.BUY) {
            double requiredBalance = order.getQuantity() * order.getPrice();
            return order.getQuantity() > 0 && order.getUser().getBalance() >= requiredBalance;
        } else if (order.getOrderType() == OrderType.SELL) {
            // For simplicity, assume sellers always have enough shares (extend logic for inventory)
            return order.getQuantity() > 0;
        }
        return false;
    }
}

// Market Data class for stock price information
class MarketData {
    private Map<String, Stock> stocks = new HashMap<>();

    // Add stock to the market data
    public void addStock(String symbol, double price) {
        stocks.put(symbol, new Stock(symbol, price));
    }

    // Get stock data
    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }
}

// Main trading system class to demonstrate all functionalities
public class TradingSystem {
    public static void main(String[] args) {
        // Create exchange and market data
        Exchange exchange = new Exchange();
        MarketData marketData = new MarketData();
        marketData.addStock("AAPL", 150.0);
        marketData.addStock("GOOGL", 2800.0);
        marketData.addStock("MSFT", 300.0);
        marketData.addStock("TSLA", 700.0);

        // Create users with initial balance
        User buyer1 = new User("Buyer1", 10000);
        User seller1 = new User("Seller1", 2000);
        User seller2 = new User("Seller2", 3000);
        User seller3 = new User("Seller3", 5000);

        // Get stock information
        Stock appleStock = marketData.getStock("AAPL");
        Stock googleStock = marketData.getStock("GOOGL");
        Stock microsoftStock = marketData.getStock("MSFT");
        Stock teslaStock = marketData.getStock("TSLA");

        // Add buy and sell orders to the exchange
        exchange.placeOrder(new Order(buyer1, appleStock, 100, OrderType.BUY, 150.0, OrderCategory.LIMIT)); // Limit buy order
        exchange.placeOrder(new Order(seller1, appleStock, 30, OrderType.SELL, 149.0, OrderCategory.LIMIT)); // Limit sell order
        exchange.placeOrder(new Order(seller2, appleStock, 50, OrderType.SELL, 150.0, OrderCategory.LIMIT)); // Limit sell order

        // Market orders
        exchange.placeOrder(new Order(buyer1, googleStock, 10, OrderType.BUY, 0.0, OrderCategory.MARKET)); // Market buy order
        exchange.placeOrder(new Order(seller3, googleStock, 10, OrderType.SELL, 2800.0, OrderCategory.MARKET)); // Market sell order
        exchange.placeOrder(new Order(buyer1, microsoftStock, 20, OrderType.BUY, 300.0, OrderCategory.LIMIT)); // Limit buy order
        exchange.placeOrder(new Order(seller2, microsoftStock, 20, OrderType.SELL, 300.0, OrderCategory.LIMIT)); // Limit sell order
        exchange.placeOrder(new Order(buyer1, teslaStock, 5, OrderType.BUY, 0.0, OrderCategory.MARKET)); // Market buy order
        exchange.placeOrder(new Order(seller3, teslaStock, 5, OrderType.SELL, 700.0, OrderCategory.MARKET)); // Market sell order
    }
}