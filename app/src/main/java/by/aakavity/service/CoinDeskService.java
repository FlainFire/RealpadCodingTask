package by.aakavity.service;

import by.aakavity.model.BitcoinPrice;
import by.aakavity.model.BitcoinPriceData;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CoinDeskService {

    public static final String COIN_DESK_CURRENT_PRICE_URL = "https://api.coindesk.com/v1/bpi/currentprice/USD.json";
    public static final String COIN_DESK_HISTORICAL_URL_FORMAT = "https://api.coindesk.com/v1/bpi/historical/close.json?start=%s&end=%s";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BitcoinPriceData getBitcoinPrices(int countOfRecordsToGet) throws IOException {
        var gson = new Gson();

        var currentPriceJson = IOUtils.toString(new URL(COIN_DESK_CURRENT_PRICE_URL), StandardCharsets.UTF_8);
        var coinDeskCurrentPriceResponse = gson.fromJson(currentPriceJson, CoinDeskCurrentPriceResponse.class);
        var historicalUrlWithDates = String.format(COIN_DESK_HISTORICAL_URL_FORMAT,
                LocalDate.now().minusDays(countOfRecordsToGet).format(DATE_TIME_FORMATTER),
                LocalDate.now().format(DATE_TIME_FORMATTER)
        );
        var historicalDataJson = IOUtils.toString(new URL(historicalUrlWithDates), StandardCharsets.UTF_8);
        var coinDeskHistoricalPriceResponse = gson.fromJson(historicalDataJson, CoinDeskHistoricalPricesResponse.class);

        var currentBitcoinPrice = BitcoinPrice.builder()
                .date(LocalDate.now())
                .price(coinDeskCurrentPriceResponse.getBpi().getUsd().getRate())
                .build();

        Set<BitcoinPrice> historicalBitcoinPrice = coinDeskHistoricalPriceResponse.getBpi().entrySet().stream()
                .map(entry -> BitcoinPrice.builder()
                        .date(LocalDate.parse(entry.getKey(), DATE_TIME_FORMATTER))
                        .price(entry.getValue())
                        .build())
                .collect(Collectors.toCollection(TreeSet::new));

        return checkAndGetData(currentBitcoinPrice, historicalBitcoinPrice);
    }

    private BitcoinPriceData checkAndGetData(BitcoinPrice currentBitcoinPrice,
                                             Set<BitcoinPrice> historicalBitcoinPrice) throws IOException {

        if (Objects.isNull(currentBitcoinPrice) || Objects.isNull(currentBitcoinPrice.getPrice())
                || historicalBitcoinPrice.stream().anyMatch(price -> Objects.isNull(price)
                || Objects.isNull(price.getDate())
                || Objects.isNull(price.getPrice()))) {
            throw new IOException("Data that we get from CoinDesk looks invalid, please check it.");
        }

        return BitcoinPriceData.builder().current(currentBitcoinPrice).historical(historicalBitcoinPrice).build();
    }

    @Getter
    private static class CoinDeskCurrentPriceResponse {
        private BitcoinCurrentPriceInfoCurrency bpi;
    }

    @Getter
    private static class BitcoinCurrentPriceInfoCurrency {
        @SerializedName("USD")
        private BitcoinCurrentPriceInfo usd;
    }

    @Getter
    private static class BitcoinCurrentPriceInfo {
        @SerializedName("rate_float")
        private Double rate;
    }

    @Getter
    private static class CoinDeskHistoricalPricesResponse {
        private Map<String, Double> bpi;
    }
}
