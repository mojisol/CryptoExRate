package com.currency.android.cryptoexrate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.currency.android.cryptoexrate.CurrencyRate;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String url;
    public CurrencyRate currencyRate;
    RequestQueue requestQueue;
    LinearLayout ll;
    ListView ratesListView;
    SwipeRefreshLayout swipeRefreshLayout;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ll = (LinearLayout) findViewById(R.id.main_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        ratesListView = (ListView) findViewById(R.id.rates_list_view);
        ratesListView.setDivider(null); //to remove dividers from the list view

        currencyRate = new CurrencyRate();

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        settings = getSharedPreferences("mSettings", MODE_PRIVATE);




        url = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=BTC,ETH&tsyms=" +
                "NGN,AED,EUR,PEN,DZD,AUD,CAD,USD,AED,RON,KES,GHS,NOK,RUB,DKK,MAD,NZD,MYR,HRK,GEL,INR";
        //Currencies will NOT be displayed in this here order
        downloadRates();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadRates();
            }
        });

    }

    private static class ViewHolder {
        private TextView currencyTextView;
        private TextView btcTextView;
        private TextView ethTextView;
    }


    private class MyAdapter extends BaseAdapter {
        ArrayList<CurrencyRate.MoneyRate> rates;

        private MyAdapter(ArrayList<CurrencyRate.MoneyRate> ratesInstance) {
            rates = ratesInstance;
        }

        public int getCount() {
            return rates.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("DefaultLocale")
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            ViewHolder holder;



            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_view, parent, false);

                holder = new ViewHolder();
                holder.currencyTextView = (TextView) convertView.findViewById(R.id.currency_name);
                holder.btcTextView = (TextView) convertView.findViewById(R.id.btc_rate);
                holder.ethTextView = (TextView) convertView.findViewById(R.id.eth_rate);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final CurrencyRate.MoneyRate rateRow = rates.get(position);


            final String crossCurrency = rateRow.getCurrency();
            final double crossBtc = rateRow.getBtcRate();
            final double crossEth = rateRow.getEthRate();

            holder.currencyTextView.setText(crossCurrency);
            holder.btcTextView.setText(String.format("%1$,.2f", crossBtc));
            holder.ethTextView.setText(String.format("%1$,.2f", crossEth));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ConversionActivity.class);
                    intent.putExtra(Constants.EXTRA_CURRENCY, crossCurrency);
                    intent.putExtra(Constants.EXTRA_BTC_RATE, crossBtc);
                    intent.putExtra(Constants.EXTRA_ETH_RATE, crossEth);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim);
                }
            });

            return convertView;

        }
    }


    //Method downloads the rates of the 20 currencies in the URL, receives JSON response, parses response and displays rates
    public void downloadRates() {
        currencyRate = new CurrencyRate();

        JsonObjectRequest requestNameAvatar = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject btc_rates = response.getJSONObject("BTC".trim());
                            JSONObject eth_rates = response.getJSONObject("ETH".trim());

                            Iterator<?> keysBTC = btc_rates.keys();
                            Iterator<?> keysETH = eth_rates.keys();


                            while(keysBTC.hasNext() && keysETH.hasNext()) {
                                String keyBTC = (String) keysBTC.next();
                                String keyETH = (String) keysETH.next();


                                currencyRate.add(keyBTC, btc_rates.getDouble(keyBTC), eth_rates.getDouble(keyETH));
                            }

                            swipeRefreshLayout.setRefreshing(false); //to remove the progress bar for refresh
                            ratesListView.setAdapter(new MyAdapter(currencyRate.mrArrayList));


                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error parsing data", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {


            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(ll, Constants.REFRESH_ERROR, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                swipeRefreshLayout.setRefreshing(false);
            }



        });

        requestQueue.add(requestNameAvatar);
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestQueue.cancelAll(this);
    }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            swipeRefreshLayout.setRefreshing(true); //to show the progress bar for refresh
            downloadRates();
            return true;
        }
        ratesListView.setAdapter(new MyAdapter(currencyRate.mrArrayList));
        SharedPreferences.Editor editor = settings.edit();
        editor.apply();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cryptocompare) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cryptocompare.com/api/#introduction"));
            startActivity(browserIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}