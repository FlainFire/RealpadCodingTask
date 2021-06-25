package by.aakavity;

import by.aakavity.service.BitcoinChartService;
import by.aakavity.service.CoinDeskService;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class App {

    public static void main(String[] args) {
        try {
            var bitcoinPrices = new CoinDeskService().getBitcoinPrices(7);

            var regression = bitcoinPrices.getRegression();
            var predict = regression.predict(bitcoinPrices.getCurrent().getPrice());

            new BitcoinChartService(bitcoinPrices, predict).setVisible(true);
        } catch (IOException e) {
            log.log(Level.WARNING, "Cannot run application due to: " + e.getMessage());
        }
    }
}
