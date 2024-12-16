package com.swisspost;

import com.swisspost.exception.InvalidSymbolException;
import com.swisspost.service.PriceFetcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);

//        PriceFetcher priceFetcher = context.getBean(PriceFetcher.class);
//
//        List<String> symbols = List.of("BTC", "ETH", "BNB", "XRP", "SOL", "DOGE");
//
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
//
//        executorService.scheduleWithFixedDelay(() -> symbols.parallelStream().forEach(symbol -> {
//            try {
//                double price = priceFetcher.fetchLatestPrice(symbol);
//                System.out.println(String.format("%-4s price = $%.2f", symbol, price));
//            } catch (InvalidSymbolException e ) {
//                System.err.println("Error: Unsupported symbol - " + symbol);
//            } catch (RuntimeException e) {
//                // Handle general errors, such as issues with fetching prices
//                System.err.println("Error fetching price for " + symbol + ": " + e.getMessage());
//            }
//        }), 1, 5, TimeUnit.SECONDS);
    }
}
