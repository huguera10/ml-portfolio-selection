/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parameters;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

/**
 *
 * @author hugo
 */
public class Parameters {

    int maxNumberOfAssets;
    int historicalDays;
    int optimizationTimeLimit = 300;

    double alpha; // cvar alpha

    String date;
    String dataPath;
    String trackedAssetName; // ssd
    String optimizationType; // cvar
    String portfolioWeightsResultPath;

    public Parameters(String parametersPath) throws FileNotFoundException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        Object object = jsonParser.parse(new FileReader(parametersPath));
        JSONObject jsonObject = (JSONObject) object;

        parseJsonElements(jsonObject);
    }

    private void parseJsonElements(JSONObject jsonObject) {

        historicalDays = (int) (long) jsonObject.getOrDefault("historicalDays", 100);
        optimizationTimeLimit = (int) (long) jsonObject.getOrDefault("optimizationTimeLimit", 300);

        date = (String) jsonObject.getOrDefault("date", "2017-01-01");
        dataPath = (String) jsonObject.getOrDefault("dataPath", "../data/sp500.csv");
        optimizationType = (String) jsonObject.getOrDefault("optimizationType", "cvar");
        portfolioWeightsResultPath = (String) jsonObject.getOrDefault(
                "portfolioWeightsResultPath",
                "../portfolio_weights/portfolio_weights_011.json"
        );

        trackedAssetName = (String) jsonObject.getOrDefault("trackedAssetName", "IBOV");
        alpha = (double) jsonObject.getOrDefault("alpha", 0.05);
        maxNumberOfAssets = (int) (long) jsonObject.getOrDefault("maxNumberOfAssets", -1);

    }

    public String getDataPath() {
        return dataPath;
    }

    public int getHistoricalDays() {
        return historicalDays;
    }

    public String getDate() {
        return date;
    }

    public String getPortfolioWeightsResultPath() {
        return portfolioWeightsResultPath;
    }

    public int getOptimizationTimeLimit() {
        return optimizationTimeLimit;
    }

    public String getOptimizationType() {
        return optimizationType;
    }

    public double getAlpha() {
        return alpha;
    }

    public String getTrackedAssetName() {
        return trackedAssetName;
    }

    public int getMaxNumberOfAssets() {
        return maxNumberOfAssets;
    }
}
