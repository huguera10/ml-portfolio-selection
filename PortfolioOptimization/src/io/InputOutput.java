/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import parameters.Parameters;
import stockmarket.Asset;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class InputOutput {

    public Portfolio readFileSpecificDate(String filePath, String initDate, String finDate) throws FileNotFoundException, IOException, ParseException {
        Portfolio portfolio = new Portfolio();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String[] values;
        String row;

        // Header
        row = br.readLine();
        values = row.split(",");

        for (int i = 1; i < values.length; i++) {
            portfolio.addAsset(new Asset(values[i]));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (finDate.equals("")) {
            finDate = sdf.format(new Date());
        }
        Date initialDate = sdf.parse(initDate);
        Date finalDate = sdf.parse(finDate);

        while ((row = br.readLine()) != null) {
            values = row.split(",");

            Date date = sdf.parse(values[0]);
            if (date.compareTo(initialDate) >= 0
                    && date.compareTo(finalDate) <= 0) {

                portfolio.addDate(values[0]);

                for (int i = 1; i < values.length; i++) {
                    portfolio.addReturnToAsset(Double.parseDouble(values[i]), i - 1);
                }
            }
        }

        return portfolio;
    }

    public Portfolio readFile(String filePath) throws FileNotFoundException, IOException, ParseException {
        Portfolio portfolio = new Portfolio();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String[] values;
        String row;

        // Header      
        row = br.readLine();
        values = row.split(",");

        for (int i = 1; i < values.length; i++) {
            portfolio.addAsset(new Asset(values[i]));
        }

        while ((row = br.readLine()) != null) {
            values = row.split(",");

            portfolio.addDate(values[0]);
            for (int i = 1; i < values.length; i++) {
                if (values[i].equals("")) {
                    values[i] = "0.0";
                }
                portfolio.addReturnToAsset(Double.parseDouble(values[i]), i - 1);
            }
        }

        return portfolio;
    }

    private void _write(Parameters params, Portfolio portfolio, String filePath) throws IOException {
        System.out.println("=============================================================");
        System.out.println("\nWriting portfolio weights to file: " + filePath);
        System.out.println("=============================================================");

        File file = new File(filePath);

        if (!(file.exists())) {
            file.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        bw.write("{\n");
        bw.write("\t\"DATA_PATH\": \"" + params.getDataPath() + "\",\n");
        bw.write("\t\"START_DATE\": \"" + portfolio.getDateGivenIndex(0) + "\",\n");
        bw.write("\t\"END_DATE\": \"" + portfolio.getDateGivenIndex(portfolio.getS() - 1) + "\",\n");

        bw.write("\t\"HISTORICAL_DAYS\": \"" + params.getHistoricalDays() + "\",\n");
        bw.write("\t\"OPTIMIZATION_TIME_LIMIT\": \"" + params.getOptimizationTimeLimit() + "\",\n");
        bw.write("\t\"OPTIMIZATION_TYPE\": \"" + params.getOptimizationType()+ "\",\n");
        

        bw.write("\t\"PORTFOLIO\": {\n");
        boolean isFirstAsset = true;
        for (int i = 0; i < portfolio.getN(); i++) {
            Asset asset = portfolio.getAsset(i);
            if (asset.getWeigth() > 0.0) {
                    if (!isFirstAsset) {
                        bw.write(",\n");
                    }
                    bw.write("\t\t\"" + asset.getName() + "\": \"" + asset.getWeigth() + "\"");
                    isFirstAsset = false;
            }
        }
        bw.write("\t}\n");

        bw.write("}\n");

        bw.close();
    }

    public void writePortfoliosWeightsDictionary(Parameters params, Portfolio portfolio) throws IOException {
        String filePath = params.getPortfolioWeightsResultPath();

        this._write(params, portfolio, filePath);
    }

    public void writePortfoliosWeightsDictionaryId(Parameters params, Portfolio portfolio, int id) throws IOException {
        String filePath = params.getPortfolioWeightsResultPath();

        filePath = filePath.replace(".json", "_" + id + ".json");

        this._write(params, portfolio, filePath);
    }
}
