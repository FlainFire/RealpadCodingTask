package by.aakavity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Set;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitcoinPriceData {

    private Set<BitcoinPrice> historical;
    private BitcoinPrice current;

    public SimpleRegression getRegression() {
        var regression = new SimpleRegression();
        var prevPrice = 0d;
        for (BitcoinPrice bitcoinPrice : historical) {
            regression.addData(prevPrice, bitcoinPrice.getPrice());
            prevPrice = bitcoinPrice.getPrice();
        }
        return regression;
    }

    public double getMin() {
        return Stream.concat(historical.stream(), Stream.of(current)).mapToDouble(BitcoinPrice::getPrice).min().orElse(0);
    }

    public double getMax() {
        return Stream.concat(historical.stream(), Stream.of(current)).mapToDouble(BitcoinPrice::getPrice).max().orElse(Double.MAX_VALUE);
    }
}
