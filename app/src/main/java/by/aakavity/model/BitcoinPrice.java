package by.aakavity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitcoinPrice implements Comparable<BitcoinPrice> {

    private Double price;
    private LocalDate date;

    @Override
    public int compareTo(BitcoinPrice o) {
        return date.compareTo(o.date);
    }
}
