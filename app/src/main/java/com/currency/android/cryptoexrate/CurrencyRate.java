package com.currency.android.cryptoexrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by MOJISOLA on 03/11/2017.
 */

public class CurrencyRate {
    ArrayList<MoneyRate> mrArrayList = new ArrayList<>();

    CurrencyRate() {

    }
    void add(String currency, Double btcRate, Double ethRate) {
        MoneyRate mRate = new MoneyRate( currency, btcRate, ethRate);
        mrArrayList.add(mRate);
    }
    void orderList(final int mode) {
        Collections.sort(mrArrayList, new Comparator<MoneyRate>() {
            @Override
            public int compare(MoneyRate lhs, MoneyRate rhs) {
                if(mode == Constants.ORDER_ALPHABETICAL)return lhs.getCurrency().compareTo(rhs.getCurrency());
                else return Double.compare(lhs.getBtcRate(), rhs.getBtcRate());
            }
        });
    }

    class MoneyRate{
        private String currency;
        private double btcRate, ethRate;


        MoneyRate( String currency, double btcRate, double ethRate) {
            this.currency = currency;
            this.btcRate = btcRate;
            this.ethRate = ethRate;

        }

        String getCurrency() {
            return currency;
        }

        double getBtcRate() {
            return btcRate;
        }

        double getEthRate() {
            return ethRate;
        }



    }


}

